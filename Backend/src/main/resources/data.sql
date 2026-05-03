-- Desactivar chequeo de claves foráneas para limpiar e insertar tranquilo
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar tablas si ya existen para evitar errores de duplicados al reiniciar
TRUNCATE TABLE rols;
TRUNCATE TABLE colors;
TRUNCATE TABLE states_game;
TRUNCATE TABLE Secrets_objectives;

-- Insertar Roles (Vital para tu error actual)
INSERT INTO rols (role_id, description) VALUES (1, 'admin');
INSERT INTO rols (role_id, description) VALUES (2, 'player');

-- Insertar Colores
INSERT INTO colors (color_id, name_color) VALUES
                                              (1, 'red'), (2, 'blue'), (3, 'green'), (4, 'yellow'), (5, 'black'), (6, 'magenta');

-- Insertar Estados de Juego
INSERT INTO states_game (state_id, description) VALUES
                                                    (1, 'paused'), (2, 'in_course'), (3, 'canceled'), (4, 'finished'), (5, 'preparation');

-- Insertar objetivos (incluye el objetivo por defecto id=16 usado por el servicio)
INSERT INTO Secrets_objectives (objective_id, description) VALUES (16, 'Conquistar el mundo (objetivo por defecto)');

SET FOREIGN_KEY_CHECKS = 1;