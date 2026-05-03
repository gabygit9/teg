-- Desactivar chequeo de claves foráneas para limpiar e insertar tranquilo
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar tablas si ya existen para evitar errores de duplicados al reiniciar
TRUNCATE TABLE rols;
TRUNCATE TABLE colors;
TRUNCATE TABLE states_game;
TRUNCATE TABLE Secrets_objectives;
TRUNCATE TABLE Communication_types;
TRUNCATE TABLE Levels_bot;

-- Insertar Roles (Vital para tu error actual)
INSERT INTO rols (role_id, description) VALUES (1, 'admin');
INSERT INTO rols (role_id, description) VALUES (2, 'player');

-- Insertar Colores
INSERT INTO colors (color_id, name_color) VALUES
                                              (1, 'red'), (2, 'blue'), (3, 'green'), (4, 'yellow'), (5, 'black'), (6, 'magenta');

-- Insertar Estados de Juego
INSERT INTO states_game (state_id, description) VALUES
                                                    (1, 'PREPARATION'),
                                                    (2, 'FIRST_ROUND'),
                                                    (3, 'SECOND_ROUND'),
                                                    (4, 'HOSTILITIES'),
                                                    (5, 'FINISHED'),
                                                    (6, 'PAUSED'),
                                                    (7, 'IN_COURSE'),
                                                    (8, 'CANCELED');

-- Insertar objetivos (16 objetivos secretos)
INSERT INTO Secrets_objectives (objective_id, description) VALUES
(1, 'Ocupar África, 5 países de América del Norte y 4 países de Europa.'),
(2, 'Ocupar América del Sur, 7 países de Europa y 3 países limítrofes entre sí en cualquier lugar del mapa.'),
(3, 'Ocupar Asia y 2 países de América del Sur.'),
(4, 'Ocupar Europa, 4 países de Asia y 2 países de América del Sur.'),
(5, 'Ocupar América del Norte, 2 países de Oceanía y 4 de Asia.'),
(6, 'Ocupar 2 países de Oceanía, 2 países de África, 2 países de América del Sur, 3 países de Europa, 4 de América del Norte y 3 de Asia.'),
(7, 'Ocupar Oceanía, América del Norte y 2 países de Europa.'),
(8, 'Ocupar América del Sur, África y 4 países de Asia.'),
(9, 'Ocupar Oceanía, África y 5 países de América del Norte.'),
(10, 'Destruir el ejército azul, de ser imposible al jugador de la derecha.'),
(11, 'Destruir al ejército rojo, de ser imposible al jugador de la derecha.'),
(12, 'Destruir al ejército negro, de ser imposible al jugador de la derecha.'),
(13, 'Destruir al ejército amarillo, de ser imposible al jugador de la derecha.'),
(14, 'Destruir al ejército verde, de ser imposible al jugador de la derecha.'),
(15, 'Destruir al ejército magenta, de ser imposible al jugador de la derecha.'),
(16, 'OCUPAR 30 PAÍSES');

-- Insertar comunicación por defecto
INSERT INTO Communication_types (communication_id, description) VALUES (1, 'CHAT');

-- Insertar niveles de bot por defecto
INSERT INTO Levels_bot (level_id, name_bot) VALUES (1, 'novice'), (2, 'balanced'), (3, 'expert');

SET FOREIGN_KEY_CHECKS = 1;