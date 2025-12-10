-- SQL para crear la tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(150) NOT NULL,
  correo VARCHAR(150) NOT NULL UNIQUE,
  clave_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(50) NOT NULL DEFAULT 'user',
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ejemplo para insertar un usuario (usa contraseña 'secret123')
-- INSERT INTO usuarios (nombre, correo, clave_hash, rol) VALUES ('Admin', 'admin@example.com', '$2y$10$...hash...', 'admin');
-- Recomiendo usar el endpoint registrar_usuario.php para crear usuarios de forma segura.

