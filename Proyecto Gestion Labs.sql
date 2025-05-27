-- Eliminar la base de datos existente si es necesario
DROP DATABASE IF EXISTS GESTOR;

-- Crear la base de datos nuevamente
CREATE DATABASE GESTOR;
USE GESTOR;

-- Tabla Usuarios 
CREATE TABLE Usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL,
    segundo_nombre VARCHAR(20),
    primer_apellido VARCHAR(20) NOT NULL,
    segundo_apellido VARCHAR(20),
    turno ENUM('Mañana', 'Tarde', 'Noche') NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    ci INT NOT NULL UNIQUE,
    rol ENUM('Administrador(a)', 'Docente', 'Técnico(a) de Mantenimiento') NOT NULL,
    sexo ENUM('Hombre', 'Mujer') NOT NULL,
    estado BOOLEAN DEFAULT TRUE
) AUTO_INCREMENT = 1;

-- Tabla Telefonos 
CREATE TABLE Telefono (
    id_telefono INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    numero_telefono VARCHAR(15) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla Laboratorios
CREATE TABLE Laboratorios (
    Id_Laboratorio INT AUTO_INCREMENT PRIMARY KEY,
    nombre ENUM('Laboratorio 1', 'Laboratorio 2', 'Laboratorio 3', 
               'Laboratorio 4', 'Laboratorio 5', 'Laboratorio 6', 
               'Laboratorio 7') NOT NULL,
    capacidad INT NOT NULL DEFAULT 30,
    descripcion TEXT,
    estado ENUM('Disponible', 'En Mantenimiento', 'Fuera de Servicio') DEFAULT 'Disponible' NOT NULL
) AUTO_INCREMENT = 1;

-- Tabla Equipos
CREATE TABLE Equipos (
    Id_Equipo INT AUTO_INCREMENT PRIMARY KEY,
    Marca VARCHAR(50),
    Modelo VARCHAR(50),
    numero_de_serie VARCHAR(50) NULL,
    Procesador VARCHAR(100),
    RAM VARCHAR(50),
    Almacenamiento VARCHAR(50),
    SO VARCHAR(50),
    Estado ENUM('Operativo', 'Mantenimiento', 'Dañado') DEFAULT 'Operativo',
    Id_Laboratorio INT,
    FOREIGN KEY (Id_Laboratorio) REFERENCES Laboratorios(Id_Laboratorio)
) AUTO_INCREMENT = 1;

-- Tabla Material_Adicional
CREATE TABLE Material_Adicional (
    N_Objeto INT AUTO_INCREMENT PRIMARY KEY,
    nombre_objeto VARCHAR(40) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    cantidad_minima INT DEFAULT 1,
    extravio BOOLEAN NOT NULL DEFAULT FALSE,
    daño BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(200),
    Id_Laboratorio INT,
    fecha_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reportado_por INT,
    proveedor VARCHAR(50) NULL,
    fecha_compra DATE NULL,
    costo DECIMAL(10,2) NULL,
    FOREIGN KEY (Id_Laboratorio) REFERENCES Laboratorios(Id_Laboratorio),
    FOREIGN KEY (reportado_por) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla Reservas
CREATE TABLE Reservas (
    Id_Reserva INT AUTO_INCREMENT PRIMARY KEY,
    Nro_Laboratorio INT NOT NULL,
    tipo_de_prestamo ENUM('Clase', 'Mantenimiento') NOT NULL,
    materia VARCHAR(30) NOT NULL CHECK (materia IN ('Electrónica', 'Hardware', 'Redes y Telecomunicaciones')),
    fecha_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado ENUM('Pendiente', 'Confirmado', 'Cancelado', 'Completado') NOT NULL DEFAULT 'Pendiente',
    id_usuario INT NOT NULL,
    usa_material_adicional BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (Nro_Laboratorio) REFERENCES Laboratorios(Id_Laboratorio),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    CONSTRAINT chk_horarios CHECK (hora_fin > hora_inicio)
) AUTO_INCREMENT = 1;

-- Tabla Reserva_Material_Adicional
CREATE TABLE Reserva_Material_Adicional (
    Id_Reserva INT NOT NULL,
    N_Objeto INT NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (Id_Reserva, N_Objeto),
    FOREIGN KEY (Id_Reserva) REFERENCES Reservas(Id_Reserva),
    FOREIGN KEY (N_Objeto) REFERENCES Material_Adicional(N_Objeto),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0)
);

-- Tabla Registro_Cliente
CREATE TABLE Registro_Cliente (
    Id_Registro INT AUTO_INCREMENT PRIMARY KEY,
    Nro_Reserva INT NOT NULL,
    ci_cliente INT NOT NULL,
    nombre_cliente VARCHAR(50) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    FOREIGN KEY (Nro_Reserva) REFERENCES Reservas(Id_Reserva)
) AUTO_INCREMENT = 1;

-- Tabla Mantenimiento
CREATE TABLE Mantenimiento (
    Id_Mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    tipo ENUM('Preventivo', 'Correctivo', 'Predictivo', 'Actualización') NOT NULL,
    tipo_elemento ENUM('Equipo', 'Material') NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NULL,
    descripcion VARCHAR(200) NOT NULL,
    procedimiento TEXT NULL,
    repuestos_utilizados VARCHAR(200) NULL,
    costo DECIMAL(10,2) NULL,
    Id_Usuario_Responsable INT NOT NULL,
    estado ENUM('Pendiente', 'En Proceso', 'Completado', 'Cancelado') DEFAULT 'Pendiente',
    FOREIGN KEY (Id_Usuario_Responsable) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla Mantenimiento_Equipo
CREATE TABLE Mantenimiento_Equipo (
    Id_Mantenimiento INT NOT NULL,
    Id_Equipo INT NOT NULL,
    PRIMARY KEY (Id_Mantenimiento, Id_Equipo),
    FOREIGN KEY (Id_Mantenimiento) REFERENCES Mantenimiento(Id_Mantenimiento) ON DELETE CASCADE,
    FOREIGN KEY (Id_Equipo) REFERENCES Equipos(Id_Equipo)
);

-- Tabla Mantenimiento_Material
CREATE TABLE Mantenimiento_Material (
    Id_Mantenimiento INT NOT NULL,
    N_Objeto INT NOT NULL,
    PRIMARY KEY (Id_Mantenimiento, N_Objeto),
    FOREIGN KEY (Id_Mantenimiento) REFERENCES Mantenimiento(Id_Mantenimiento) ON DELETE CASCADE,
    FOREIGN KEY (N_Objeto) REFERENCES Material_Adicional(N_Objeto)
);

-- Tabla Gestion_Incidentes
CREATE TABLE Gestion_Incidentes (
    Id_Incidente INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    verificacion VARCHAR(20) NOT NULL,
    prioridad ENUM('Baja', 'Media', 'Alta', 'Crítica') NOT NULL,
    observaciones VARCHAR(200) NOT NULL,
    fecha_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_solucion TIMESTAMP NULL,
    Id_Usuario INT NOT NULL,
    Id_Mantenimiento INT NULL,
    Id_Equipo INT NULL,
    estado ENUM('Reportado', 'En Proceso', 'Resuelto', 'Cerrado') DEFAULT 'Reportado',
    evidencia VARCHAR(255) NULL,
    FOREIGN KEY (Id_Usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (Id_Mantenimiento) REFERENCES Mantenimiento(Id_Mantenimiento),
    FOREIGN KEY (Id_Equipo) REFERENCES Equipos(Id_Equipo)
) AUTO_INCREMENT = 1;

-- Tabla Reposicion_Dispositivos
CREATE TABLE Reposicion_Dispositivos (
    Id_Reposicion INT AUTO_INCREMENT PRIMARY KEY,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(200) NULL,
    aprobado_por INT NULL,
    fecha_aprobacion TIMESTAMP NULL,
    estado ENUM('Pendiente', 'Aprobado', 'Rechazado', 'Completado') DEFAULT 'Pendiente',
    motivo_rechazo VARCHAR(200) NULL,
    fecha_estimada_entrega DATE NULL,
    fecha_real_entrega TIMESTAMP NULL,
    costo_estimado DECIMAL(10,2) NULL,
    costo_real DECIMAL(10,2) NULL,
    observaciones VARCHAR(200) NULL,
    cantidad INT NOT NULL DEFAULT 1,
    tipo_elemento ENUM('Equipo', 'Material') NULL,
    id_elemento INT NULL,
    FOREIGN KEY (aprobado_por) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla Dispositivos_Baja
CREATE TABLE Dispositivos_Baja (
    Id_Baja INT AUTO_INCREMENT PRIMARY KEY,
    Id_Equipo INT NULL,
    N_Objeto INT NULL,
    fecha_baja TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo ENUM('Obsoleto', 'Daño Irreparable', 'Pérdida', 'Actualización', 'Otro') NOT NULL,
    descripcion_motivo VARCHAR(200) NULL,
    autorizado_por INT NOT NULL,
    evidencia VARCHAR(255) NULL,
    valor_contable DECIMAL(10,2) NULL,
    FOREIGN KEY (Id_Equipo) REFERENCES Equipos(Id_Equipo),
    FOREIGN KEY (N_Objeto) REFERENCES Material_Adicional(N_Objeto),
    FOREIGN KEY (autorizado_por) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla de relación entre bajas y reposiciones
CREATE TABLE Relacion_Baja_Reposicion (
    Id_Baja INT NOT NULL,
    Id_Reposicion INT NOT NULL,
    PRIMARY KEY (Id_Baja, Id_Reposicion),
    FOREIGN KEY (Id_Baja) REFERENCES Dispositivos_Baja(Id_Baja),
    FOREIGN KEY (Id_Reposicion) REFERENCES Reposicion_Dispositivos(Id_Reposicion)
);

CREATE TABLE ConfiguracionReportes (
    id_config INT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(100) NOT NULL DEFAULT 'NOMBRE DE EMPRESA',
    direccion_empresa VARCHAR(200) NOT NULL DEFAULT 'DIRECCIÓN DE LA EMPRESA',
    logo LONGBLOB NULL,
    id_usuario INT NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);

-- Procedimiento almacenado para reiniciar IDs después de eliminar
DELIMITER //
CREATE PROCEDURE ReiniciarIDs(IN tabla_nombre VARCHAR(50))
BEGIN
    SET @sql = CONCAT('ALTER TABLE ', tabla_nombre, ' AUTO_INCREMENT = 1');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

USE GESTOR;
CREATE OR REPLACE VIEW Reporte_Laboratorios AS
SELECT 
    l.Id_Laboratorio,
    l.nombre AS Nombre_Laboratorio,
    l.capacidad,
    l.estado AS Estado_Laboratorio,
    r.fecha_reserva,
    r.hora_inicio,
    r.hora_fin,
    r.tipo_de_prestamo,
    u.nombre AS Nombre_Usuario,
    u.username AS Usuario_Reserva
FROM Laboratorios l
LEFT JOIN Reservas r ON l.Id_Laboratorio = r.Nro_Laboratorio
LEFT JOIN Usuarios u ON r.id_usuario = u.id_usuario;

CREATE OR REPLACE VIEW Reporte_Mantenimiento AS
SELECT 
    m.Id_Mantenimiento,
    m.nombre AS Nombre_Mantenimiento,
    m.tipo AS Tipo_Mantenimiento,
    m.fecha_inicio,
    m.fecha_fin,
    m.descripcion,
    u.nombre AS Responsable,
    u.username AS Usuario_Responsable
FROM Mantenimiento m
LEFT JOIN Usuarios u ON m.Id_Usuario_Responsable = u.id_usuario;

CREATE OR REPLACE VIEW Reporte_Dispositivos_Baja AS
SELECT 
    db.Id_Baja,
    db.Id_Equipo,
    db.N_Objeto,
    CASE 
        WHEN db.Id_Equipo IS NOT NULL THEN CONCAT(e.Marca, ' ', e.Modelo)
        WHEN db.N_Objeto IS NOT NULL THEN ma.nombre_objeto
        ELSE 'Desconocido'
    END AS Descripcion,
    db.fecha_baja,
    db.motivo,
    db.descripcion_motivo,
    db.valor_contable,
    u.nombre AS Autorizado_Por,
    u.rol AS Rol_Autorizador,
    CASE 
        WHEN db.Id_Equipo IS NOT NULL THEN 'Equipo'
        WHEN db.N_Objeto IS NOT NULL THEN 'Material'
        ELSE 'Desconocido'
    END AS Tipo_Elemento
FROM Dispositivos_Baja db
LEFT JOIN Equipos e ON db.Id_Equipo = e.Id_Equipo
LEFT JOIN Material_Adicional ma ON db.N_Objeto = ma.N_Objeto
LEFT JOIN Usuarios u ON db.autorizado_por = u.id_usuario;

-- VISTAS PARA GENERAR REPORTES.
USE GESTOR;
SELECT * FROM Reporte_Laboratorios;
SELECT * FROM Reporte_Mantenimiento;
SELECT * FROM Reporte_Dispositivos_Baja;

USE GESTOR;
-- Usuario Administrador
INSERT INTO Usuarios (nombre, segundo_nombre, primer_apellido, segundo_apellido, turno, username, password, ci, rol, sexo, estado)
VALUES ('Admin', NULL, 'Principal', NULL, 'Mañana', 'admin', 'admin', 11111111, 'Administrador(a)', 'Hombre', TRUE);

-- Usuario Docente
INSERT INTO Usuarios (nombre, segundo_nombre, primer_apellido, segundo_apellido, turno, username, password, ci, rol, sexo, estado)
VALUES ('Lucía', 'María', 'Pérez', 'González', 'Tarde', 'lperez', '12345', 22222222, 'Docente', 'Mujer', TRUE);
-- Usuario Docente
INSERT INTO Usuarios (nombre, primer_apellido, turno, username, password, ci, rol, sexo) VALUES
('Juan', 'Pérez', 'Mañana', 'juanperez', '123456', 10123456, 'Docente', 'Hombre'),
('María', 'González', 'Tarde', 'mariagonzalez', 'abcdef', 10123457, 'Administrador(a)', 'Mujer'),
('Carlos', 'Ramírez', 'Noche', 'carlosramirez', 'xyz987', 10123458, 'Técnico(a) de Mantenimiento', 'Hombre');

-- Usuario Técnico(a) de Mantenimiento
INSERT INTO Usuarios (nombre, segundo_nombre, primer_apellido, segundo_apellido, turno, username, password, ci, rol, sexo, estado)
VALUES ('Juan', 'Carlos', 'Rodríguez', 'Fernández', 'Noche', 'jrodriguez', '67890', 33333333, 'Técnico(a) de Mantenimiento', 'Hombre', TRUE);

use gestor;
INSERT INTO Laboratorios (nombre, capacidad, descripcion, estado) VALUES
('Laboratorio 1', 30, 'Laboratorio de electrónica avanzada', 'Disponible'),
('Laboratorio 2', 25, 'Laboratorio de computación con estaciones de trabajo', 'Disponible'),
('Laboratorio 3', 40, 'Laboratorio de redes y telecomunicaciones', 'Disponible'),
('Laboratorio 4', 35, 'Laboratorio de hardware y mantenimiento', 'En Mantenimiento'),
('Laboratorio 5', 30, 'Laboratorio de sistemas operativos y virtualización', 'Disponible'),
('Laboratorio 6', 28, 'Laboratorio de programación y desarrollo', 'Disponible'),
('Laboratorio 7', 30, 'Laboratorio de seguridad informática', 'Disponible');

-- Equipos para el Laboratorio 1
INSERT INTO Equipos (Marca, Modelo, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) VALUES
('HP', 'ProDesk 400', 'Intel Core i5-10400', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 1),
('Dell', 'OptiPlex 3080', 'Intel Core i5-10500', '8GB', '1TB HDD', 'Windows 10 Pro', 'Operativo', 1),
('Lenovo', 'ThinkCentre M70t', 'Intel Core i7-10700', '16GB', '256GB SSD', 'Windows 11 Pro', 'Operativo', 1);

-- Equipos para el Laboratorio 2
INSERT INTO Equipos (Marca, Modelo, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) VALUES
('HP', 'EliteDesk 800', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 2),
('Acer', 'Veriton M', 'Intel Core i3-10100', '8GB', '500GB HDD', 'Windows 10 Home', 'Operativo', 2),
('Dell', 'OptiPlex 5090', 'Intel Core i5-11500', '16GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 2);

-- Equipos para el Laboratorio 3
INSERT INTO Equipos (Marca, Modelo, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) VALUES
('Lenovo', 'ThinkCentre M80s', 'Intel Core i7-11700', '32GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 3),
('HP', 'ProDesk 600', 'Intel Core i5-1135G7', '16GB', '512GB SSD', 'Windows 10 Pro', 'Mantenimiento', 3),
('Dell', 'Vostro 3681', 'Intel Core i3-10105', '8GB', '256GB SSD', 'Windows 10 Pro', 'Operativo', 3);


INSERT INTO Material_Adicional (nombre_objeto, categoria, cantidad, cantidad_minima, Id_Laboratorio) VALUES
-- Electrónica (Laboratorio 1)
('Fuente de Alimentación Regulable', 'Electrónica', 5, 1, 1),
('Placa Arduino Uno', 'Electrónica', 10, 2, 1),

-- Computación (Laboratorio 2)
('Mouse y Teclado', 'Computación', 15, 5, 2),
('Pantallas LCD', 'Computación', 8, 2, 2),

-- Redes (Laboratorio 3)
('Cable UTP Cat 6', 'Redes', 50, 10, 3),
('Tarjetas de Red PCIe', 'Redes', 7, 2, 3);


