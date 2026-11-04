CREATE DATABASE IF NOT EXISTS bd_proveedor CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS bd_producto CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS bd_movimiento CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS bd_proveedor_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS bd_producto_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS bd_movimiento_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Opcional: cambia CAMBIAR_ESTA_CLAVE antes de ejecutar y conserva el secreto fuera de Git.
CREATE USER IF NOT EXISTS 'almacen_app'@'localhost' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
GRANT ALL PRIVILEGES ON bd_proveedor.* TO 'almacen_app'@'localhost';
GRANT ALL PRIVILEGES ON bd_producto.* TO 'almacen_app'@'localhost';
GRANT ALL PRIVILEGES ON bd_movimiento.* TO 'almacen_app'@'localhost';
GRANT ALL PRIVILEGES ON bd_proveedor_test.* TO 'almacen_app'@'localhost';
GRANT ALL PRIVILEGES ON bd_producto_test.* TO 'almacen_app'@'localhost';
GRANT ALL PRIVILEGES ON bd_movimiento_test.* TO 'almacen_app'@'localhost';
FLUSH PRIVILEGES;
