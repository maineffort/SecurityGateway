package de.cavas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.cavas.model.Alert;


@Repository 
public interface AlertRepository extends JpaRepository<Alert, Integer>{

}
