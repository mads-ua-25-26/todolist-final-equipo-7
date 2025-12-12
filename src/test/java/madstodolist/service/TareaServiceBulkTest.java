package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TareaServiceBulkTest {

    @Autowired
    TareaService tareaService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TareaRepository tareaRepository;

    @Test
    @Transactional
    public void restaurarVariasTareas() {
        // GIVEN
        Usuario usuario = new Usuario("ana.garcia@gmail.com");
        usuario.setId(1L); // Simulado o real dependiendo de la BD
        usuario = usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Tarea 1");
        tarea1.setVisible(false);
        tareaRepository.save(tarea1);

        Tarea tarea2 = new Tarea(usuario, "Tarea 2");
        tarea2.setVisible(false);
        tareaRepository.save(tarea2);

        List<Long> ids = Arrays.asList(tarea1.getId(), tarea2.getId());

        // WHEN
        tareaService.restaurarTareas(ids);

        // THEN
        Tarea t1 = tareaRepository.findById(tarea1.getId()).orElse(null);
        Tarea t2 = tareaRepository.findById(tarea2.getId()).orElse(null);

        assertThat(t1.getVisible()).isTrue();
        assertThat(t2.getVisible()).isTrue();
    }

    @Test
    @Transactional
    public void borrarDefinitivamenteVariasTareas() {
        // GIVEN
        Usuario usuario = new Usuario("pepe.garcia@gmail.com");
        usuario = usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Tarea Borrar 1");
        tareaRepository.save(tarea1);

        Tarea tarea2 = new Tarea(usuario, "Tarea Borrar 2");
        tareaRepository.save(tarea2);

        List<Long> ids = Arrays.asList(tarea1.getId(), tarea2.getId());

        // WHEN
        tareaService.borrarTareasDefinitivamente(ids);

        // THEN
        assertThat(tareaRepository.existsById(tarea1.getId())).isFalse();
        assertThat(tareaRepository.existsById(tarea2.getId())).isFalse();
    }
}
