// src/main/java/com/magi/api/repository/AusenciaRepository.java
package com.magi.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.Ausencia;

public interface AusenciaRepository extends JpaRepository<Ausencia, Integer> {
}
