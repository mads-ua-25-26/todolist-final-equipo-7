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
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // Añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche", null, null);
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI", null, null);

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS", null, null);

        // THEN
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        assertThat(tareas).contains(nuevaTarea);
        assertThat(nuevaTarea.esRaiz()).isTrue();
    }

    @Test
    public void testNuevaTareaConFechaFinalizacion() {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        java.time.LocalDate fecha = java.time.LocalDate.of(2023, 12, 31);
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea con fecha", "Descripción", fecha);

        // THEN
        TareaData tareaRecuperada = tareaService.findById(nuevaTarea.getId());
        assertThat(tareaRecuperada.getFechaFinalizacion()).isEqualTo(fecha);
    }

    @Test
    public void testFindById() {
        // GIVEN
        Long tareaId = addUsuarioTareasBD().get("tareaId");

        // WHEN
        TareaData lavarCoche = tareaService.findById(tareaId);

        // THEN
        assertThat(lavarCoche).isNotNull();
        assertThat(lavarCoche.getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void testModificarTarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche", null, null);

        // THEN
        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    @Test
    public void testBorrarTarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        tareaService.borraTarea(tareaId);

        // THEN
        assertThat(tareaService.findById(tareaId)).isNull();

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(1);
    }

    @Test
    public void testUsuarioContieneTarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        assertThat(tareaService.usuarioContieneTarea(usuarioId, tareaId)).isTrue();
    }

    @Test
    public void testNuevaTareaSeAñadeAlFinal() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        // WHEN
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuarioId, "Tercera tarea", null, null);

        // THEN
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        assertThat(tareas.get(2).getId()).isEqualTo(tarea3.getId());
    }

    @Test
    public void testActualizarOrden() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        List<TareaData> tareasIniciales = tareaService.allTareasUsuario(usuarioId);
        Long idTarea1 = tareasIniciales.get(0).getId();
        Long idTarea2 = tareasIniciales.get(1).getId();

        // WHEN
        tareaService.actualizarOrden(usuarioId, java.util.Arrays.asList(idTarea2, idTarea1));

        // THEN
        List<TareaData> tareasReordenadas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareasReordenadas.get(0).getId()).isEqualTo(idTarea2);
        assertThat(tareasReordenadas.get(1).getId()).isEqualTo(idTarea1);
    }

    @Test
    public void testCrearSubtarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        // WHEN
        TareaData subtarea = tareaService.nuevaSubtarea(tareaId, "Comprar jabón");

        // THEN
        assertThat(subtarea).isNotNull();
        assertThat(subtarea.getTitulo()).isEqualTo("Comprar jabón");
        assertThat(subtarea.esRaiz()).isFalse();
        assertThat(subtarea.getTareaPadreId()).isEqualTo(tareaId);

        // Verificar que la tarea padre contiene la subtarea
        TareaData tareaPadre = tareaService.findById(tareaId);
        assertThat(tareaPadre.getSubtareas()).hasSize(1);
        assertThat(tareaPadre.getSubtareas().get(0).getId()).isEqualTo(subtarea.getId());
    }

    @Test
    public void testObtenerSubtareas() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        TareaData subtarea1 = tareaService.nuevaSubtarea(tareaId, "Subtarea 1");
        TareaData subtarea2 = tareaService.nuevaSubtarea(tareaId, "Subtarea 2");

        // WHEN
        List<TareaData> subtareas = tareaService.obtenerSubtareas(tareaId);

        // THEN
        assertThat(subtareas).hasSize(2);
        assertThat(subtareas).extracting(TareaData::getId)
                .containsExactly(subtarea1.getId(), subtarea2.getId());
    }

    @Test
    public void testAsignarEtiquetaATarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
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
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        Etiqueta etiqueta = etiquetaService.crearEtiqueta(usuarioId, "Urgente", "red");
        tareaService.asignarEtiqueta(tareaId, etiqueta.getId());

        // WHEN
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        TareaData tareaData = tareas.stream()
                .filter(t -> t.getId().equals(tareaId))
                .findFirst()
                .orElse(null);

        // THEN
        assertThat(tareaData).isNotNull();
        assertThat(tareaData.getEtiquetas()).hasSize(1);
        assertThat(tareaData.getEtiquetas()).contains(etiqueta);
    }

    @Test
    public void testBorrarTareaPadreEliminaSubtareas() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        TareaData subtarea1 = tareaService.nuevaSubtarea(tareaId, "Subtarea 1");
        TareaData subtarea2 = tareaService.nuevaSubtarea(tareaId, "Subtarea 2");
        Long subtarea1Id = subtarea1.getId();
        Long subtarea2Id = subtarea2.getId();

        // WHEN
        tareaService.borraTarea(tareaId);

        // THEN
        assertThat(tareaService.findById(tareaId)).isNull();
        assertThat(tareaService.findById(subtarea1Id)).isNull();
        assertThat(tareaService.findById(subtarea2Id)).isNull();
    }

    @Test
    public void testModificarSubtarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");
        TareaData subtarea = tareaService.nuevaSubtarea(tareaId, "Subtarea original");

        // WHEN
        tareaService.modificaTarea(subtarea.getId(), "Subtarea modificada", null, null);

        // THEN
        TareaData subtareaModificada = tareaService.findById(subtarea.getId());
        assertThat(subtareaModificada.getTitulo()).isEqualTo("Subtarea modificada");
    }

    @Test
    public void testSubtareasNoAparecenEnListadoPrincipal() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // Crear subtareas
        tareaService.nuevaSubtarea(tareaId, "Subtarea 1");
        tareaService.nuevaSubtarea(tareaId, "Subtarea 2");

        // WHEN
        List<TareaData> tareasRaiz = tareaService.allTareasUsuario(usuarioId);

        // THEN
        // Solo deben aparecer las 2 tareas raíz, no las subtareas
        assertThat(tareasRaiz).hasSize(2);
        assertThat(tareasRaiz).allMatch(TareaData::esRaiz);

        // Pero la tarea padre debe tener sus subtareas
        TareaData tareaPadre = tareasRaiz.stream()
                .filter(t -> t.getId().equals(tareaId))
                .findFirst()
                .orElse(null);
        assertThat(tareaPadre).isNotNull();
        assertThat(tareaPadre.getSubtareas()).hasSize(2);
    }

    @Test
    public void testSubtareaDeSubtarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        // Crear una subtarea
        TareaData subtarea1 = tareaService.nuevaSubtarea(tareaId, "Subtarea nivel 1");

        // WHEN - Crear una subtarea de la subtarea (subtarea de nivel 2)
        TareaData subtarea2 = tareaService.nuevaSubtarea(subtarea1.getId(), "Subtarea nivel 2");

        // THEN
        assertThat(subtarea2.getTareaPadreId()).isEqualTo(subtarea1.getId());

        // Verificar la jerarquía
        TareaData subtarea1Actualizada = tareaService.findById(subtarea1.getId());
        assertThat(subtarea1Actualizada.getSubtareas()).hasSize(1);
        assertThat(subtarea1Actualizada.getSubtareas().get(0).getId()).isEqualTo(subtarea2.getId());
    }

    @Test
    public void testActualizarEtiquetas() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");
        Long usuarioId = ids.get("usuarioId");

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