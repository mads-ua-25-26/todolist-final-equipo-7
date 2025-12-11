package madstodolist.repository;

import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import madstodolist.model.Etiqueta;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaTest {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TareaRepository tareaRepository;

    @Autowired
    EtiquetaRepository etiquetaRepository;

    // Tests modelo Tarea en memoria

    @Test
    public void crearTarea() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

        // WHEN
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");

        // THEN
        assertThat(tarea.getTitulo()).isEqualTo("Práctica 1 de MADS");
        assertThat(tarea.getUsuario()).isEqualTo(usuario);
        assertThat(tarea.esRaiz()).isTrue();
    }

    @Test
    public void crearSubtarea() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tareaPadre = new Tarea(usuario, "Tarea principal");

        // WHEN
        Tarea subtarea = new Tarea(tareaPadre, "Subtarea 1");

        // THEN
        assertThat(subtarea.getTitulo()).isEqualTo("Subtarea 1");
        assertThat(subtarea.getTareaPadre()).isEqualTo(tareaPadre);
        assertThat(subtarea.esRaiz()).isFalse();
        assertThat(tareaPadre.getSubtareas()).contains(subtarea);
        assertThat(subtarea.getUsuario()).isEqualTo(usuario); // Hereda el usuario
    }

    @Test
    public void laListaDeTareasDeUnUsuarioSeActualizaEnMemoriaConUnaNuevaTarea() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

        // WHEN
        Set<Tarea> tareas = usuario.getTareas();
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");

        // THEN
        assertThat(usuario.getTareas()).contains(tarea);
        assertThat(tareas).contains(tarea);
    }

    @Test
    public void comprobarIgualdadTareasSinId() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS");
        Tarea tarea2 = new Tarea(usuario, "Práctica 1 de MADS");
        Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler");

        // THEN
        assertThat(tarea1).isEqualTo(tarea2);
        assertThat(tarea1).isNotEqualTo(tarea3);
    }

    @Test
    public void comprobarIgualdadTareasConId() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS");
        Tarea tarea2 = new Tarea(usuario, "Lavar la ropa");
        Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler");
        tarea1.setId(1L);
        tarea2.setId(2L);
        tarea3.setId(1L);

        // THEN
        assertThat(tarea1).isEqualTo(tarea3);
        assertThat(tarea1).isNotEqualTo(tarea2);
    }

    // Tests TareaRepository

    @Test
    @Transactional
    public void guardarTareaEnBaseDatos() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");

        // WHEN
        tareaRepository.save(tarea);

        // THEN
        assertThat(tarea.getId()).isNotNull();
        Tarea tareaBD = tareaRepository.findById(tarea.getId()).orElse(null);
        assertThat(tareaBD.getTitulo()).isEqualTo(tarea.getTitulo());
        assertThat(tareaBD.getUsuario()).isEqualTo(usuario);
        assertThat(tareaBD.esRaiz()).isTrue();
    }

    @Test
    @Transactional
    public void guardarSubtareaEnBaseDatos() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tareaPadre = new Tarea(usuario, "Tarea principal");
        tareaRepository.save(tareaPadre);

        Tarea subtarea = new Tarea(tareaPadre, "Subtarea 1");

        // WHEN
        tareaRepository.save(subtarea);

        // THEN
        assertThat(subtarea.getId()).isNotNull();

        Tarea subtareaBD = tareaRepository.findById(subtarea.getId()).orElse(null);
        assertThat(subtareaBD.getTitulo()).isEqualTo("Subtarea 1");
        assertThat(subtareaBD.getTareaPadre().getId()).isEqualTo(tareaPadre.getId());
        assertThat(subtareaBD.esRaiz()).isFalse();

        // Verificar que la tarea padre tiene la subtarea
        Tarea tareaPadreBD = tareaRepository.findById(tareaPadre.getId()).orElse(null);
        assertThat(tareaPadreBD.getSubtareas()).hasSize(1);
    }

    @Test
    @Transactional
    public void salvarTareaEnBaseDatosConUsuarioNoBDLanzaExcepcion() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");

        // WHEN // THEN
        Assertions.assertThrows(Exception.class, () -> {
            tareaRepository.save(tarea);
        });
    }

    @Test
    @Transactional
    public void unUsuarioTieneUnaListaDeTareas() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS");
        Tarea tarea2 = new Tarea(usuario, "Renovar el DNI");
        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);

        // WHEN
        Usuario usuarioRecuperado = usuarioRepository.findById(usuarioId).orElse(null);

        // THEN
        assertThat(usuarioRecuperado.getTareas()).hasSize(2);
    }

    @Test
    @Transactional
    public void añadirUnaTareaAUnUsuarioEnBD() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        // WHEN
        Usuario usuarioBD = usuarioRepository.findById(usuarioId).orElse(null);
        Tarea tarea = new Tarea(usuarioBD, "Práctica 1 de MADS");
        tareaRepository.save(tarea);
        Long tareaId = tarea.getId();

        // THEN
        Tarea tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD).isEqualTo(tarea);
        assertThat(tarea.getUsuario()).isEqualTo(usuarioBD);

        usuarioBD = usuarioRepository.findById(usuarioId).orElse(null);
        assertThat(usuarioBD.getTareas()).contains(tareaBD);
    }

    @Test
    @Transactional
    public void cambioEnLaEntidadEnTransactionalModificaLaBD() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");
        tareaRepository.save(tarea);

        Long tareaId = tarea.getId();
        tarea = tareaRepository.findById(tareaId).orElse(null);

        // WHEN
        tarea.setTitulo("Esto es una prueba");

        // THEN
        Tarea tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getTitulo()).isEqualTo(tarea.getTitulo());
    }

    @Test
    public void comprobarPosicionTarea() {
        // GIVEN
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");

        // WHEN
        tarea.setPosition(5);

        // THEN
        assertThat(tarea.getPosition()).isEqualTo(5);
    }

    @Test
    @Transactional
    public void guardarTareaConPosicionEnBaseDatos() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");
        tarea.setPosition(3);

        // WHEN
        tareaRepository.save(tarea);

        // THEN
        assertThat(tarea.getId()).isNotNull();
        Tarea tareaBD = tareaRepository.findById(tarea.getId()).orElse(null);
        assertThat(tareaBD).isNotNull();
        assertThat(tareaBD.getPosition()).isEqualTo(3);
    }

    @Test
    @Transactional
    public void añadirEtiquetaAUnaTarea() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea = new Tarea(usuario, "Comprar leche");
        tareaRepository.save(tarea);

        Etiqueta etiqueta = new Etiqueta(usuario, "Compras", "green");
        etiquetaRepository.save(etiqueta);

        // WHEN
        tarea.addEtiqueta(etiqueta);
        tareaRepository.save(tarea);

        // THEN
        Tarea tareaBD = tareaRepository.findById(tarea.getId()).orElse(null);
        assertThat(tareaBD.getEtiquetas()).hasSize(1);
        assertThat(tareaBD.getEtiquetas()).contains(etiqueta);
    }

    @Test
    @Transactional
    public void eliminarTareaPadreEliminaSubtareas() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tareaPadre = new Tarea(usuario, "Tarea principal");
        tareaRepository.save(tareaPadre);
        Long tareaPadreId = tareaPadre.getId();

        Tarea subtarea1 = new Tarea(tareaPadre, "Subtarea 1");
        Tarea subtarea2 = new Tarea(tareaPadre, "Subtarea 2");
        tareaRepository.save(subtarea1);
        tareaRepository.save(subtarea2);
        Long subtarea1Id = subtarea1.getId();
        Long subtarea2Id = subtarea2.getId();

        // WHEN
        tareaRepository.delete(tareaPadre);

        // THEN - Las subtareas también deben haber sido eliminadas
        assertThat(tareaRepository.findById(tareaPadreId)).isEmpty();
        assertThat(tareaRepository.findById(subtarea1Id)).isEmpty();
        assertThat(tareaRepository.findById(subtarea2Id)).isEmpty();
    }

    @Test
    @Transactional
    public void recuperarSubtareasDeTarea() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tareaPadre = new Tarea(usuario, "Tarea principal");
        tareaRepository.save(tareaPadre);

        Tarea subtarea1 = new Tarea(tareaPadre, "Subtarea 1");
        Tarea subtarea2 = new Tarea(tareaPadre, "Subtarea 2");
        tareaRepository.save(subtarea1);
        tareaRepository.save(subtarea2);

        // WHEN
        Tarea tareaBD = tareaRepository.findById(tareaPadre.getId()).orElse(null);

        // THEN
        assertThat(tareaBD.getSubtareas()).hasSize(2);
        assertThat(tareaBD.getSubtareas()).contains(subtarea1, subtarea2);
    }

    @Test
    @Transactional
    public void findTareasRaizByUsuarioIdNoDevuelveSubtareas() {
        // GIVEN
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Tarea 1");
        Tarea tarea2 = new Tarea(usuario, "Tarea 2");
        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);

        Tarea subtarea = new Tarea(tarea1, "Subtarea de Tarea 1");
        tareaRepository.save(subtarea);

        // WHEN
        List<Tarea> tareasRaiz = tareaRepository.findTareasRaizByUsuarioId(usuario.getId());

        // THEN - Solo deben aparecer las tareas raíz
        assertThat(tareasRaiz).hasSize(2);
        assertThat(tareasRaiz).containsExactlyInAnyOrder(tarea1, tarea2);
        assertThat(tareasRaiz).doesNotContain(subtarea);
    }
}