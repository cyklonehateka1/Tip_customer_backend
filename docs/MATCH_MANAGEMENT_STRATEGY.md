# Match Management Strategy

## Overview

This document outlines the strategy for managing soccer matches in the Tipster application. Matches are fetched from The Odds API and stored in our database for performance, reliability, and historical tracking.

## Core Principle

**Selective Sync**: Only sync matches that are likely to be used, not all matches from all leagues.

---

## Architecture

### 1. Automatic Sync (Scheduled Job)

Syncs matches from **popular leagues only** on a regular schedule.

#### Leagues to Auto-Sync (Top 20-30)

```
MAJOR LEAGUES:
- Premier League (England)
- La Liga (Spain)
- Serie A (Italy)
- Bundesliga (Germany)
- Ligue 1 (France)
- Eredivisie (Netherlands)
- Primeira Liga (Portugal)
- Super Lig (Turkey)
- Russian Premier League
- Belgian Pro League
- Scottish Premiership
- Austrian Bundesliga
- Swiss Super League
- Danish Superliga
- Norwegian Eliteserien
- Swedish Allsvenskan
- MLS (USA)
- Liga MX (Mexico)
- Brasileirão (Brazil)
- Argentine Primera División

MAJOR TOURNAMENTS:
- UEFA Champions League
- UEFA Europa League
- UEFA Europa Conference League
- FIFA World Cup (every 4 years)
- UEFA European Championship (every 4 years)
- Copa América (every 4 years)
- AFCON - Africa Cup of Nations (every 2 years)
- CONCACAF Gold Cup (every 2 years)
- FIFA Club World Cup - Old Format (annually)
- FIFA Club World Cup - New Format (every 4 years, expanded)
- AFC Champions League
- CONCACAF Champions Cup
- Copa Libertadores
- UEFA Nations League
```

#### Sync Frequency

- **Upcoming matches**: Every 12 hours (next 7-14 days)
- **Live matches**: Every 30 minutes (status updates)
- **Finished matches**: Once after completion (score updates)

#### Match Retention

- **Matches in tips**: Keep FOREVER (never delete)
- **Matches not in tips**: Can archive/delete after 3 months

---

### 2. On-Demand Sync (User-Triggered)

When a tipster searches for a match that's not in our database:

1. User searches for match (e.g., "English Championship - Leeds vs Norwich")
2. System checks database → Not found
3. System fetches from The Odds API
4. System saves to database
5. Match is now available for tip creation

**Use Cases:**

- Tipster wants to create tip for a match in a lower league
- Tipster searches for a specific team/match
- Tipster wants matches from a cup competition not in auto-sync list

---

## Data Flow

```
┌─────────────────┐
│  The Odds API   │
└────────┬────────┘
         │
         ├─────────────────┬─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
┌─────────────────┐  ┌──────────────┐  ┌──────────────┐
│  Auto Sync Job  │  │ Search API  │  │ Tip Creation │
│  (Scheduled)    │  │ (On-demand) │  │ (On-demand) │
└────────┬────────┘  └──────┬───────┘  └──────┬───────┘
         │                  │                  │
         └──────────────────┴──────────────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │   Database      │
                   │  - Matches      │
                   │  - Teams        │
                   │  - Leagues      │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  API Endpoints  │
                   │  - List matches │
                   │  - Get match    │
                   │  - Search match │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │   Frontend      │
                   └─────────────────┘
```

---

## Database Schema

### Matches Table

- `id` (UUID) - Primary key
- `external_id` (VARCHAR) - The Odds API match ID
- `league_id` (UUID) - Reference to leagues table
- `home_team_id` (UUID) - Reference to teams table
- `away_team_id` (UUID) - Reference to teams table
- `match_date` (TIMESTAMPTZ) - Scheduled match time
- `status` (ENUM) - scheduled, live, finished, cancelled
- `home_score`, `away_score` - Final scores
- `last_synced_at` (TIMESTAMPTZ) - Last sync timestamp
- `created_at`, `updated_at` - Timestamps

### Relationship with Tips

- `tip_selections.match_id` → `matches.id`
- **Critical**: Never delete matches that are referenced by tips

---

## Configuration

### Application Properties

```properties
# The Odds API Configuration
the-odds-api.api-key=f81341af96fda4a4015ab8e422bb54fe
the-odds-api.base-url=https://api.the-odds-api.com/v4

# Match Sync Configuration
match.sync.enabled=true
match.sync.upcoming-days=14
match.sync.recent-days=7
match.sync.interval-hours=12
match.sync.live-interval-minutes=30

# Match Retention
match.retention.days-for-unused=90
match.retention.keep-referenced-forever=true
```

### League Configuration (JSON/Properties)

List of league keys from The Odds API to auto-sync:

```json
{
  "autoSyncLeagues": [
    "soccer_epl",
    "soccer_spain_la_liga",
    "soccer_italy_serie_a",
    "soccer_germany_bundesliga",
    "soccer_france_ligue_one",
    "soccer_uefa_champions_league",
    "soccer_uefa_europa_league",
    "soccer_world_cup"
  ]
}
```

---

## API Endpoints

### Match Management

- `GET /api/matches` - List matches (with filters: date, league, status)
- `GET /api/matches/{id}` - Get match details
- `GET /api/matches/search` - Search matches (triggers on-demand sync)
- `POST /api/admin/matches/sync` - Manual sync trigger (admin only)

### League Management

- `GET /api/leagues` - List all leagues
- `GET /api/leagues/{id}/matches` - Get matches for a league

---

## Implementation Components

### 1. The Odds API Client

- Service to interact with The Odds API
- Handles authentication, rate limiting, error handling
- Maps API responses to our domain models

### 2. Match Sync Service

- Scheduled job for automatic sync
- Fetches matches from configured leagues
- Updates existing matches, creates new ones
- Handles teams and leagues creation/updates

### 3. Match Service

- Business logic for match operations
- Handles on-demand fetching
- Match search and filtering
- Match validation

### 4. Match Repository

- Database operations
- Query methods for filtering
- Existence checks

### 5. Match Controller

- REST endpoints
- Request/response DTOs
- Validation

---

## Sync Strategy Details

### What Gets Synced Automatically

1. **Upcoming matches** from configured leagues (next 14 days)
2. **Live matches** status updates
3. **Finished matches** score updates (recent 7 days)

### What Gets Synced On-Demand

1. Matches searched by tipsters
2. Matches selected for tip creation (if not in DB)
3. Matches from leagues not in auto-sync list

### Sync Process

1. Fetch matches from The Odds API
2. Check if match exists (by `external_id`)
3. If exists: Update (scores, status, etc.)
4. If not exists: Create new match
5. Ensure teams and leagues exist (create if needed)
6. Update `last_synced_at` timestamp

---

## Error Handling

### API Failures

- Log error and continue with existing data
- Retry with exponential backoff
- Alert if sync fails multiple times

### Data Validation

- Validate match data before saving
- Skip invalid matches (log warning)
- Ensure referential integrity (teams, leagues)

---

## Performance Considerations

### Database Indexes

- `matches.external_id` (unique)
- `matches.match_date`
- `matches.status`
- `matches.league_id`
- Composite: `(match_date, status)`

### Caching

- Cache popular matches (Redis optional)
- Cache league lists
- Cache team lookups

### Query Optimization

- Pagination for match lists
- Date range filtering
- Status filtering
- League filtering

---

## Cleanup Strategy

### Archive Old Matches

- Matches not referenced by tips
- Older than 90 days
- Status: finished
- Action: Soft delete (mark as archived) or hard delete

### Prevent Deletion

- Check `tip_selections` before deleting
- Never delete matches with `tip_selections` references
- Log warning if deletion prevented

---

## Future Enhancements

1. **User Preferences**: Let tipsters subscribe to specific leagues
2. **Smart Sync**: Sync leagues based on tipster activity
3. **Match Predictions**: Store AI predictions alongside matches
4. **Match Statistics**: Aggregate stats from The Odds API
5. **Real-time Updates**: WebSocket for live match updates

---

## Notes

- The Odds API rate limits: Check API documentation for limits
- API costs: Monitor usage to stay within plan limits
- Data freshness: Balance sync frequency with API costs
- Storage: Monitor database growth, implement cleanup as needed

---

**Last Updated**: 2024
**Maintained By**: Development Team
