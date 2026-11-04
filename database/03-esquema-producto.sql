USE bd_producto;
CREATE TABLE IF NOT EXISTS producto (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(30) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  descripcion VARCHAR(255) NULL,
  categoria VARCHAR(60) NULL,
  unidad_medida VARCHAR(20) NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  stock_minimo INT NOT NULL DEFAULT 0,
  proveedor_id BIGINT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_producto_codigo UNIQUE (codigo),
  CONSTRAINT ck_producto_precio CHECK (precio_unitario >= 0),
  CONSTRAINT ck_producto_stock_minimo CHECK (stock_minimo >= 0),
  INDEX idx_producto_proveedor (proveedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
