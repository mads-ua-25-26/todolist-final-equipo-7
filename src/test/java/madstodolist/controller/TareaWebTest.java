package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TareaWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche", null);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI", null);

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    @Test
    public void listaTareas() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                ))));
    }

    @Test
    public void getNuevaTareaDevuelveForm() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlAction = "action=\"/usuarios/" + usuarioId.toString() + "/tareas/nueva\"";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect((content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                ))));
    }

    @Test
    public void postNuevaTareaDevuelveRedirectYAñadeTarea() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Estudiar examen MADS")
                        .param("descripcion", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("Estudiar examen MADS"))));
    }

    @Test
    public void deleteTareaDevuelveOKyBorraTarea() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // Usamos POST al endpoint /borrar (estilo formulario HTML)
        String urlBorrarPost = "/tareas/" + tareaLavarCocheId + "/borrar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlBorrarPost))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(
                        allOf(not(containsString("Lavar coche")),
                                containsString("Renovar DNI"))));
    }

    @Test
    public void editarTareaActualizaLaTarea() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaLavarCocheId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Limpiar cristales coche")
                        .param("descripcion", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("Limpiar cristales coche")));
    }

    @Test
    public void getEditarTareaDevuelveForm() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/tareas/" + tareaId + "/editar";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("formEditarTarea"))
                .andExpect(content().string(containsString("Lavar coche")));
    }

    @Test
    public void reordenarTareasActualizaOrden() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        Long idTarea1 = tareas.get(0).getId();
        Long idTarea2 = tareas.get(1).getId();

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(post("/tareas/guardarOrden")
                        .param("orden_root", idTarea2.toString())
                        .param("orden_root", idTarea1.toString()))
                .andExpect(status().is3xxRedirection());

        List<TareaData> tareasReordenadas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareasReordenadas.get(0).getId()).isEqualTo(idTarea2);
        assertThat(tareasReordenadas.get(1).getId()).isEqualTo(idTarea1);
    }
}