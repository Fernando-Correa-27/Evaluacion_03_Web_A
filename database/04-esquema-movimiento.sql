USE bd_movimiento;
CREATE TABLE IF NOT EXISTS movimiento (
  id BIGINT NOT NULL AUTO_INCREMENT,
  producto_id BIGINT NOT NULL,
  tipo VARCHAR(10) NOT NULL,
  cantidad INT NOT NULL,
  fecha DATETIME NOT NULL,
  observacion VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT ck_movimiento_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_movimiento_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA')),
  INDEX idx_movimiento_producto_fecha (producto_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
