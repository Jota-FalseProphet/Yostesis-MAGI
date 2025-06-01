package com.magi.api.service;

import com.magi.api.dto.CrearDocenteDTO;
import com.magi.api.dto.DocenteDTO;
import com.magi.api.model.Docent;
import com.magi.api.model.Usuario;
import com.magi.api.repository.DocentRepository;
import com.magi.api.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocenteService {

    private final DocentRepository docentRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DocenteService(DocentRepository docentRepository,
                          UsuarioRepository usuarioRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.docentRepository = docentRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //devuelve la lista completa de docentes mapeada en dcenteDTO
     
    public List<DocenteDTO> listarTodos() {
        List<Docent> entidades = docentRepository.findAll();
        return entidades.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    public DocenteDTO buscarPorDocumento(String dni) {
        Optional<Docent> opt = docentRepository.findByDni(dni);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado");
        }
        return convertirADTO(opt.get());
    }

    //crea un nuevo docente con rol de profesor 
     //luego lo inserta en public.usuarios y usa el trigger que hice ne la bd
    
    @Transactional
    public DocenteDTO crearDocente(CrearDocenteDTO crearDto) {
        //comprobar duplicado de user
        if (usuarioRepository.existsById(crearDto.getDni())) {
            throw new DataIntegrityViolationException(
                "Ya existe un usuario con DNI " + crearDto.getDni()
            );
        }

        //cifro con bycript
        String hash = passwordEncoder.encode(crearDto.getContrasena());

        //construir y guardar la entidad usuario con el mismo rol siemore
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setDni(crearDto.getDni());
        nuevoUsuario.setNombre(crearDto.getNombreCompleto());
        nuevoUsuario.setPassword(hash);
        nuevoUsuario.setRol("PROFESOR");

        try {
            usuarioRepository.save(nuevoUsuario);
        } catch (DataIntegrityViolationException dive) {
            throw dive;
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear usuario/profesor en la base de datos.", ex);
        }

        //devolver un docentDTO basico con id null
        return new DocenteDTO(null, crearDto.getNombreCompleto());
    }

    //Convierte una entidad Docent a un DocenteDTO:
    private DocenteDTO convertirADTO(Docent entidad) {
        StringBuilder sb = new StringBuilder();

        if (entidad.getNom() != null && !entidad.getNom().isBlank()) {
            sb.append(entidad.getNom().trim());
        }
        if (entidad.getCognom1() != null && !entidad.getCognom1().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(entidad.getCognom1().trim());
        }
        if (entidad.getCognom2() != null && !entidad.getCognom2().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(entidad.getCognom2().trim());
        }

        String nombreCompleto = sb.toString();
        if (nombreCompleto.isBlank()) {
            // si no hay nombre/cognoms usamos el campo document
            nombreCompleto = entidad.getDni() != null ? entidad.getDni() : "";
        }

        return new DocenteDTO(entidad.getIdDocent(), nombreCompleto);
    }
}
