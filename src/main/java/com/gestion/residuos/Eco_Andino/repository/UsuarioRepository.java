package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Trae roles y permisos en la misma consulta para no depender de la sesión abierta (LazyInitializationException)
    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    @Query("select u from Usuario u where lower(u.username) = lower(:login) or lower(u.email) = lower(:login)")
    Optional<Usuario> findByUsernameOrEmail(@Param("login") String login);

    boolean existsByUsername(String username);
}
