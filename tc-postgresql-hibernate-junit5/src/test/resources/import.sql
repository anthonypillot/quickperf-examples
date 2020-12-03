CREATE UNIQUE INDEX index_team_id ON team (id);
CREATE INDEX index_player_team ON player (team_id);