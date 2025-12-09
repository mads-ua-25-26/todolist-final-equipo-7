package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD}

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    // 1. AÑADIMOS EL SERVICIO DE ETIQUETAS
    @Autowired
    private EtiquetaService etiquetaService;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        else if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else {
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);

            if (usuario.getAdmin() != null && usuario.getAdmin()) {
                usuarioNuevo.setAdmin();
            } else {
                usuarioNuevo.disableAdmin();
            }

            // Guardamos el usuario (aquí se genera su ID)
            usuarioNuevo = usuarioRepository.save(usuarioNuevo);

            // 2. CREAMOS LAS ETIQUETAS POR DEFECTO
            // Usamos los códigos HEX para que coincidan con tu selector de colores
            etiquetaService.crearEtiqueta(usuarioNuevo.getId(), "Personal", "#28a745"); // Verde
            etiquetaService.crearEtiqueta(usuarioNuevo.getId(), "Escuela", "#007bff");  // Azul
            etiquetaService.crearEtiqueta(usuarioNuevo.getId(), "Importante", "#dc3545"); // Rojo

            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> allUsuarios() {
        List<UsuarioData> usuariosData = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll()) {
            usuariosData.add(modelMapper.map(usuario, UsuarioData.class));
        }
        return usuariosData;
    }
}