package com.almacen.producto.repository;

import com.almacen.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivo(boolean activo);
    List<Producto> findByActivoAndProveedorId(boolean activo, Long proveedorId);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
