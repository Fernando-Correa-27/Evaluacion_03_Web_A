package com.almacen.proveedor.services;

import com.almacen.proveedor.dto.*;
import com.almacen.proveedor.exception.*;
import com.almacen.proveedor.mapper.ProveedorMapper;
import com.almacen.proveedor.model.Proveedor;
import com.almacen.proveedor.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProveedorService {
    private final ProveedorRepository repository;
    public ProveedorService(ProveedorRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listar(boolean activo) { return repository.findByActivo(activo).stream().map(ProveedorMapper::aRespuesta).toList(); }
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtener(Long id) { return ProveedorMapper.aRespuesta(buscar(id)); }
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        if (repository.existsByDocumento(dto.documento())) throw new ConflictException("Ya existe un proveedor con ese documento");
        Proveedor proveedor = new Proveedor(); ProveedorMapper.aplicar(dto, proveedor);
        return ProveedorMapper.aRespuesta(repository.save(proveedor));
    }
    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor proveedor = buscar(id);
        if (repository.existsByDocumentoAndIdNot(dto.documento(), id)) throw new ConflictException("Ya existe un proveedor con ese documento");
        ProveedorMapper.aplicar(dto, proveedor);
        return ProveedorMapper.aRespuesta(repository.save(proveedor));
    }
    @Transactional
    public void desactivar(Long id) { Proveedor proveedor = buscar(id); proveedor.setActivo(false); repository.save(proveedor); }
    private Proveedor buscar(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe el proveedor " + id)); }
}
