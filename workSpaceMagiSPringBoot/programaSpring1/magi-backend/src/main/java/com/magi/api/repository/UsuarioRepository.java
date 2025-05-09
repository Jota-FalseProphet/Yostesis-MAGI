package com.magi.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.magi.api.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    boolean existsByDni(String dni);

    @Query("select u.password from Usuario u where u.dni = :dni")
    String getPasswordHash(@Param("dni") String dni);

    @Query("select u.rol from Usuario u where u.dni = :dni")
    String getRol(@Param("dni") String dni);

    Optional<Usuario> findByDni(String dni);
}