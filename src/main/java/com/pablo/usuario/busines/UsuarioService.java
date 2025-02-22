package com.pablo.usuario.busines;

import com.pablo.usuario.busines.converter.UsuarioConverter;
import com.pablo.usuario.busines.dto.UsuarioDTO;
import com.pablo.usuario.infrastructure.entity.Usuario;
import com.pablo.usuario.infrastructure.exceptions.ConflictException;
import com.pablo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.pablo.usuario.infrastructure.repository.UsuarioRepository;
import com.pablo.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw new ConflictException("Email ja cadastrado " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado", e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarusuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado" + email));
    }

    public void deletaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto) {
       // busca email do usuario atraves do token
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null );

       // Busca dados de usuario do banco
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));
        // mescla dados recebidos da request DTO com os dados do banco
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        // salva dados de usuario convertidos, depoios converteu para UsuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }


}
