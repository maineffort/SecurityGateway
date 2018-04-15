package de.cavas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import de.cavas.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, Integer>{

}
