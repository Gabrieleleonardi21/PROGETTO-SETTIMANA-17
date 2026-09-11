package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FotoRepository extends JpaRepository<Foto, UUID> {
}
