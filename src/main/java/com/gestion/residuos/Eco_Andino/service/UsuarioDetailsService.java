package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Rol;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    // El "username" recibido puede ser el nombre de usuario o el correo
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + login));

        Set<String> authorities = new LinkedHashSet<>();
        for (Rol rol : usuario.getRoles()) {
            authorities.add("ROLE_" + rol.getCodigo());
            rol.getPermisos().forEach(permiso -> authorities.add(permiso.getCodigo()));
        }

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities.toArray(String[]::new))
                .disabled(!usuario.isActivo())
                .build();
    }
}
