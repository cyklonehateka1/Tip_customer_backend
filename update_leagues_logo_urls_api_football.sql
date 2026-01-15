-- Update logo URLs using api-football.com logo service
-- This service provides working logo URLs for football leagues
-- Base URL pattern: https://media.api-sports.io/football/leagues/{league_id}.png
-- League IDs mapped from external_id

BEGIN;

-- Major European Leagues - Using api-football.com logo service
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/39.png' WHERE external_id = 'soccer_epl';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/40.png' WHERE external_id = 'soccer_efl_champ';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/45.png' WHERE external_id = 'soccer_england_efl_cup';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/46.png' WHERE external_id = 'soccer_england_league1';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/47.png' WHERE external_id = 'soccer_england_league2';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/48.png' WHERE external_id = 'soccer_fa_cup';

UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/140.png' WHERE external_id = 'soccer_spain_la_liga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/141.png' WHERE external_id = 'soccer_spain_segunda_division';

UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/135.png' WHERE external_id = 'soccer_italy_serie_a';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/136.png' WHERE external_id = 'soccer_italy_serie_b';

UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/78.png' WHERE external_id = 'soccer_germany_bundesliga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/79.png' WHERE external_id = 'soccer_germany_bundesliga2';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/80.png' WHERE external_id = 'soccer_germany_liga3';

UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/61.png' WHERE external_id = 'soccer_france_ligue_one';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/62.png' WHERE external_id = 'soccer_france_ligue_two';

-- Other European Leagues
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/88.png' WHERE external_id = 'soccer_netherlands_eredivisie';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/94.png' WHERE external_id = 'soccer_portugal_primeira_liga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/203.png' WHERE external_id = 'soccer_turkey_super_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/197.png' WHERE external_id = 'soccer_greece_super_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/179.png' WHERE external_id = 'soccer_spl';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/207.png' WHERE external_id = 'soccer_switzerland_superleague';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/218.png' WHERE external_id = 'soccer_austria_bundesliga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/119.png' WHERE external_id = 'soccer_denmark_superliga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/144.png' WHERE external_id = 'soccer_belgium_first_div';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/103.png' WHERE external_id = 'soccer_league_of_ireland';

-- Americas
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/71.png' WHERE external_id = 'soccer_brazil_campeonato';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/72.png' WHERE external_id = 'soccer_brazil_serie_b';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/128.png' WHERE external_id = 'soccer_argentina_primera_division';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/262.png' WHERE external_id = 'soccer_mexico_ligamx';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/265.png' WHERE external_id = 'soccer_chile_campeonato';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/253.png' WHERE external_id = 'soccer_usa_mls';

-- Asia & Oceania
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_australia_aleague';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/98.png' WHERE external_id = 'soccer_japan_j_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/292.png' WHERE external_id = 'soccer_korea_kleague1';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/169.png' WHERE external_id = 'soccer_china_superleague';

-- Other European
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/106.png' WHERE external_id = 'soccer_poland_ekstraklasa';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/103.png' WHERE external_id = 'soccer_norway_eliteserien';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/113.png' WHERE external_id = 'soccer_sweden_allsvenskan';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/114.png' WHERE external_id = 'soccer_sweden_superettan';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/116.png' WHERE external_id = 'soccer_finland_veikkausliiga';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/235.png' WHERE external_id = 'soccer_russia_premier_league';

-- International Tournaments
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_africa_cup_of_nations';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/5.png' WHERE external_id = 'soccer_conmebol_copa_libertadores';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/6.png' WHERE external_id = 'soccer_conmebol_copa_sudamericana';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/2.png' WHERE external_id = 'soccer_uefa_champs_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/3.png' WHERE external_id = 'soccer_uefa_europa_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/848.png' WHERE external_id = 'soccer_uefa_europa_conference_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_fifa_world_cup';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_fifa_world_cup_winner';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_fifa_world_cup_qualifiers_europe';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_fifa_world_cup_qualifiers_south_america';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/4.png' WHERE external_id = 'soccer_uefa_european_championship';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/4.png' WHERE external_id = 'soccer_uefa_euro_qualification';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/9.png' WHERE external_id = 'soccer_conmebol_copa_america';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/12.png' WHERE external_id = 'soccer_concacaf_gold_cup';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/673.png' WHERE external_id = 'soccer_concacaf_leagues_cup';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/13.png' WHERE external_id = 'soccer_fifa_club_world_cup';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/5.png' WHERE external_id = 'soccer_uefa_nations_league';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/2.png' WHERE external_id = 'soccer_uefa_champs_league_women';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/2.png' WHERE external_id = 'soccer_uefa_champs_league_qualification';
UPDATE leagues SET logo_url = 'https://media.api-sports.io/football/leagues/1.png' WHERE external_id = 'soccer_fifa_world_cup_womens';

COMMIT;
