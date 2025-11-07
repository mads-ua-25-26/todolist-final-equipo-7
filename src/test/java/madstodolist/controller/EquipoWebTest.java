package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de los equipos añadidos
    Map<String, Long> addUsuarioEquiposBD() {
        // Añadimos un usuario a la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // Añadimos dos equipos
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto A");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto B");

        // Añadimos el usuario al primer equipo
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());

        // Devolvemos los ids del usuario y de los equipos
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("equipo1Id", equipo1.getId());
        ids.put("equipo2Id", equipo2.getId());
        return ids;
    }

    @Test
    public void listaEquipos() throws Exception {
        // GIVEN
        // Un usuario y dos equipos en la BD
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");

        // Moqueamos el método usuarioLogeado para que devuelva el usuario
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET al listado de equipos,
        // el HTML devuelto contiene los nombres de los equipos.

        String url = "/equipos";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Proyecto A"),
                        containsString("Proyecto B")
                )));
    }

    @Test
    public void getEquipoMuestraUsuarios() throws Exception {
        // GIVEN
        // Un usuario añadido a un equipo
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo1Id = ids.get("equipo1Id");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET para ver los usuarios de un equipo,
        // el HTML devuelto contiene el email del usuario

        String url = "/equipos/" + equipo1Id + "/usuarios";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("user@ua"),
                        containsString("Proyecto A")
                )));
    }

    @Test
    public void getEquipoSinUsuariosMuestraMensaje() throws Exception {
        // GIVEN
        // Un equipo sin usuarios
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo2Id = ids.get("equipo2Id");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET para ver los usuarios de un equipo vacío,
        // el HTML muestra el mensaje de que no hay usuarios

        String url = "/equipos/" + equipo2Id + "/usuarios";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay usuarios en este equipo")));
    }

    @Test
    public void getNuevoEquipoDevuelveForm() throws Exception {
        // GIVEN
        // Un usuario logueado
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // si ejecutamos una petición GET para crear un nuevo equipo,
        // el HTML resultante contiene un formulario y la ruta con
        // la acción para crear el nuevo equipo.

        String urlPeticion = "/equipos/nuevo";
        String urlAction = "action=\"/equipos/nuevo\"";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                )));
    }

    @Test
    public void postNuevoEquipoDevuelveRedirectYAñadeEquipo() throws Exception {
        // GIVEN
        // Un usuario logueado
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición POST para añadir un nuevo equipo,
        // el estado HTTP que se devuelve es un REDIRECT al listado
        // de equipos.

        String urlPost = "/equipos/nuevo";
        String urlRedirect = "/equipos";

        this.mockMvc.perform(post(urlPost)
                        .param("nombre", "Proyecto C"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y si después consultamos el listado de equipos con una petición
        // GET el HTML contiene el equipo añadido.

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("Proyecto C")));
    }

    @Test
    public void agregarUsuarioAEquipoDevuelveRedirect() throws Exception {
        // GIVEN
        // Un usuario y un equipo al que no pertenece
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo2Id = ids.get("equipo2Id");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición GET para añadir el usuario al equipo,
        // se devuelve un REDIRECT

        String urlAgregar = "/equipos/" + equipo2Id + "/usuarios/" + usuarioId + "/agregar";
        String urlRedirect = "/equipos";

        this.mockMvc.perform(get(urlAgregar))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y cuando consultamos los usuarios del equipo, aparece el usuario

        String urlUsuarios = "/equipos/" + equipo2Id + "/usuarios";

        this.mockMvc.perform(get(urlUsuarios))
                .andExpect(content().string(containsString("user@ua")));
    }

    @Test
    public void eliminarUsuarioDeEquipoDevuelveRedirect() throws Exception {
        // GIVEN
        // Un usuario añadido a un equipo
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo1Id = ids.get("equipo1Id");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición GET para eliminar el usuario del equipo,
        // se devuelve un REDIRECT

        String urlEliminar = "/equipos/" + equipo1Id + "/usuarios/" + usuarioId + "/eliminar";
        String urlRedirect = "/equipos/" + equipo1Id + "/usuarios";

        this.mockMvc.perform(get(urlEliminar))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y cuando consultamos los usuarios del equipo, aparece el mensaje
        // de que no hay usuarios

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("No hay usuarios en este equipo")));
    }

    @Test
    public void listaEquiposOrdenadaPorNombre() throws Exception {
        // GIVEN
        // Varios equipos con nombres diferentes
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");

        // Añadimos un equipo con nombre que va antes alfabéticamente
        equipoService.crearEquipo("Alpha Team");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET al listado de equipos,
        // los equipos aparecen ordenados alfabéticamente

        String url = "/equipos";
        String responseContent = this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verificamos que "Alpha Team" aparece antes que "Proyecto A"
        int posAlpha = responseContent.indexOf("Alpha Team");
        int posProyecto = responseContent.indexOf("Proyecto A");

        assert(posAlpha < posProyecto);
    }

    @Test
    public void accederAEquiposSinLoguearDevuelveRedirectALogin() throws Exception {
        // GIVEN
        // No hay usuario logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // intentamos acceder al listado de equipos,
        // se redirige a la página de login

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void accederAUsuariosDeEquipoSinLoguearDevuelveRedirectALogin() throws Exception {
        // GIVEN
        // Un equipo en la BD pero no hay usuario logueado
        Long equipo1Id = addUsuarioEquiposBD().get("equipo1Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // intentamos acceder a los usuarios del equipo,
        // se redirige a la página de login

        String url = "/equipos/" + equipo1Id + "/usuarios";

        this.mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void crearEquipoSinLoguearDevuelveRedirectALogin() throws Exception {
        // GIVEN
        // No hay usuario logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // intentamos crear un equipo,
        // se redirige a la página de login

        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Equipo Nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void editarEquipoUsuarioAdminCambiaElNombre() throws Exception {
        // Given
        UsuarioData admin = new UsuarioData();
        admin.setNombre("admin");
        admin.setPassword("admin");
        admin.setEmail("admin@admin.com");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        EquipoData equipo = equipoService.crearEquipo("Equipo Original");
        Long equipoId = equipo.getId();  // Guardar el ID

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());

        // When - Llamar al endpoint de editar con POST
        String nuevoNombre = "Equipo Modificado";
        mockMvc.perform(post("/equipos/{idEquipo}/editar", equipoId)
                        .param("nombre", nuevoNombre))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        // Then - Recuperar de nuevo desde BD (esto fuerza una nueva consulta)
        EquipoData equipoEditado = equipoService.recuperarEquipo(equipoId);
        assertEquals(nuevoNombre, equipoEditado.getNombre());
    }

}