package madstodolist.service;

import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.model.Etiqueta;
import madstodolist.model.Tarea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

// Hemos eliminado todos los @Transactional de los tests
// y usado un script para limpiar la BD de test después de
// cada test
// https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    EtiquetaService etiquetaService;

    @Autowired
    madstodolist.repository.TareaRepository tareaRepository;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea
    // añadida
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        // Añadimos un usuario a la base de datos
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche", null, null);
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI", null, null);

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        // creamos una nueva tarea asociada al usuario,
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS", null, null);

        // THEN
        // al recuperar la lista de tareas del usuario, la nueva tarea
        // está en la lista de tareas del usuario.

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        assertThat(tareas).contains(nuevaTarea);
    }

    @Test
    public void testNuevaTareaConFechaFinalizacion() {
        // GIVEN
        // Un usuario en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        // creamos una nueva tarea asociada al usuario con fecha de finalización
        java.time.LocalDate fecha = java.time.LocalDate.of(2023, 12, 31);
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea con fecha", "Descripción", fecha);

        // THEN
        // la tarea recuperada tiene la fecha correcta
        TareaData tareaRecuperada = tareaService.findById(nuevaTarea.getId());
        assertThat(tareaRecuperada.getFechaFinalizacion()).isEqualTo(fecha);
    }

    // ... skipping unchanged parts ...
    @Test
    public void testModificarTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        // modificamos la tarea correspondiente al identificador,

        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche", null, null);

        // THEN
        // al buscar por el identificador en la base de datos se devuelve la tarea
        // modificada

        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");

        // y el usuario tiene también esa tarea modificada.
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    // ...
    @Test
    public void testNuevaTareaSeAñadeAlFinal() {
        // GIVEN
        // Un usuario con 2 tareas creadas por el método auxiliar (posiciones 1 y 2)
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        // WHEN
        // Creamos una tercera tarea
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuarioId, "Tercera tarea", null, null);

        // THEN
        // Al recuperar las tareas, la nueva está en la última posición (índice 2)
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        // Verificamos que la última de la lista es la que acabamos de crear
        assertThat(tareas.get(2).getId()).isEqualTo(tarea3.getId());
    }

    @Test
    public void testActualizarOrden() {
        // GIVEN
        // Un usuario con 2 tareas: "Lavar coche" (id1, pos 1) y "Renovar DNI" (id2, pos
        // 2)
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        List<TareaData> tareasIniciales = tareaService.allTareasUsuario(usuarioId);
        Long idTarea1 = tareasIniciales.get(0).getId(); // Lavar coche
        Long idTarea2 = tareasIniciales.get(1).getId(); // Renovar DNI

        // WHEN
        // Cambiamos el orden para que la segunda pase a ser la primera
        // Enviamos una lista con [idTarea2, idTarea1]
        tareaService.actualizarOrden(usuarioId, java.util.Arrays.asList(idTarea2, idTarea1));

        // THEN
        // Al recuperar las tareas, el orden ha cambiado
        List<TareaData> tareasReordenadas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareasReordenadas.get(0).getId()).isEqualTo(idTarea2); // Renovar DNI ahora es primera
        assertThat(tareasReordenadas.get(1).getId()).isEqualTo(idTarea1); // Lavar coche ahora es segunda
    }

    @Test
    public void asignarEtiquetaATarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId"); // Recuperamos ID usuario
        Long tareaId = ids.get("tareaId");

        Etiqueta etiqueta = etiquetaService.crearEtiqueta(usuarioId, "Urgente", "red");

        // WHEN
        tareaService.asignarEtiqueta(tareaId, etiqueta.getId());

        // THEN
        madstodolist.model.Tarea tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getEtiquetas()).hasSize(1);
        assertThat(tareaBD.getEtiquetas().contains(etiqueta)).isTrue();
    }

    @Test
    public void testDevuelveEtiquetasEnTareaData() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        Etiqueta etiqueta = etiquetaService.crearEtiqueta(usuarioId, "Urgente", "red");

        tareaService.asignarEtiqueta(tareaId, etiqueta.getId());

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        TareaData tareaData = tareas.stream()
                .filter(t -> t.getId().equals(tareaId))
                .findFirst()
                .orElse(null);

        assertThat(tareaData).isNotNull();
        assertThat(tareaData.getEtiquetas()).hasSize(1);
        assertThat(tareaData.getEtiquetas()).contains(etiqueta);
    }

    @Test
    public void testActualizarEtiquetas() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");
        Long usuarioId = ids.get("usuarioId"); // Recuperamos ID usuario

        Etiqueta e1 = etiquetaService.crearEtiqueta(usuarioId, "E1", "red");
        Etiqueta e2 = etiquetaService.crearEtiqueta(usuarioId, "E2", "blue");
        Etiqueta e3 = etiquetaService.crearEtiqueta(usuarioId, "E3", "green");

        // WHEN: Asignamos E1 y E2
        tareaService.actualizarEtiquetas(tareaId, List.of(e1.getId(), e2.getId()));

        // THEN
        Tarea tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getEtiquetas()).hasSize(2);
        assertThat(tareaBD.getEtiquetas()).contains(e1, e2);

        // WHEN: Cambiamos para tener E2 y E3
        tareaService.actualizarEtiquetas(tareaId, List.of(e2.getId(), e3.getId()));

        // THEN
        tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getEtiquetas()).hasSize(2);
        assertThat(tareaBD.getEtiquetas()).contains(e2, e3);
        assertThat(tareaBD.getEtiquetas()).doesNotContain(e1);

        // WHEN: Pasamos lista vacía
        tareaService.actualizarEtiquetas(tareaId, new ArrayList<>());

        // THEN
        tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getEtiquetas()).isEmpty();
    }
}
