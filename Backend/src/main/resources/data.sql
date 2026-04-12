-- Desactivar chequeo de claves foráneas para limpiar e insertar tranquilo
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar tablas si ya existen para evitar errores de duplicados al reiniciar
TRUNCATE TABLE rols;
TRUNCATE TABLE colors;
TRUNCATE TABLE states_game;

-- Insertar Roles (Vital para tu error actual)
INSERT INTO rols (role_id, description) VALUES (1, 'admin');
INSERT INTO rols (role_id, description) VALUES (2, 'player');

-- Insertar Colores
INSERT INTO colors (color_id, name_color) VALUES
                                              (1, 'red'), (2, 'blue'), (3, 'green'), (4, 'yellow'), (5, 'black'), (6, 'magenta');

-- Insertar Estados de Juego
INSERT INTO states_game (state_id, description) VALUES
                                                    (1, 'paused'), (2, 'in_course'), (3, 'canceled'), (4, 'finished'), (5, 'preparation');

SET FOREIGN_KEY_CHECKS = 1;