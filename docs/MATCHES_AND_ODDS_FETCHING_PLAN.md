# Matches and Odds Fetching Plan

## Key Confirmations from The Odds API Documentation

### ✅ Multiple Markets in One Request
- **Confirmed**: Multiple markets can be specified as comma-separated values
- **Example**: `markets=h2h,spreads,totals,btts,double_chance`
- **Cost**: Each market costs 1 credit per region
- **Example Cost**: 5 markets × 3 regions (us,uk,eu) = 15 credits per request

### ✅ Date Filtering (When NOT Using "upcoming")
- **Confirmed**: `commenceTimeFrom` and `commenceTimeTo` work when using specific sport keys
- **Important**: These parameters have NO EFFECT if sport is set to "upcoming"
- **Benefit**: We can filter by custom date ranges (weekend, midweek, specific weeks, etc.)
- **Format**: ISO 8601 format (e.g., `2023-09-09T00:00:00Z`)

### ✅ Avoiding the 8-Match Limit
- **Problem**: Using `sport=upcoming` returns only the next 8 games across all sports
- **Solution**: Use specific sport keys (e.g., `soccer_epl`) with date filters
- **Benefit**: Get all matches within our date range, not limited to 8

---

## Implementation Strategy

### Two-API-Call Approach

#### **Call 1: Main Markets** (Primary Request)
```
GET /v4/sports/{sportKey}/odds
Parameters:
  - apiKey: {apiKey}
  - regions: us,uk,eu
  - markets: h2h,spreads,totals
  - oddsFormat: decimal
  - commenceTimeFrom: {startDate} (ISO 8601)
  - commenceTimeTo: {endDate} (ISO 8601)
```

**Returns:**
- Match Result (h2h): Home, Draw, Away
- Handicap (spreads): One main handicap line per bookmaker
- Over/Under (totals): One main over/under line per bookmaker

**Cost:** 3 markets × 3 regions = 9 credits per request

---

#### **Call 2: Additional Markets** (Secondary Request)
```
GET /v4/sports/{sportKey}/odds
Parameters:
  - apiKey: {apiKey}
  - regions: us,uk,eu
  - markets: btts,double_chance,alternate_totals,alternate_spreads
  - oddsFormat: decimal
  - commenceTimeFrom: {startDate} (ISO 8601)
  - commenceTimeTo: {endDate} (ISO 8601)
```

**Returns:**
- Both Teams to Score (btts): Yes, No
- Double Chance: Home/Draw, Home/Away, Away/Draw
- Alternate Totals: Multiple over/under lines (0.5, 1.5, 2.5, 3.5, 4.5)
- Alternate Spreads: Multiple handicap lines (-1.5, -1, -0.5, +0.5, +1, +1.5)

**Cost:** 4 markets × 3 regions = 12 credits per request

**Total Cost per League Sync:** 9 + 12 = 21 credits

---

## Date Range Strategy

### Custom Date Ranges

Instead of using `sport=upcoming` (which limits to 8 matches), we'll:

1. **Define Custom Date Ranges**
   - Weekend matches: Friday 00:00 to Sunday 23:59
   - Midweek matches: Monday 00:00 to Thursday 23:59
   - Weekly: Monday 00:00 to Sunday 23:59
   - Custom periods: Any date range we specify

2. **Per-League Fetching**
   - Fetch matches for each league separately
   - Use league's `externalId` as `sportKey` (e.g., `soccer_epl`, `soccer_spain_la_liga`)
   - Apply date filters to each league

3. **Example Implementation**
   ```java
   // Get matches for this weekend (Friday to Sunday)
   String startDate = "2024-01-12T00:00:00Z"; // Friday
   String endDate = "2024-01-14T23:59:59Z";   // Sunday
   
   // Fetch for EPL
   fetchOdds("soccer_epl", startDate, endDate, "h2h,spreads,totals");
   fetchOdds("soccer_epl", startDate, endDate, "btts,double_chance,alternate_totals,alternate_spreads");
   ```

---

## Data Flow

### 1. **Match Sync Service** (Scheduled Job)
   - Runs periodically (e.g., every 6 hours)
   - For each active league:
     - Calculate date range (e.g., next 7 days)
     - Make Call 1 (main markets)
     - Make Call 2 (additional markets)
     - Parse and merge results
     - Save/update matches in database

### 2. **On-Demand Fetching** (API Endpoint)
   - When user requests upcoming matches:
     - Check database for matches in date range
     - If missing or stale, fetch from The Odds API
     - Return combined data

### 3. **Odds Aggregation**
   - Combine results from both API calls
   - Match events by `id` (event ID from The Odds API)
   - Aggregate odds from multiple bookmakers (use best odds or average)
   - Map to frontend structure

---

## API Client Implementation

### Method Signatures

```java
/**
 * Fetch main markets (h2h, spreads, totals)
 */
public String fetchMainMarkets(
    String sportKey, 
    String commenceTimeFrom, 
    String commenceTimeTo
)

/**
 * Fetch additional markets (btts, double_chance, alternate_totals, alternate_spreads)
 */
public String fetchAdditionalMarkets(
    String sportKey, 
    String commenceTimeFrom, 
    String commenceTimeTo
)

/**
 * Generic method for fetching odds with custom markets
 */
public String fetchOdds(
    String sportKey,
    String regions,
    String markets,
    String commenceTimeFrom,
    String commenceTimeTo,
    String oddsFormat
)
```

---

## Data Mapping Strategy

### Mapping The Odds API Response to Frontend Structure

#### 1. **Match Result** (`h2h` → `match_result`)
```json
// The Odds API
{
  "key": "h2h",
  "outcomes": [
    {"name": "Manchester United", "price": 2.5},
    {"name": "Draw", "price": 3.2},
    {"name": "Liverpool", "price": 2.8}
  ]
}

// Frontend Structure
{
  "match_result": {
    "home_win": 2.5,
    "draw": 3.2,
    "away_win": 2.8
  }
}
```

#### 2. **Over/Under** (`totals` + `alternate_totals` → `over_under`)
```json
// The Odds API (multiple markets)
{
  "key": "totals",
  "point": 2.5,
  "outcomes": [
    {"name": "Over", "price": 1.8},
    {"name": "Under", "price": 2.0}
  ]
}
// ... plus alternate_totals with other lines

// Frontend Structure
{
  "over_under": {
    "over_0_5": 1.15,
    "under_0_5": 5.0,
    "over_1_5": 1.40,
    "under_1_5": 2.75,
    "over_2_5": 1.80,
    "under_2_5": 2.00,
    "over_3_5": 2.50,
    "under_3_5": 1.50,
    "over_4_5": 4.00,
    "under_4_5": 1.25
  }
}
```

#### 3. **Both Teams to Score** (`btts` → `btts`)
```json
// The Odds API
{
  "key": "btts",
  "outcomes": [
    {"name": "Yes", "price": 1.75},
    {"name": "No", "price": 2.10}
  ]
}

// Frontend Structure
{
  "btts": {
    "yes": 1.75,
    "no": 2.10
  }
}
```

#### 4. **Double Chance** (`double_chance` → `double_chance`)
```json
// The Odds API
{
  "key": "double_chance",
  "outcomes": [
    {"name": "Home/Draw", "price": 1.25},
    {"name": "Home/Away", "price": 1.15},
    {"name": "Draw/Away", "price": 2.00}
  ]
}

// Frontend Structure
{
  "double_chance": {
    "home_draw": 1.25,
    "home_away": 1.15,
    "away_draw": 2.00
  }
}
```

#### 5. **Handicap** (`spreads` + `alternate_spreads` → `handicap`)
```json
// The Odds API (multiple markets)
{
  "key": "spreads",
  "outcomes": [
    {"name": "Manchester United", "price": 1.9, "point": -1.5},
    {"name": "Liverpool", "price": 1.9, "point": 1.5}
  ]
}
// ... plus alternate_spreads with other lines

// Frontend Structure
{
  "handicap": {
    "-1.5": 2.90,
    "-1": 2.20,
    "-0.5": 1.60,
    "+0.5": 2.40,
    "+1": 3.20,
    "+1.5": 4.50
  }
}
```

---

## Bookmaker Aggregation Strategy

### Option 1: Best Odds (Recommended)
- For each outcome, select the best (highest) odds across all bookmakers
- Provides best value for users
- Simple to implement

### Option 2: Average Odds
- Calculate average odds across all bookmakers
- More stable, less volatile
- May not reflect actual best available odds

### Option 3: Most Popular Bookmaker
- Use odds from a single trusted bookmaker (e.g., first in list)
- Consistent source
- May not have best odds

**Recommendation**: Use **Best Odds** for user value, with fallback to first bookmaker if no comparison available.

---

## Error Handling & Fallbacks

### 1. **API Rate Limiting**
   - Monitor `x-requests-remaining` header
   - Implement exponential backoff
   - Queue requests if quota exhausted

### 2. **Missing Markets**
   - If additional markets fail, still return main markets
   - Log missing markets for monitoring
   - Graceful degradation

### 3. **Date Range Issues**
   - If no matches in date range, return empty list
   - Don't count as error (normal for some periods)

### 4. **Team Name Matching**
   - Use fuzzy matching for team names
   - Store external team IDs for better matching
   - Fallback to name-based matching

---

## Caching Strategy

### 1. **Database Cache**
   - Store matches and odds in database
   - Update via scheduled sync jobs
   - Serve from database for API requests

### 2. **In-Memory Cache**
   - Cache recent API responses (5-10 minutes)
   - Reduce redundant API calls
   - Use Spring Cache

### 3. **Stale Data Handling**
   - Mark odds as stale after 1 hour
   - Still serve stale data if API unavailable
   - Background refresh

---

## Usage Quota Management

### Daily Quota Tracking
- Track `x-requests-remaining` header
- Implement quota monitoring
- Alert when quota low

### Cost Optimization
- Batch requests by date range
- Cache aggressively
- Only sync active leagues
- Skip weekends for inactive leagues

### Example Calculation
- **Leagues to sync**: 20 active leagues
- **Sync frequency**: Every 6 hours (4 times/day)
- **Cost per sync**: 21 credits per league
- **Daily cost**: 20 leagues × 4 syncs × 21 credits = 1,680 credits/day
- **Monthly cost**: ~50,400 credits/month

---

## Implementation Phases

### Phase 1: Basic Implementation
- [ ] Extend `TheOddsApiClient` with date filtering
- [ ] Implement two-call strategy (main + additional markets)
- [ ] Basic mapping to frontend structure
- [ ] Save matches to database

### Phase 2: Advanced Features
- [ ] Bookmaker aggregation (best odds)
- [ ] Team name matching improvements
- [ ] Error handling and fallbacks
- [ ] Caching implementation

### Phase 3: Optimization
- [ ] Usage quota monitoring
- [ ] Smart sync scheduling
- [ ] Performance optimization
- [ ] Monitoring and alerts

---

## Next Steps

1. ✅ Confirm date filtering works with specific sport keys
2. ✅ Implement two-call strategy in `TheOddsApiClient`
3. ✅ Create odds mapping service
4. ✅ Update match sync service
5. ✅ Test with real API calls
6. ✅ Implement caching
7. ✅ Add monitoring
