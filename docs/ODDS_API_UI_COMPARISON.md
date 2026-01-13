# The Odds API vs Frontend UI Market Comparison

## Frontend UI Requirements (from `BettingMarkets` type)

The frontend expects the following betting markets for soccer matches:

### 1. **Match Result** (`match_result`)
- `home_win` (number)
- `draw` (number)
- `away_win` (number)

### 2. **Over/Under** (`over_under`)
- `over_0_5` (number)
- `under_0_5` (number)
- `over_1_5` (number)
- `under_1_5` (number)
- `over_2_5` (number)
- `under_2_5` (number)
- `over_3_5` (number)
- `under_3_5` (number)
- `over_4_5` (number)
- `under_4_5` (number)

### 3. **Both Teams to Score** (`btts`)
- `yes` (number)
- `no` (number)

### 4. **Double Chance** (`double_chance`)
- `home_draw` (number)
- `home_away` (number)
- `away_draw` (number)

### 5. **Handicap** (`handicap`)
- Multiple handicap lines (e.g., `-1.5`, `-1`, `-0.5`, `+0.5`, `+1`, `+1.5`)
- Each line has an associated odd (number)

---

## The Odds API Coverage for Soccer

### ✅ **Available Markets**

#### 1. **Match Result** → `h2h` market
- **Status**: ✅ **FULLY AVAILABLE**
- **API Market Key**: `h2h`
- **Outcomes**: 
  - Home team (name)
  - Draw (name: "Draw")
  - Away team (name)
- **Endpoint**: Main `/odds` endpoint
- **Coverage**: Available for all soccer leagues
- **Mapping**: Direct mapping possible
  - `h2h` → `match_result` (home_win, draw, away_win)

#### 2. **Over/Under** → `totals` market
- **Status**: ✅ **AVAILABLE** (with limitations)
- **API Market Key**: `totals`
- **Outcomes**:
  - Over (with `point` value)
  - Under (with `point` value)
- **Endpoint**: Main `/odds` endpoint
- **Coverage**: Available for soccer, but typically returns ONE line per bookmaker
- **Multiple Lines**: 
  - The main `totals` market usually returns the most popular line (often 2.5)
  - For multiple lines (0.5, 1.5, 2.5, 3.5, 4.5), we need:
    - **Option 1**: Use `alternate_totals` market (additional market)
    - **Option 2**: Make multiple API calls with different filters
- **Mapping**: 
  - `totals` → `over_under` (need to collect multiple lines)
  - `alternate_totals` → Additional over/under lines

#### 3. **Both Teams to Score** → `btts` market
- **Status**: ✅ **AVAILABLE** (additional market)
- **API Market Key**: `btts`
- **Outcomes**:
  - Yes
  - No
- **Endpoint**: Additional market (requires separate request)
- **Coverage**: Available for soccer (EPL, La Liga, Serie A, Bundesliga, Ligue 1, MLS)
- **Note**: This is an "additional market" that needs to be requested separately
- **Mapping**: Direct mapping possible
  - `btts` → `btts` (yes, no)

#### 4. **Double Chance** → `double_chance` market
- **Status**: ✅ **AVAILABLE** (additional market)
- **API Market Key**: `double_chance`
- **Outcomes**:
  - Home/Draw
  - Home/Away
  - Draw/Away
- **Endpoint**: Additional market (requires separate request)
- **Coverage**: Available for soccer
- **Note**: This is an "additional market" that needs to be requested separately
- **Mapping**: Direct mapping possible
  - `double_chance` → `double_chance` (home_draw, home_away, away_draw)

#### 5. **Handicap** → `spreads` market
- **Status**: ✅ **AVAILABLE** (with limitations)
- **API Market Key**: `spreads`
- **Outcomes**:
  - Home team (with `point` value)
  - Away team (with `point` value)
- **Endpoint**: Main `/odds` endpoint
- **Coverage**: Available for soccer, but typically returns ONE line per bookmaker
- **Multiple Lines**: 
  - The main `spreads` market usually returns the most popular handicap line
  - For multiple lines (-1.5, -1, -0.5, +0.5, +1, +1.5), we need:
    - **Option 1**: Use `alternate_spreads` market (additional market)
    - **Option 2**: Make multiple API calls with different filters
- **Mapping**: 
  - `spreads` → `handicap` (need to collect multiple lines)
  - `alternate_spreads` → Additional handicap lines

---

## Summary

### ✅ **All Required Markets Are Available**

| Frontend Market | The Odds API Market | Status | Notes |
|----------------|---------------------|--------|-------|
| `match_result` | `h2h` | ✅ Fully Available | Direct mapping |
| `over_under` | `totals` + `alternate_totals` | ✅ Available | Need multiple lines |
| `btts` | `btts` | ✅ Available | Additional market |
| `double_chance` | `double_chance` | ✅ Available | Additional market |
| `handicap` | `spreads` + `alternate_spreads` | ✅ Available | Need multiple lines |

### ⚠️ **Important Considerations**

1. **Additional Markets**: `btts` and `double_chance` are "additional markets" that:
   - Need to be requested separately
   - May require using `/events/{eventId}/odds` endpoint for individual events
   - Update at 1-minute intervals (vs main markets which update more frequently)

2. **Multiple Lines**: For `over_under` and `handicap`:
   - Main markets (`totals`, `spreads`) typically return ONE line per bookmaker
   - To get multiple lines (0.5, 1.5, 2.5, 3.5, 4.5), we need:
     - Use `alternate_totals` and `alternate_spreads` markets
     - These are additional markets that may require separate requests

3. **API Endpoint Strategy**:
   - **Main markets** (`h2h`, `spreads`, `totals`): Available via `/sports/{sport}/odds`
   - **Additional markets** (`btts`, `double_chance`, `alternate_totals`, `alternate_spreads`): 
     - May need `/events/{eventId}/odds` endpoint
     - Or include in main request if supported

4. **Usage Quota**:
   - Cost = number of markets × number of regions
   - Requesting all markets: `h2h,spreads,totals,btts,double_chance,alternate_totals,alternate_spreads` = 7 markets
   - With 3 regions (us,uk,eu) = 21 credits per request

---

## Recommended Implementation Strategy

### Option 1: Two-Step Approach (Recommended)
1. **Step 1**: Fetch main markets via `/sports/{sport}/odds`
   - Markets: `h2h,spreads,totals`
   - Get match_result, basic handicap, basic over/under
   
2. **Step 2**: For each match, fetch additional markets via `/events/{eventId}/odds`
   - Markets: `btts,double_chance,alternate_totals,alternate_spreads`
   - Get complete odds data

### Option 2: Single Request (If Supported)
- Request all markets in one call: `h2h,spreads,totals,btts,double_chance,alternate_totals,alternate_spreads`
- Check if The Odds API supports this for soccer

### Option 3: Cached Approach
- Fetch main markets frequently (every 5-10 minutes)
- Fetch additional markets less frequently (every 15-30 minutes)
- Cache and combine results

---

## Conclusion

✅ **YES, The Odds API provides ALL the markets required by the frontend UI.**

However, some markets are "additional markets" that require:
- Separate API requests (possibly per-event)
- Different endpoint usage (`/events/{eventId}/odds`)
- Consideration of usage quota costs

The implementation needs to:
1. Request main markets (`h2h`, `spreads`, `totals`) from the main endpoint
2. Request additional markets (`btts`, `double_chance`, `alternate_totals`, `alternate_spreads`) per event
3. Aggregate and map all markets to the frontend's expected structure
