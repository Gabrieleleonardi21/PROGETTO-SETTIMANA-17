package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
