USE bd_proveedor;
CREATE TABLE IF NOT EXISTS proveedor (
  id BIGINT NOT NULL AUTO_INCREMENT,
  documento VARCHAR(20) NOT NULL,
  razon_social VARCHAR(120) NOT NULL,
  contacto VARCHAR(100) NULL,
  telefono VARCHAR(20) NULL,
  email VARCHAR(100) NULL,
  direccion VARCHAR(200) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_proveedor_documento UNIQUE (documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
