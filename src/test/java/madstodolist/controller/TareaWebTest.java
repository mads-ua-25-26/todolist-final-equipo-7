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
import static org.assertj.core.api.Assertions.assertThat;
import madstodolist.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        @Autowired
        private madstodolist.service.EtiquetaService etiquetaService;

        @Autowired
        private madstodolist.repository.EtiquetaRepository etiquetaRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @MockBean
        private ManagerUserSession managerUserSession;

        Map<String, Long> addUsuarioTareasBD() {
                UsuarioData usuario = new UsuarioData();
                usuario.setEmail("user@ua");
                usuario.setPassword("123");
                usuario = usuarioService.registrar(usuario);

                TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche", null, null);
                tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI", null, null);

                Map<String, Long> ids = new HashMap<>();
                ids.put("usuarioId", usuario.getId());
                ids.put("tareaId", tarea1.getId());
                return ids;
        }

        @Test
        public void listadoTareas() throws Exception {
                Long usuarioId = addUsuarioTareasBD().get("usuarioId");
                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                String url = "/usuarios/" + usuarioId.toString() + "/tareas";

                this.mockMvc.perform(get(url))
                                .andExpect((content().string(allOf(
                                                containsString("Lavar coche"),
                                                containsString("Renovar DNI")))))
                                .andExpect(model().attributeExists("today"))
                                .andExpect(model().attributeExists("tomorrow"));
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
                                                containsString(urlAction)))));
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
                                .param("descripcion", "")
                                .param("fechaFinalizacion", "2023-12-31"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl(urlRedirect));

                this.mockMvc.perform(get(urlRedirect))
                                .andExpect(content().string(containsString("Limpiar cristales coche")))
                                .andExpect(content().string(containsString("31-12-2023")));
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

        @Test
        public void editarTareaConMultiplesEtiquetasActualizaCorrectamente() throws Exception {
                Map<String, Long> ids = addUsuarioTareasBD();
                Long usuarioId = ids.get("usuarioId");
                Long tareaId = ids.get("tareaId");

                madstodolist.model.Usuario usuario = usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                madstodolist.model.Etiqueta e1 = new madstodolist.model.Etiqueta(usuario, "Tag1", "red");
                madstodolist.model.Etiqueta e2 = new madstodolist.model.Etiqueta(usuario, "Tag2", "blue");
                e1 = etiquetaRepository.save(e1);
                e2 = etiquetaRepository.save(e2);

                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                this.mockMvc.perform(post("/tareas/" + tareaId + "/editar")
                                .param("titulo", "Tarea con etiquetas")
                                .param("descripcion", "Descripción editada")
                                .param("etiquetaIds", e1.getId().toString())
                                .param("etiquetaIds", e2.getId().toString()))
                                .andExpect(status().is3xxRedirection());

                TareaData tareaModificada = tareaService.findById(tareaId);
                assertThat(tareaModificada.getEtiquetas()).hasSize(2);
        }

        @Test
        public void completarTareaRedirigeAListado() throws Exception {
                Map<String, Long> ids = addUsuarioTareasBD();
                Long usuarioId = ids.get("usuarioId");
                Long tareaId = ids.get("tareaId");

                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                this.mockMvc.perform(post("/tareas/" + tareaId + "/completar"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas"));
        }

        @Test
        public void listadoTareasOcultaLasCompletadas() throws Exception {
                // GIVEN
                Map<String, Long> ids = addUsuarioTareasBD();
                Long usuarioId = ids.get("usuarioId");
                Long tareaId = ids.get("tareaId");

                // Marcamos la tarea como completada en el servicio (simulación)
                tareaService.completarTarea(tareaId);

                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                // WHEN: Pedimos el listado normal
                this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas"))
                                .andExpect(status().isOk())
                                // THEN: No debería contener "Lavar coche" porque está completada
                                .andExpect(content().string(not(containsString("Lavar coche"))))
                                // Pero sí "Renovar DNI" que sigue pendiente
                                .andExpect(content().string(containsString("Renovar DNI")));
        }

        @Test
        public void listadoTareasMuestraBorradas() throws Exception {
                // GIVEN
                Map<String, Long> ids = addUsuarioTareasBD();
                Long usuarioId = ids.get("usuarioId");
                Long tareaId = ids.get("tareaId");

                tareaService.borraTarea(tareaId);
                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                // WHEN
                String url = "/usuarios/" + usuarioId + "/tareas/borradas";

                this.mockMvc.perform(get(url))
                                .andExpect(status().isOk())
                                .andExpect(view().name("tareasBorradas"))
                                .andExpect(content().string(containsString("Lavar coche")));
        }

        @Test
        public void restaurarTareaRedirigeYLaMueveAListaPrincipal() throws Exception {
                // GIVEN
                Map<String, Long> ids = addUsuarioTareasBD();
                Long usuarioId = ids.get("usuarioId");
                Long tareaId = ids.get("tareaId");

                tareaService.borraTarea(tareaId);
                when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

                // WHEN: Restauramos
                String urlRestaurar = "/tareas/" + tareaId + "/restaurar";
                this.mockMvc.perform(post(urlRestaurar))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas/borradas"));

                // THEN: Vuelve a estar en la lista principal
                this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas"))
                                .andExpect(content().string(containsString("Lavar coche")));
        }
}