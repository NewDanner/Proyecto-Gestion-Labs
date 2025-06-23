-- Eliminar la base de datos existente si es necesario
DROP DATABASE IF EXISTS GESTORV2;

-- Crear la base de datos nuevamente
CREATE DATABASE GESTORV2;
USE GESTORV2;

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
    estado BOOLEAN DEFAULT TRUE,
    correo VARCHAR(255)
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

-- Crear la tabla Equipos sin el campo id_usuario
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


-- Tabla para registrar todas las transacciones importantes
CREATE TABLE Historial_Transacciones (
    id_transaccion INT AUTO_INCREMENT PRIMARY KEY,
    tabla_afectada VARCHAR(50) NOT NULL,
    id_registro_afectado INT NULL,
    tipo_operacion ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    datos_anteriores TEXT NULL,
    datos_nuevos TEXT NULL,
    fecha_transaccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT NOT NULL,
    descripcion VARCHAR(255) NULL,
    tipo_historial ENUM('USUARIO', 'EQUIPO', 'MATERIAL', 'RESERVA', 'MANTENIMIENTO', 'BAJA') NOT NULL,
    ip_conexion VARCHAR(45) NULL,
    dispositivo VARCHAR(100) NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
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

USE GESTORV2;

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
USE GESTORV2;
SELECT * FROM Reporte_Laboratorios;
SELECT * FROM Reporte_Mantenimiento;
SELECT * FROM Reporte_Dispositivos_Baja;

USE GESTORV2;
-- TRIGGERS
-- Trigger para INSERT en Usuarios
DELIMITER //
CREATE TRIGGER trg_usuarios_insert AFTER INSERT ON Usuarios
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Usuarios', NEW.id_usuario, 'INSERT',
        CONCAT('Nombre: ', NEW.nombre, ' | Username: ', NEW.username, 
               ' | Rol: ', NEW.rol, ' | Estado: ', NEW.estado),
        NEW.id_usuario, 'Nuevo usuario creado', 'USUARIO'
    );
END //
DELIMITER ;

-- Trigger para UPDATE en Usuarios
DELIMITER //
CREATE TRIGGER trg_usuarios_update AFTER UPDATE ON Usuarios
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Usuarios', NEW.id_usuario, 'UPDATE',
        CONCAT('Nombre: ', OLD.nombre, ' | Username: ', OLD.username, 
               ' | Rol: ', OLD.rol, ' | Estado: ', OLD.estado),
        CONCAT('Nombre: ', NEW.nombre, ' | Username: ', NEW.username, 
               ' | Rol: ', NEW.rol, ' | Estado: ', NEW.estado),
        NEW.id_usuario, 'Actualización de usuario', 'USUARIO'
    );
END //
DELIMITER ;

-- Trigger para DELETE en Usuarios
DELIMITER //
CREATE TRIGGER trg_usuarios_delete AFTER DELETE ON Usuarios
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Usuarios', OLD.id_usuario, 'DELETE',
        CONCAT('Nombre: ', OLD.nombre, ' | Username: ', OLD.username, 
               ' | Rol: ', OLD.rol, ' | Estado: ', OLD.estado),
        OLD.id_usuario, 'Usuario eliminado', 'USUARIO'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_equipos_insert AFTER INSERT ON Equipos
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Equipos', NEW.Id_Equipo, 'INSERT',
        CONCAT('Marca: ', NEW.Marca, ' | Modelo: ', NEW.Modelo, 
               ' | Estado: ', NEW.Estado, ' | Lab: ', NEW.Id_Laboratorio),
        1, -- Usuario por defecto (admin)
        'Nuevo equipo registrado', 'EQUIPO'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_equipos_update AFTER UPDATE ON Equipos
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Equipos', NEW.Id_Equipo, 'UPDATE',
        CONCAT('Marca: ', OLD.Marca, ' | Modelo: ', OLD.Modelo, 
               ' | Estado: ', OLD.Estado, ' | Lab: ', OLD.Id_Laboratorio),
        CONCAT('Marca: ', NEW.Marca, ' | Modelo: ', NEW.Modelo, 
               ' | Estado: ', NEW.Estado, ' | Lab: ', NEW.Id_Laboratorio),
        1, -- Usuario por defecto (admin)
        'Actualización de equipo', 'EQUIPO'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_equipos_delete AFTER DELETE ON Equipos
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Equipos', OLD.Id_Equipo, 'DELETE',
        CONCAT('Marca: ', OLD.Marca, ' | Modelo: ', OLD.Modelo, 
               ' | Estado: ', OLD.Estado, ' | Lab: ', OLD.Id_Laboratorio),
        1, -- Usuario por defecto (admin)
        'Equipo eliminado', 'EQUIPO'
    );
END //
DELIMITER ;

-- Triggers para Material_Adicional (INSERT, UPDATE, DELETE)
DELIMITER //
CREATE TRIGGER trg_material_insert AFTER INSERT ON Material_Adicional
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Material_Adicional', NEW.N_Objeto, 'INSERT',
        CONCAT('Nombre: ', NEW.nombre_objeto, ' | Cantidad: ', NEW.cantidad, 
               ' | Lab: ', NEW.Id_Laboratorio),
        NEW.reportado_por, 'Nuevo material registrado', 'MATERIAL'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_material_update AFTER UPDATE ON Material_Adicional
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Material_Adicional', NEW.N_Objeto, 'UPDATE',
        CONCAT('Nombre: ', OLD.nombre_objeto, ' | Cantidad: ', OLD.cantidad, 
               ' | Lab: ', OLD.Id_Laboratorio),
        CONCAT('Nombre: ', NEW.nombre_objeto, ' | Cantidad: ', NEW.cantidad, 
               ' | Lab: ', NEW.Id_Laboratorio),
        NEW.reportado_por, 'Actualización de material', 'MATERIAL'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_material_delete AFTER DELETE ON Material_Adicional
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Material_Adicional', OLD.N_Objeto, 'DELETE',
        CONCAT('Nombre: ', OLD.nombre_objeto, ' | Cantidad: ', OLD.cantidad, 
               ' | Lab: ', OLD.Id_Laboratorio),
        OLD.reportado_por, 'Material eliminado', 'MATERIAL'
    );
END //
DELIMITER ;

-- Triggers para Reservas (INSERT, UPDATE, DELETE)
DELIMITER //
CREATE TRIGGER trg_reservas_insert AFTER INSERT ON Reservas
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Reservas', NEW.Id_Reserva, 'INSERT',
        CONCAT('Lab: ', NEW.Nro_Laboratorio, ' | Tipo: ', NEW.tipo_de_prestamo, 
               ' | Fecha: ', NEW.fecha_reserva, ' | Estado: ', NEW.estado),
        NEW.id_usuario, 'Nueva reserva creada', 'RESERVA'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_reservas_update AFTER UPDATE ON Reservas
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Reservas', NEW.Id_Reserva, 'UPDATE',
        CONCAT('Lab: ', OLD.Nro_Laboratorio, ' | Tipo: ', OLD.tipo_de_prestamo, 
               ' | Fecha: ', OLD.fecha_reserva, ' | Estado: ', OLD.estado),
        CONCAT('Lab: ', NEW.Nro_Laboratorio, ' | Tipo: ', NEW.tipo_de_prestamo, 
               ' | Fecha: ', NEW.fecha_reserva, ' | Estado: ', NEW.estado),
        NEW.id_usuario, 'Actualización de reserva', 'RESERVA'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_reservas_delete AFTER DELETE ON Reservas
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Reservas', OLD.Id_Reserva, 'DELETE',
        CONCAT('Lab: ', OLD.Nro_Laboratorio, ' | Tipo: ', OLD.tipo_de_prestamo, 
               ' | Fecha: ', OLD.fecha_reserva, ' | Estado: ', OLD.estado),
        OLD.id_usuario, 'Reserva eliminada', 'RESERVA'
    );
END //
DELIMITER ;

-- Triggers para Mantenimiento (INSERT, UPDATE, DELETE)
DELIMITER //
CREATE TRIGGER trg_mantenimiento_insert AFTER INSERT ON Mantenimiento
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Mantenimiento', NEW.Id_Mantenimiento, 'INSERT',
        CONCAT('Tipo: ', NEW.tipo, ' | Estado: ', NEW.estado, 
               ' | F.Ini: ', NEW.fecha_inicio, ' | F.Fin: ', IFNULL(NEW.fecha_fin, 'NULL')),
        NEW.Id_Usuario_Responsable, 'Nuevo mantenimiento registrado', 'MANTENIMIENTO'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_mantenimiento_update AFTER UPDATE ON Mantenimiento
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Mantenimiento', NEW.Id_Mantenimiento, 'UPDATE',
        CONCAT('Tipo: ', OLD.tipo, ' | Estado: ', OLD.estado, 
               ' | F.Ini: ', OLD.fecha_inicio, ' | F.Fin: ', IFNULL(OLD.fecha_fin, 'NULL')),
        CONCAT('Tipo: ', NEW.tipo, ' | Estado: ', NEW.estado, 
               ' | F.Ini: ', NEW.fecha_inicio, ' | F.Fin: ', IFNULL(NEW.fecha_fin, 'NULL')),
        NEW.Id_Usuario_Responsable, 'Actualización de mantenimiento', 'MANTENIMIENTO'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_mantenimiento_delete AFTER DELETE ON Mantenimiento
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_anteriores, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Mantenimiento', OLD.Id_Mantenimiento, 'DELETE',
        CONCAT('Tipo: ', OLD.tipo, ' | Estado: ', OLD.estado, 
               ' | F.Ini: ', OLD.fecha_inicio, ' | F.Fin: ', IFNULL(OLD.fecha_fin, 'NULL')),
        OLD.Id_Usuario_Responsable, 'Mantenimiento eliminado', 'MANTENIMIENTO'
    );
END //
DELIMITER ;

-- Trigger para Dispositivos_Baja
DELIMITER //
CREATE TRIGGER trg_bajas_audit AFTER INSERT ON Dispositivos_Baja
FOR EACH ROW
BEGIN
    DECLARE descripcion_elemento VARCHAR(100);
    DECLARE id_usuario_reporta INT;
    
    IF NEW.Id_Equipo IS NOT NULL THEN
        SELECT CONCAT(Marca, ' ', Modelo), Id_Laboratorio INTO descripcion_elemento, id_usuario_reporta 
        FROM Equipos WHERE Id_Equipo = NEW.Id_Equipo;
    ELSE
        SELECT nombre_objeto, reportado_por INTO descripcion_elemento, id_usuario_reporta
        FROM Material_Adicional WHERE N_Objeto = NEW.N_Objeto;
    END IF;
    
    INSERT INTO Historial_Transacciones (
        tabla_afectada, id_registro_afectado, tipo_operacion,
        datos_nuevos, id_usuario, descripcion, tipo_historial
    ) VALUES (
        'Dispositivos_Baja', NEW.Id_Baja, 'INSERT',
        CONCAT('Elemento: ', descripcion_elemento, ' | Motivo: ', NEW.motivo),
        NEW.autorizado_por, 'Registro de baja de dispositivo', 'BAJA'
    );
END //
DELIMITER ;





USE GESTORV2;
-- Usuario Administrador
-- Solo para crear usuarios, para el inicio de sesion debe crearse de forma manual
INSERT INTO Usuarios (nombre, segundo_nombre, primer_apellido, segundo_apellido, turno, username, password, ci, rol, sexo, estado, correo)
VALUES ('Admin', NULL, 'Principal', NULL, 'Mañana', 'admin', 'admin', 11111111, 'Administrador(a)', 'Hombre', TRUE, 'admin@gmail.com');

use gestorv2;
INSERT INTO Laboratorios (nombre, capacidad, descripcion, estado) VALUES
('Laboratorio 1', 30, 'Laboratorio de electrónica avanzada', 'Disponible'),
('Laboratorio 2', 25, 'Laboratorio de computación con estaciones de trabajo', 'Disponible'),
('Laboratorio 3', 40, 'Laboratorio de redes y telecomunicaciones', 'Disponible'),
('Laboratorio 4', 35, 'Laboratorio de hardware y mantenimiento', 'En Mantenimiento'),
('Laboratorio 5', 30, 'Laboratorio de sistemas operativos y virtualización', 'Disponible'),
('Laboratorio 6', 28, 'Laboratorio de programación y desarrollo', 'Disponible'),
('Laboratorio 7', 30, 'Laboratorio de seguridad informática', 'Disponible');

USE GESTORV2;
-- Equipos para el Laboratorio 1 (Electrónica)
INSERT INTO Equipos (Marca, Modelo, numero_de_serie, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) 
VALUES 
    ('HP', 'ProDesk 400 G7', 'ABC123456', 'Intel Core i5-11400', '16GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Lenovo', 'ThinkCentre M80q', 'XYZ789012', 'Intel Core i7-11700', '32GB DDR4', '1TB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Dell', 'OptiPlex 7090', 'DEF456789', 'Intel Core i5-11500', '16GB DDR4', '1TB HDD', 'Windows 11 Pro', 'Operativo', 1),
    ('HP', 'EliteDesk 800 G8', 'GHI234567', 'Intel Core i7-11700', '32GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Lenovo', 'ThinkCentre M90q', 'JKL890123', 'Intel Core i5-11500', '16GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Dell', 'OptiPlex 7090', 'MNO345678', 'Intel Core i7-11700', '32GB DDR4', '1TB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('HP', 'ProDesk 400 G7', 'PQR567890', 'Intel Core i5-11400', '16GB DDR4', '1TB HDD', 'Windows 11 Pro', 'Operativo', 1),
    ('Lenovo', 'ThinkCentre M80q', 'STU123456', 'Intel Core i7-11700', '32GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Dell', 'OptiPlex 7090', 'VWX234567', 'Intel Core i5-11500', '16GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('HP', 'EliteDesk 800 G8', 'YZA890123', 'Intel Core i7-11700', '32GB DDR4', '1TB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Lenovo', 'ThinkCentre M80q', 'BCD345678', 'Intel Core i7-11700', '32GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Dell', 'OptiPlex 7090', 'CDE456789', 'Intel Core i5-11500', '16GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('HP', 'ProDesk 400 G7', 'EFG234567', 'Intel Core i5-11400', '16GB DDR4', '1TB HDD', 'Windows 11 Pro', 'Operativo', 1),
    ('Lenovo', 'ThinkCentre M90q', 'FGH345678', 'Intel Core i5-11500', '16GB DDR4', '512GB SSD', 'Windows 11 Pro', 'Operativo', 1),
    ('Dell', 'OptiPlex 7090', 'GHI456789', 'Intel Core i7-11700', '32GB DDR4', '1TB SSD', 'Windows 11 Pro', 'Operativo', 1);
-- Material Adicional para el Laboratorio 1 (Electrónica)
INSERT INTO Material_Adicional (nombre_objeto, categoria, cantidad, cantidad_minima, observaciones, Id_Laboratorio, reportado_por, fecha_compra, costo) 
VALUES 
    ('Multímetro Digital', 'Herramientas', 5, 3, 'Precisión ±0.5%', 1, 1, '2024-01-15', 150.00),
    ('Soldador de Punto', 'Herramientas', 8, 5, 'Temperatura ajustable 200-450°C', 1, 1, '2024-02-10', 75.00),
    ('Kit de Cables RJ45', 'Conectores', 20, 10, 'Cables CAT6 1m', 1, 1, '2024-03-05', 40.00),
    ('Protoboard 830 Puntos', 'Componentes', 15, 8, 'Tamaño estándar', 1, 1, '2024-01-20', 25.00),
    ('Fuente de Alimentación 12V', 'Componentes', 10, 5, '5A corriente', 1, 1, '2024-02-15', 60.00),
    ('Osciloscopio USB', 'Equipos de Medición', 3, 2, '2 canales, 100MHz', 1, 1, '2024-03-10', 200.00),
    ('Kit de Resistencias', 'Componentes', 25, 15, 'Valores E12', 1, 1, '2024-01-25', 30.00),
    ('Generador de Funciones', 'Equipos de Medición', 2, 1, '10MHz, 2 canales', 1, 1, '2024-02-20', 300.00),
    ('Multímetro Analógico', 'Herramientas', 8, 5, '20kΩ/V', 1, 1, '2024-03-15', 50.00),
    ('Caja de Componentes', 'Almacenamiento', 12, 8, '40 compartimentos', 1, 1, '2024-01-30', 45.00),
    ('Fuente de Alimentación Variable', 'Herramientas', 3, 2, '0-30V, 5A', 1, 1, '2024-02-25', 120.00),
    ('Pinzas de Precisión', 'Herramientas', 15, 10, 'Set de 5 pares', 1, 1, '2024-03-10', 80.00),
    ('Soldador de Vela', 'Herramientas', 5, 3, 'Temperatura ajustable', 1, 1, '2024-01-20', 45.00),
    ('Cable de Datos USB', 'Conectores', 30, 20, '2m, USB 3.0', 1, 1, '2024-02-15', 15.00),
    ('Termómetro Digital', 'Equipos de Medición', 4, 2, 'Precisión ±0.1°C', 1, 1, '2024-03-05', 90.00);

-- Equipos para el Laboratorio 2 (Computación)
INSERT INTO Equipos (Marca, Modelo, numero_de_serie, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) 
VALUES 
    ('HP', 'EliteDesk 800', 'IJK123456', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('Acer', 'Veriton M', 'LMN789012', 'Intel Core i3-10100', '8GB', '500GB HDD', 'Windows 10 Home', 'Operativo', 2),
    ('Dell', 'OptiPlex 5090', 'OPQ456789', 'Intel Core i5-11500', '16GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('Lenovo', 'ThinkCentre M70s', 'NPR234567', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('HP', 'ProDesk 600', 'MST123456', 'Intel Core i5-1135G7', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 2),
    ('Acer', 'Veriton X', 'RST789012', 'Intel Core i3-1115G4', '8GB', '256GB SSD', 'Windows 11 Home', 'Operativo', 2),
    ('Dell', 'Vostro 3681', 'TUV456789', 'Intel Core i3-10105', '8GB', '256GB SSD', 'Windows 10 Pro', 'Operativo', 2),
    ('Lenovo', 'ThinkCentre M80a', 'UVW234567', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('HP', 'ProDesk 400', 'WXY123456', 'Intel Core i5-10400', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 2),
    ('Acer', 'Veriton S', 'VWX789012', 'Intel Core i3-1005G1', '8GB', '512GB SSD', 'Windows 11 Home', 'Operativo', 2),
    ('Dell', 'OptiPlex 3080', 'XYZ456789', 'Intel Core i5-10500', '16GB', '1TB HDD', 'Windows 10 Pro', 'Operativo', 2),
    ('Lenovo', 'ThinkCentre M70t', 'ZAB234567', 'Intel Core i7-10700', '32GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('HP', 'EliteDesk 800', 'BCD123456', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 2),
    ('Acer', 'Veriton M', 'CDE789012', 'Intel Core i3-10100', '8GB', '500GB HDD', 'Windows 10 Home', 'Operativo', 2),
    ('Dell', 'OptiPlex 5090', 'EFG456789', 'Intel Core i5-11500', '16GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 2);

-- Material Adicional para el Laboratorio 2 (Computación)
INSERT INTO Material_Adicional (nombre_objeto, categoria, cantidad, cantidad_minima, observaciones, Id_Laboratorio, reportado_por, fecha_compra, costo) 
VALUES 
    ('Mouse y Teclado', 'Periféricos', 15, 5, 'Set completo inalámbrico', 2, 1, '2024-01-15', 60.00),
    ('Pantallas LCD', 'Monitores', 8, 2, '24" Full HD', 2, 1, '2024-02-10', 180.00),
    ('Webcams HD', 'Periféricos', 12, 4, '1080p con micrófono', 2, 1, '2024-03-05', 45.00),
    ('Altavoces USB', 'Periféricos', 15, 5, '2.0 con subwoofer', 2, 1, '2024-01-20', 35.00),
    ('Impresora Multifunción', 'Impresoras', 3, 1, 'Wi-Fi, escáner, fax', 2, 1, '2024-02-15', 250.00),
    ('Scanner de Documentos', 'Periféricos', 2, 1, 'Alimentador automático', 2, 1, '2024-03-10', 200.00),
    ('Cables HDMI', 'Conectores', 30, 20, '2m, 4K@60Hz', 2, 1, '2024-01-25', 15.00),
    ('Cables USB-C', 'Conectores', 25, 15, '3.2 Gen 2, 20Gbps', 2, 1, '2024-02-20', 20.00),
    ('Adaptadores USB-C', 'Accesorios', 20, 10, 'USB-C a USB-A', 2, 1, '2024-03-15', 10.00),
    ('Soportes para Monitor', 'Accesorios', 12, 6, 'Ajustable en altura', 2, 1, '2024-01-30', 40.00),
    ('Lámparas LED', 'Iluminación', 15, 10, 'Flexible, USB', 2, 1, '2024-02-25', 25.00),
    ('Cargadores USB', 'Accesorios', 30, 20, '6 puertos, 60W', 2, 1, '2024-03-10', 30.00),
    ('Etiquetadora', 'Accesorios', 5, 3, 'Autoalimentada', 2, 1, '2024-01-20', 50.00),
    ('Cable Red', 'Conectores', 40, 30, 'CAT6, 1m', 2, 1, '2024-02-15', 5.00),
    ('Switch USB', 'Accesorios', 10, 5, '4 puertos', 2, 1, '2024-03-05', 15.00);

-- Equipos para el Laboratorio 3 (Redes y Telecomunicaciones)
INSERT INTO Equipos (Marca, Modelo, numero_de_serie, Procesador, RAM, Almacenamiento, SO, Estado, Id_Laboratorio) 
VALUES 
    ('Lenovo', 'ThinkCentre M80s', 'PQR123456', 'Intel Core i7-11700', '32GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('HP', 'ProDesk 600', 'STU789012', 'Intel Core i5-1135G7', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Dell', 'Vostro 3681', 'VWX456789', 'Intel Core i3-10105', '8GB', '256GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Lenovo', 'ThinkCentre M80a', 'XYZ234567', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('HP', 'ProDesk 400', 'ZAB123456', 'Intel Core i5-10400', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Dell', 'OptiPlex 3080', 'BCD789012', 'Intel Core i5-10500', '16GB', '1TB HDD', 'Windows 10 Pro', 'Operativo', 3),
    ('Lenovo', 'ThinkCentre M70t', 'CDE456789', 'Intel Core i7-10700', '32GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('HP', 'EliteDesk 800', 'EFG234567', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('Dell', 'OptiPlex 5090', 'FGH456789', 'Intel Core i5-11500', '16GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('Lenovo', 'ThinkCentre M80s', 'GHI234567', 'Intel Core i7-11700', '32GB', '1TB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('HP', 'ProDesk 600', 'JKL123456', 'Intel Core i5-1135G7', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Dell', 'Vostro 3681', 'MNO789012', 'Intel Core i3-10105', '8GB', '256GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Lenovo', 'ThinkCentre M80a', 'PQR456789', 'Intel Core i5-11400', '16GB', '512GB SSD', 'Windows 11 Pro', 'Operativo', 3),
    ('HP', 'ProDesk 400', 'STU234567', 'Intel Core i5-10400', '16GB', '512GB SSD', 'Windows 10 Pro', 'Operativo', 3),
    ('Dell', 'OptiPlex 3080', 'UVW456789', 'Intel Core i5-10500', '16GB', '1TB HDD', 'Windows 10 Pro', 'Operativo', 3);

-- Material Adicional para el Laboratorio 3 (Redes y Telecomunicaciones)
INSERT INTO Material_Adicional (nombre_objeto, categoria, cantidad, cantidad_minima, observaciones, Id_Laboratorio, reportado_por, fecha_compra, costo) 
VALUES 
    ('Cable UTP Cat 6', 'Redes', 50, 10, 'Cables de 1m', 3, 1, '2024-01-15', 15.00),
    ('Tarjetas de Red PCIe', 'Redes', 7, 2, 'Gigabit Ethernet', 3, 1, '2024-02-10', 45.00),
    ('Router Gigabit', 'Equipos de Red', 3, 1, '8 puertos, Wi-Fi 6', 3, 1, '2024-03-05', 200.00),
    ('Switch Gigabit', 'Equipos de Red', 5, 2, '16 puertos, gestionable', 3, 1, '2024-01-20', 150.00),
    ('Antenas Wi-Fi', 'Redes', 8, 3, '2.4GHz/5GHz', 3, 1, '2024-02-15', 60.00),
    ('Cable de Fibra Óptica', 'Conectores', 20, 10, 'LC/LC, 1m', 3, 1, '2024-03-10', 30.00),
    ('Conectores RJ45', 'Conectores', 100, 50, 'Categoría 6', 3, 1, '2024-01-25', 5.00),
    ('Tester de Cable', 'Herramientas', 5, 2, 'Cable y red', 3, 1, '2024-02-20', 80.00),
    ('Sistema VoIP', 'Telecomunicaciones', 4, 2, 'Centralita IP', 3, 1, '2024-03-15', 250.00),
    ('Teléfonos IP', 'Telecomunicaciones', 12, 6, 'Video HD', 3, 1, '2024-01-30', 120.00),
    ('Adaptadores PoE', 'Redes', 15, 8, '60W, Gigabit', 3, 1, '2024-02-25', 40.00),
    ('Cámara de Seguridad IP', 'Seguridad', 8, 4, '4K, IR', 3, 1, '2024-03-10', 180.00),
    ('Servidor de VoIP', 'Equipos de Red', 2, 1, 'Virtualización', 3, 1, '2024-01-20', 400.00),
    ('Punto de Acceso Wi-Fi', 'Redes', 10, 5, 'Indoor, Wave 2', 3, 1, '2024-02-15', 100.00),
    ('Concentrador de Fibra', 'Conectores', 5, 2, '24 puertos', 3, 1, '2024-03-05', 120.00);
    
    
use gestorv2;
-- Insertar registros de mantenimiento para equipos
INSERT INTO Mantenimiento (nombre, tipo, tipo_elemento, fecha_inicio, descripcion, Id_Usuario_Responsable, estado) VALUES
('Revisión preventiva HP ProDesk', 'Preventivo', 'Equipo', '2024-01-10 09:00:00', 'Limpieza interna y actualización de drivers', 3, 'Completado'),
('Cambio de disco duro Dell', 'Correctivo', 'Equipo', '2024-02-15 14:30:00', 'Reemplazo de HDD por SSD de 1TB', 3, 'Completado'),
('Actualización de BIOS Lenovo', 'Actualización', 'Equipo', '2024-03-05 10:00:00', 'Actualización a última versión de BIOS', 3, 'En Proceso'),
('Reparación fuente de poder', 'Correctivo', 'Material', '2024-01-20 11:00:00', 'Reparación de fuente de alimentación variable', 3, 'Completado'),
('Calibración multímetros', 'Preventivo', 'Material', '2024-02-28 15:00:00', 'Calibración anual de equipos de medición', 3, 'Pendiente');

-- Relacionar mantenimientos con equipos (usando IDs que correspondan a tus equipos existentes)
INSERT INTO Mantenimiento_Equipo (Id_Mantenimiento, Id_Equipo) VALUES
(1, 1),  -- Mantenimiento 1 para Equipo 1 (HP ProDesk)
(2, 3),  -- Mantenimiento 2 para Equipo 3 (Dell OptiPlex)
(3, 2);  -- Mantenimiento 3 para Equipo 2 (Lenovo ThinkCentre)

-- Relacionar mantenimientos con materiales (usando IDs que correspondan a tus materiales existentes)
INSERT INTO Mantenimiento_Material (Id_Mantenimiento, N_Objeto) VALUES
(4, 1),  -- Mantenimiento 4 para Material 1 (Multímetro Digital)
(5, 6);  -- Mantenimiento 5 para Material 6 (Osciloscopio USB)

-- Registrar bajas de equipos
INSERT INTO Dispositivos_Baja (Id_Equipo, fecha_baja, motivo, descripcion_motivo, autorizado_por, valor_contable) VALUES
(5, '2024-01-15 10:00:00', 'Obsoleto', 'Equipo con más de 8 años de antigüedad, sin soporte técnico', 1, 250.00),
(8, '2024-02-20 11:30:00', 'Daño Irreparable', 'Placa madre quemada por subida de voltaje', 1, 500.00);

-- Registrar bajas de materiales
INSERT INTO Dispositivos_Baja (N_Objeto, fecha_baja, motivo, descripcion_motivo, autorizado_por, valor_contable) VALUES
(4, '2024-03-01 09:45:00', 'Pérdida', 'Protoboard no devuelto por estudiante', 1, 25.00),
(12, '2024-01-30 14:15:00', 'Daño Irreparable', 'Pinzas de precisión con puntas rotas', 1, 15.00),
(7, '2024-02-10 16:00:00', 'Actualización', 'Reemplazo por kit de resistencias más completo', 1, 30.00);

-- Solicitudes de reposición
INSERT INTO Reposicion_Dispositivos (fecha_solicitud, motivo, estado, cantidad, tipo_elemento, id_elemento) VALUES
('2024-01-16 09:00:00', 'Reposición por equipo obsoleto', 'Aprobado', 1, 'Equipo', 5),
('2024-02-21 10:00:00', 'Reposición por daño irreparable', 'Pendiente', 1, 'Equipo', 8),
('2024-03-02 11:00:00', 'Reposición por material perdido', 'Completado', 2, 'Material', 4),
('2024-01-31 14:30:00', 'Reposición por herramienta dañada', 'Aprobado', 5, 'Material', 12),
('2024-02-11 16:30:00', 'Actualización de componentes', 'Completado', 10, 'Material', 7);

-- Relacionar bajas con reposiciones
INSERT INTO Relacion_Baja_Reposicion (Id_Baja, Id_Reposicion) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

CREATE OR REPLACE PACKAGE gestion_laboratorios AS
    -- Procedimiento para realizar una reserva de laboratorio
    PROCEDURE realizar_reserva(
        p_id_laboratorio IN NUMBER,
        p_id_usuario IN NUMBER,
        p_tipo_prestamo IN VARCHAR2,
        p_materia IN VARCHAR2,
        p_fecha IN DATE,
        p_hora_inicio IN TIMESTAMP,
        p_hora_fin IN TIMESTAMP,
        p_usa_material IN NUMBER DEFAULT 0,
        p_resultado OUT VARCHAR2,
        p_id_reserva OUT NUMBER
    );
    
    -- Procedimiento para actualizar el estado de un laboratorio
    PROCEDURE actualizar_estado_laboratorio(
        p_id_laboratorio IN NUMBER,
        p_nuevo_estado IN VARCHAR2,
        p_resultado OUT VARCHAR2
    );
    
    -- Función para obtener el docente con más reservas en un período
    FUNCTION obtener_docente_mas_reservas(
        p_fecha_inicio IN DATE,
        p_fecha_fin IN DATE
    ) RETURN VARCHAR2;
    
    -- Función para verificar disponibilidad de laboratorio
    FUNCTION verificar_disponibilidad_laboratorio(
        p_id_laboratorio IN NUMBER,
        p_fecha IN DATE,
        p_hora_inicio IN TIMESTAMP,
        p_hora_fin IN TIMESTAMP
    ) RETURN BOOLEAN;
END gestion_laboratorios;
/

CREATE OR REPLACE PACKAGE BODY gestion_laboratorios AS
    -- Procedimiento para realizar una reserva de laboratorio
    PROCEDURE realizar_reserva(
        p_id_laboratorio IN NUMBER,
        p_id_usuario IN NUMBER,
        p_tipo_prestamo IN VARCHAR2,
        p_materia IN VARCHAR2,
        p_fecha IN DATE,
        p_hora_inicio IN TIMESTAMP,
        p_hora_fin IN TIMESTAMP,
        p_usa_material IN NUMBER DEFAULT 0,
        p_resultado OUT VARCHAR2,
        p_id_reserva OUT NUMBER
    ) IS
        v_lab_existe NUMBER;
        v_user_existe NUMBER;
        v_user_rol VARCHAR2(50);
        v_disponible BOOLEAN;
        v_hora_actual TIMESTAMP := SYSTIMESTAMP;
    BEGIN
        -- Verificar que el laboratorio existe
        SELECT COUNT(*) INTO v_lab_existe
        FROM Laboratorios
        WHERE Id_Laboratorio = p_id_laboratorio;
        
        IF v_lab_existe = 0 THEN
            p_resultado := 'Error: Laboratorio no encontrado';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Verificar que el usuario existe
        SELECT COUNT(*), MAX(rol) INTO v_user_existe, v_user_rol
        FROM Usuarios
        WHERE id_usuario = p_id_usuario;
        
        IF v_user_existe = 0 THEN
            p_resultado := 'Error: Usuario no encontrado';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Solo docentes pueden reservar para clases
        IF p_tipo_prestamo = 'Clase' AND v_user_rol != 'Docente' THEN
            p_resultado := 'Error: Solo los docentes pueden reservar para clases';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Validar tipo de préstamo
        IF p_tipo_prestamo NOT IN ('Clase', 'Mantenimiento') THEN
            p_resultado := 'Error: Tipo de préstamo no válido (Clase/Mantenimiento)';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Validar materia
        IF p_materia NOT IN ('Electrónica', 'Hardware', 'Redes y Telecomunicaciones') THEN
            p_resultado := 'Error: Materia no válida';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Validar fechas
        IF p_fecha < TRUNC(SYSDATE) THEN
            p_resultado := 'Error: No se pueden hacer reservas en fechas pasadas';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        IF p_hora_inicio >= p_hora_fin THEN
            p_resultado := 'Error: La hora de fin debe ser posterior a la hora de inicio';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Verificar disponibilidad
        v_disponible := verificar_disponibilidad_laboratorio(
            p_id_laboratorio => p_id_laboratorio,
            p_fecha => p_fecha,
            p_hora_inicio => p_hora_inicio,
            p_hora_fin => p_hora_fin
        );
        
        IF NOT v_disponible THEN
            p_resultado := 'Error: El laboratorio no está disponible en el horario solicitado';
            p_id_reserva := NULL;
            RETURN;
        END IF;
        
        -- Insertar la reserva
        INSERT INTO Reservas (
            Nro_Laboratorio, tipo_de_prestamo, materia, fecha_reserva,
            hora_inicio, hora_fin, estado, id_usuario, usa_material_adicional
        ) VALUES (
            p_id_laboratorio, p_tipo_prestamo, p_materia, p_fecha,
            p_hora_inicio, p_hora_fin, 'Confirmado', p_id_usuario, p_usa_material
        ) RETURNING Id_Reserva INTO p_id_reserva;
        
        COMMIT;
        p_resultado := 'Reserva realizada exitosamente. ID de reserva: ' || p_id_reserva;
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_resultado := 'Error al realizar reserva: ' || SQLERRM;
            p_id_reserva := NULL;
    END realizar_reserva;
    
    -- Procedimiento para actualizar el estado de un laboratorio
    PROCEDURE actualizar_estado_laboratorio(
        p_id_laboratorio IN NUMBER,
        p_nuevo_estado IN VARCHAR2,
        p_resultado OUT VARCHAR2
    ) IS
        v_laboratorio_existe NUMBER;
    BEGIN
        -- Verificar que el laboratorio existe
        SELECT COUNT(*) INTO v_laboratorio_existe
        FROM Laboratorios
        WHERE Id_Laboratorio = p_id_laboratorio;
        
        IF v_laboratorio_existe = 0 THEN
            p_resultado := 'Error: El laboratorio con ID ' || p_id_laboratorio || ' no existe';
            RETURN;
        END IF;
        
        -- Verificar que el nuevo estado es válido
        IF p_nuevo_estado NOT IN ('Disponible', 'En Mantenimiento', 'Fuera de Servicio') THEN
            p_resultado := 'Error: Estado no válido. Los estados permitidos son: Disponible, En Mantenimiento, Fuera de Servicio';
            RETURN;
        END IF;
        
        -- Actualizar el estado del laboratorio
        UPDATE Laboratorios
        SET estado = p_nuevo_estado
        WHERE Id_Laboratorio = p_id_laboratorio;
        
        COMMIT;
        p_resultado := 'Estado del laboratorio ' || p_id_laboratorio || 
                      ' actualizado a: ' || p_nuevo_estado;
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_resultado := 'Error al actualizar estado del laboratorio: ' || SQLERRM;
    END actualizar_estado_laboratorio;
    
    -- Función para obtener el docente con más reservas en un período
    FUNCTION obtener_docente_mas_reservas(
        p_fecha_inicio IN DATE,
        p_fecha_fin IN DATE
    ) RETURN VARCHAR2 IS
        v_nombre_docente VARCHAR2(100);
        v_apellido_docente VARCHAR2(100);
        v_cantidad_reservas NUMBER;
        v_resultado VARCHAR2(200);
    BEGIN
        -- Obtener el docente con más reservas en el período
        SELECT u.nombre, u.primer_apellido, COUNT(*) AS num_reservas
        INTO v_nombre_docente, v_apellido_docente, v_cantidad_reservas
        FROM Reservas r
        JOIN Usuarios u ON r.id_usuario = u.id_usuario
        WHERE u.rol = 'Docente'
        AND r.fecha_reserva BETWEEN p_fecha_inicio AND p_fecha_fin
        AND r.estado IN ('Confirmado', 'Completado')
        GROUP BY u.nombre, u.primer_apellido
        ORDER BY num_reservas DESC
        FETCH FIRST 1 ROW ONLY;
        
        v_resultado := 'Docente con más reservas: ' || v_nombre_docente || ' ' || 
                      v_apellido_docente || ' con ' || v_cantidad_reservas || 
                      ' reservas entre ' || TO_CHAR(p_fecha_inicio, 'DD/MM/YYYY') || 
                      ' y ' || TO_CHAR(p_fecha_fin, 'DD/MM/YYYY');
        
        RETURN v_resultado;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 'No se encontraron reservas de docentes en el período especificado';
        WHEN OTHERS THEN
            RETURN 'Error al obtener docente con más reservas: ' || SQLERRM;
    END obtener_docente_mas_reservas;
    
    -- Función para verificar disponibilidad de laboratorio
    FUNCTION verificar_disponibilidad_laboratorio(
        p_id_laboratorio IN NUMBER,
        p_fecha IN DATE,
        p_hora_inicio IN TIMESTAMP,
        p_hora_fin IN TIMESTAMP
    ) RETURN BOOLEAN IS
        v_estado_lab VARCHAR2(20);
        v_reservas_count NUMBER;
        v_disponible BOOLEAN := TRUE;
    BEGIN
        -- Verificar estado del laboratorio
        SELECT estado INTO v_estado_lab
        FROM Laboratorios
        WHERE Id_Laboratorio = p_id_laboratorio;
        
        IF v_estado_lab != 'Disponible' THEN
            RETURN FALSE;
        END IF;
        
        -- Verificar si hay reservas que se solapen con el horario solicitado
        SELECT COUNT(*) INTO v_reservas_count
        FROM Reservas
        WHERE Nro_Laboratorio = p_id_laboratorio
        AND fecha_reserva = p_fecha
        AND estado IN ('Confirmado', 'Pendiente')
        AND (
            (hora_inicio < p_hora_fin AND hora_fin > p_hora_inicio)
        );
        
        IF v_reservas_count > 0 THEN
            v_disponible := FALSE;
        END IF;
        
        RETURN v_disponible;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN FALSE; 
        WHEN OTHERS THEN
            RETURN FALSE; 
    END verificar_disponibilidad_laboratorio;
END gestion_laboratorios;
/





DECLARE
    v_resultado VARCHAR2(200);
BEGIN
    v_resultado := gestion_laboratorios.obtener_docente_mas_reservas(
        p_fecha_inicio => TO_DATE('2023-06-01', 'YYYY-MM-DD'),
        p_fecha_fin => TO_DATE('2023-06-30', 'YYYY-MM-DD')
    );
    
    DBMS_OUTPUT.PUT_LINE('Resultado para junio 2023: ' || v_resultado);

END;
/

DECLARE
    v_resultado VARCHAR2(200);
BEGIN
    gestion_laboratorios.actualizar_estado_laboratorio(
        p_id_laboratorio => 1,
        p_nuevo_estado => 'En Mantenimiento',
        p_resultado => v_resultado
    );
    
    DBMS_OUTPUT.PUT_LINE(v_resultado);
END;
/

BEGIN
    IF gestion_laboratorios.verificar_disponibilidad_laboratorio(
        p_id_laboratorio => 2,
        p_fecha => TO_DATE('2023-06-15', 'YYYY-MM-DD'),
        p_hora_inicio => TO_TIMESTAMP('2023-06-15 08:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hora_fin => TO_TIMESTAMP('2023-06-15 10:00:00', 'YYYY-MM-DD HH24:MI:SS')
    ) THEN
        DBMS_OUTPUT.PUT_LINE('Laboratorio disponible');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Laboratorio NO disponible');
    END IF;
END;
/




DECLARE
    v_resultado VARCHAR2(200);
    v_id_reserva NUMBER;
BEGIN
    gestion_laboratorios.realizar_reserva(
        p_id_laboratorio => 3,
        p_id_usuario => 2, 
        p_tipo_prestamo => 'Clase',
        p_materia => 'Redes y Telecomunicaciones',
        p_fecha => TO_DATE('2025-06-20', 'YYYY-MM-DD'), 
        p_hora_inicio => TO_TIMESTAMP('2025-06-20 08:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hora_fin => TO_TIMESTAMP('2025-06-20 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_usa_material => 1,
        p_resultado => v_resultado,
        p_id_reserva => v_id_reserva
    );
    DBMS_OUTPUT.PUT_LINE(v_resultado);
END;
/

--CURSOR IMPLÍCITO
SQL> SET SERVEROUTPUT ON
SQL> DECLARE

BEGIN

  SELECT nombre, rol, username, COUNT(id_reservas) AS total_reservas, MAX(fecha_reserva) AS ultima_fecha
  FROM usuarios INNER JOIN reservas USING (id_usuario)
  WHERE id_usuario = 4;

  DBMS_OUTPUT.PUT_LINE(
    'Nombre: ' || nombre || 
    ' | Rol: ' || rol || 
    ' | Username: ' || username || 
    ' | Total reservas: ' || total_reservas || 
    ' | Última reserva: ' || ultima_fecha
  );

END;
/

--CURSOR EXPLÍCITO
SQL> DECLARE
BEGIN
  FOR explicito IN (
    SELECT nombre, rol, username, COUNT(id_reservas) AS total_reservas, MAX(fecha_reserva) AS ultima_fecha
    FROM usuarios
    INNER JOIN reservas USING (id_usuario)
  )
  LOOP
    DBMS_OUTPUT.PUT_LINE(
      'Nombre: ' || nombre ||
      ' | Rol: ' || rol ||
      ' | Username: ' || username ||
      ' | Total reservas: ' || total_reservas ||
      ' | Ultima reserva: ' || ultima_fecha
    );
  END LOOP;
END;
/
