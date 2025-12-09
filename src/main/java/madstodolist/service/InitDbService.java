package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.model.Etiqueta;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
// Se ejecuta solo si el perfil activo es 'dev'
@Profile({"dev", "posttest"})
public class InitDbService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    EtiquetaService etiquetaService;

    // Se ejecuta tras crear el contexto de la aplicación
    // para inicializar la base de datos
    @PostConstruct
    public void initDatabase() {
        Usuario usuario = usuarioRepository.findByEmail("user@ua").orElse(null);

        if (usuario == null) {
            usuario = new Usuario("user@ua");
            usuario.setNombre("Usuario Ejemplo");
            usuario.setPassword("123");
            usuario = usuarioRepository.save(usuario);

            Tarea tarea1 = new Tarea(usuario, "Lavar coche");
            tareaRepository.save(tarea1);

            Tarea tarea2 = new Tarea(usuario, "Renovar DNI");
            tareaRepository.save(tarea2);
        }

        // Migración para usuarios ya existentes en la bbdd
        Iterable<Usuario> todosLosUsuarios = usuarioRepository.findAll();

        for (Usuario u : todosLosUsuarios) {
            // Consultamos cuántas etiquetas tiene este usuario
            List<Etiqueta> susEtiquetas = etiquetaService.findAllByUsuario(u.getId());

            // Solo creamos las etiquetas si la lista está VACÍA
            // Así evitamos duplicados si reinicias la app muchas veces
            if (susEtiquetas.isEmpty()) {
                etiquetaService.crearEtiqueta(u.getId(), "Personal", "#28a745");
                etiquetaService.crearEtiqueta(u.getId(), "Escuela", "#007bff");
                etiquetaService.crearEtiqueta(u.getId(), "Importante", "#dc3545");
                System.out.println("Etiquetas creadas para el usuario: " + u.getEmail());
            }
        }
    }

}
