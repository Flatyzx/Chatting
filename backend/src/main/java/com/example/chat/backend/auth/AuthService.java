package com.example.chat.backend.auth;

import com.example.chat.backend.entity.Usuario;
import com.example.chat.backend.repository.UsuarioRepository;
import com.example.chat.backend.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public void registrar(RegisterRequest request) {
        String nomeUsuario = normalize(request == null ? null : request.nomeUsuario());
        String senha = request == null ? null : request.senha();
        validateRegistration(nomeUsuario, senha);

        if (usuarioRepository.existsByNomeUsuario(nomeUsuario)) {
            throw new UsernameAlreadyExistsException("O nome de usuário já está cadastrado.");
        }

        try {
            usuarioRepository.save(new Usuario(nomeUsuario, passwordEncoder.encode(senha)));
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException("O nome de usuário já está cadastrado.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        String nomeUsuario = normalize(request == null ? null : request.nomeUsuario());
        String senha = request == null ? null : request.senha();
        if (nomeUsuario.isBlank() || senha == null || senha.isBlank()) {
            throw new InvalidCredentialsException("Usuário ou senha inválidos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(nomeUsuario, senha)
            );
        } catch (BadCredentialsException exception) {
            throw new InvalidCredentialsException("Usuário ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário ou senha inválidos."));
        return new AuthResponse(
                jwtService.generateToken(usuario),
                "Bearer",
                usuario.getNomeUsuario(),
                jwtService.getExpirationSeconds()
        );
    }

    private void validateRegistration(String nomeUsuario, String senha) {
        if (nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("O nome de usuário é obrigatório.");
        }
        if (nomeUsuario.length() < 3 || nomeUsuario.length() > 50) {
            throw new IllegalArgumentException("O nome de usuário deve ter entre 3 e 50 caracteres.");
        }
        if (!nomeUsuario.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("O nome de usuário contém caracteres inválidos.");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
