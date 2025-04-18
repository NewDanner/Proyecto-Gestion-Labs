-- Eliminar la base de datos existente si es necesario
DROP DATABASE IF EXISTS GESTOR;

-- Crear la base de datos nuevamente
CREATE DATABASE GESTOR;
USE GESTOR;

-- Tabla Usuarios
CREATE TABLE Usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    ci INT NOT NULL,
    rol ENUM('admin', 'coordinador') NOT NULL,
    estado BOOLEAN DEFAULT TRUE
) AUTO_INCREMENT = 1;

-- Tabla Telefono
CREATE TABLE Telefono (
    ci INT PRIMARY KEY,
    Telefono INT NOT NULL
);

-- Tabla Registro_Empleado con reinicio de auto_increment
CREATE TABLE Registro_Empleado (
    Id_Empleado INT AUTO_INCREMENT PRIMARY KEY,
    CI INT NOT NULL,
    nombre VARCHAR(20) NOT NULL,
    segundo_nombre VARCHAR(20),
    primer_apellido VARCHAR(20) NOT NULL,
    segundo_apellido VARCHAR(20),
    profesion VARCHAR(40) NOT NULL,
    fecha_de_incorporacion TIMESTAMP NOT NULL,
    turno VARCHAR(40),
    FOREIGN KEY (CI) REFERENCES Telefono(ci)
) AUTO_INCREMENT = 1;

-- Tabla Laboratorios con reinicio de auto_increment (sin materia)
CREATE TABLE Laboratorios (
    Id_Laboratorio INT AUTO_INCREMENT PRIMARY KEY,
    nombre ENUM('Laboratorio 1', 'Laboratorio 2', 'Laboratorio 3', 
               'Laboratorio 4', 'Laboratorio 5', 'Laboratorio 6', 
               'Laboratorio 7') NOT NULL,
    capacidad INT NOT NULL DEFAULT 30,
    descripcion TEXT,
    estado ENUM('disponible', 'en_mantenimiento', 'inactivo') NOT NULL
) AUTO_INCREMENT = 1;

-- Tabla Equipos con reinicio de auto_increment
CREATE TABLE Equipos (
    Id_Equipo INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(30) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    numero_de_serie VARCHAR(50) NOT NULL UNIQUE,
    procesador VARCHAR(40) NOT NULL,
    ram VARCHAR(30) NOT NULL,
    almacenamiento VARCHAR(30) NOT NULL,
    sistema_operativo VARCHAR(30) NOT NULL,
    fecha_de_instalacion TIMESTAMP NOT NULL,
    estado ENUM('disponible', 'en_mantenimiento', 'inactivo') NOT NULL,
    id_laboratorio INT,
    FOREIGN KEY (id_laboratorio) REFERENCES Laboratorios(Id_Laboratorio)
) AUTO_INCREMENT = 1;

-- Tabla Observaciones con reinicio de auto_increment
CREATE TABLE Observaciones (
    Id_Observacion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    detalle VARCHAR(200) NOT NULL,
    realizado VARCHAR(35) NOT NULL,
    id_equipo INT,
    id_laboratorio INT,
    FOREIGN KEY (id_equipo) REFERENCES Equipos(Id_Equipo)
) AUTO_INCREMENT = 1;

-- Tabla Prestamo con reinicio de auto_increment (agregado materia)
CREATE TABLE Prestamo (
    Id_Prestamo INT AUTO_INCREMENT PRIMARY KEY,
    Nro_Laboratorio INT NOT NULL,
    tipo_de_prestamo ENUM('clase', 'mantenimiento') NOT NULL,
    materia ENUM('electronica', 'hardware', 'redes_telecomunicaciones') NOT NULL,
    fecha_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado ENUM('pendiente', 'confirmado', 'cancelado', 'completado') NOT NULL DEFAULT 'pendiente',
    id_usuario INT NOT NULL,
    FOREIGN KEY (Nro_Laboratorio) REFERENCES Laboratorios(Id_Laboratorio),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
) AUTO_INCREMENT = 1;

-- Tabla Registro_cliente con reinicio de auto_increment
CREATE TABLE Registro_cliente (
    Id_Cliente INT AUTO_INCREMENT PRIMARY KEY,
    CI INT NOT NULL,
    nombre VARCHAR(20) NOT NULL,
    segundo_nombre VARCHAR(20),
    primer_apellido VARCHAR(20) NOT NULL,
    segundo_apellido VARCHAR(20),
    cantidad_de_participantes INT NOT NULL,
    materia VARCHAR(40) NOT NULL,
    gmail VARCHAR(45) NOT NULL,
    Nro_Prestamo INT NOT NULL,
    Encargado INT NOT NULL, 
    FOREIGN KEY (Nro_Prestamo) REFERENCES Prestamo(Id_Prestamo),
    FOREIGN KEY (CI) REFERENCES Telefono(ci),
    FOREIGN KEY (Encargado) REFERENCES Registro_Empleado(Id_Empleado)
) AUTO_INCREMENT = 1;

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
