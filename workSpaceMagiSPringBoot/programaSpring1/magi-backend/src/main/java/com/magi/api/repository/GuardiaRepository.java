package com.magi.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.Guardia;

public interface GuardiaRepository extends JpaRepository<Guardia, Long> {
}
