BEGIN;

INSERT INTO foosball_table (id, code, team_a_color, team_b_color, activated) VALUES (1, 'F16', '#0000ff', '#d0d0d0', true);
INSERT INTO foosball_table (id, code, team_a_color, team_b_color, activated) VALUES (2, 'W15', '#0000ff', '#ff0000', true);
INSERT INTO foosball_table (id, code, team_a_color, team_b_color, activated) VALUES (3, 'ITB', '#ff0000', '#0000ff', true);
INSERT INTO foosball_table (id, code, team_a_color, team_b_color, activated) VALUES (4, 'DEV', '#ff0000', '#ffff00', true);

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (5, 'mkowalski', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Marek', 'Kowalski',
'mkowalski@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (5, 'ROLE_ADMIN');

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (6, 'pnowak', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Piotr', 'Nowak',
'pnowak@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (6, 'ROLE_USER');

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (7, 'tnowak', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Tomasz', 'Nowak',
'tnowak@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (7, 'ROLE_USER');

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (8, 'skowalski', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Sławomir', 'Kowalski',
'skowalski@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (8, 'ROLE_USER');

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (9, 'mgorgon', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Michał', 'Gorgon',
'mgorgon@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (9, 'ROLE_USER');

INSERT INTO player (id, login, password_hash, first_name, last_name, email, activated, created_by, created_date)
VALUES (10, 'gtestowy', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Gracz', 'Testowy',
'gtestowy@localhost', true, 'admin', now());

INSERT INTO player_authority(player_id, authority_name) VALUES (10, 'ROLE_USER');

INSERT INTO team(id, name, attacker_id, goalkeeper_id, created_time, closed_time) VALUES (1, 'Polska', 6, 5,
'2018-01-01', null);

INSERT INTO team(id, name, attacker_id, goalkeeper_id, created_time, closed_time) VALUES (2, 'Team XXX', 9, 5,
'2017-01-01', '2018-12-31');

INSERT INTO team(id, name, attacker_id, goalkeeper_id, created_time, closed_time) VALUES (3, 'Team YYY', 10, 5,
'2017-01-01', '2018-12-31');

INSERT INTO match(id, start_time, end_time, team_a_score, team_b_score, player_a_attacker_score,
player_b_attacker_score, player_a_goalkeeper_score, player_b_goalkeeper_score, foosball_table_id, team_a_id, team_b_id,
player_a_attacker_id, player_b_attacker_id, player_a_goalkeeper_id, player_b_goalkeeper_id)
VALUES (1, '2018-11-12 08:45', '2018-11-12 09:00', 10, 3, 5, 1, 5, 2, 1, 1, null, 6, 7, 5, 8);

INSERT INTO match(id, start_time, end_time, team_a_score, team_b_score, player_a_attacker_score,
player_b_attacker_score, player_a_goalkeeper_score, player_b_goalkeeper_score, foosball_table_id, team_a_id, team_b_id,
player_a_attacker_id, player_b_attacker_id, player_a_goalkeeper_id, player_b_goalkeeper_id)
VALUES (2, '2018-11-13 08:45', '2018-11-13 09:00', 10, 4, 5, 2, 5, 2, 1, 1, null, 6, 7, 5, 8);

INSERT INTO match(id, start_time, end_time, team_a_score, team_b_score, player_a_attacker_score,
player_b_attacker_score, player_a_goalkeeper_score, player_b_goalkeeper_score, foosball_table_id, team_a_id, team_b_id,
player_a_attacker_id, player_b_attacker_id, player_a_goalkeeper_id, player_b_goalkeeper_id)
VALUES (3, '2018-11-14 08:45', '2018-11-14 09:00', 10, 1, 5, 1, 5, 0, 1, 1, null, 6, 7, 5, 9);

INSERT INTO match(id, start_time, end_time, team_a_score, team_b_score, player_a_attacker_score,
player_b_attacker_score, player_a_goalkeeper_score, player_b_goalkeeper_score, foosball_table_id, team_a_id, team_b_id,
player_a_attacker_id, player_b_attacker_id, player_a_goalkeeper_id, player_b_goalkeeper_id)
VALUES (4, '2018-11-15 08:45', '2018-11-15 09:00', 8, 10, 5, 3, 3, 7, 2, 1, null, 6, 7, 5, 9);


COMMIT;
