// src/main/java/com/magi/api/repository/SessionHorarioRepository.java
package com.magi.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.SessionHorario;

public interface SessionHorarioRepository extends JpaRepository<SessionHorario, Integer> {
}
