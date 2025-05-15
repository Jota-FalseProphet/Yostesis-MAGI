package com.magi.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.Docent;

public interface DocentRepository extends JpaRepository<Docent, Integer> {
    Optional<Docent> findByDni(String dni);
}
