package com.almacen.producto.services;

import com.almacen.producto.client.ProveedorClient;
import com.almacen.producto.dto.*;
import com.almacen.producto.exception.*;
import com.almacen.producto.mapper.ProductoMapper;
import com.almacen.producto.model.Producto;
import com.almacen.producto.repository.ProductoRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repository; private final ProveedorClient proveedorClient;
    public ProductoService(ProductoRepository repository, ProveedorClient proveedorClient) { this.repository=repository; this.proveedorClient=proveedorClient; }
    @Transactional(readOnly=true) public List<ProductoResponseDTO> listar(boolean activo, Long proveedorId) {
        var items=proveedorId==null?repository.findByActivo(activo):repository.findByActivoAndProveedorId(activo,proveedorId);
        return items.stream().map(ProductoMapper::aRespuesta).toList();
    }
    @Transactional(readOnly=true) public ProductoResponseDTO obtener(Long id) { return ProductoMapper.aRespuesta(buscar(id)); }
    @Transactional public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        validarProveedor(dto.proveedorId());
        if(repository.existsByCodigo(dto.codigo())) throw new ConflictException("Ya existe un producto con ese código");
        Producto p=new Producto(); ProductoMapper.aplicar(dto,p); return ProductoMapper.aRespuesta(repository.save(p));
    }
    @Transactional public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto p=buscar(id); validarProveedor(dto.proveedorId());
        if(repository.existsByCodigoAndIdNot(dto.codigo(),id)) throw new ConflictException("Ya existe un producto con ese código");
        ProductoMapper.aplicar(dto,p); return ProductoMapper.aRespuesta(repository.save(p));
    }
    @Transactional public void desactivar(Long id) { Producto p=buscar(id); p.setActivo(false); repository.save(p); }
    private Producto buscar(Long id) { return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("No existe el producto "+id)); }
    private void validarProveedor(Long id) {
        ProveedorDTO proveedor;
        try { proveedor=proveedorClient.obtener(id); }
        catch(FeignException e) { if(e.status()==404) throw new ResourceNotFoundException("No existe el proveedor "+id); throw new ServiceUnavailableException("No fue posible consultar proveedor-service"); }
        if(!proveedor.activo()) throw new UnprocessableException("El proveedor está inactivo");
    }
}
