package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuariosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void listadoUsuariosRegistrados() throws Exception {
        // GIVEN
        // Moqueamos el método allUsuarios para que devuelva una lista de usuarios

        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(1L);
        usuario1.setEmail("ana.garcia@gmail.com");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setEmail("pepito.perez@gmail.com");

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(usuario1);
        usuarios.add(usuario2);

        when(usuarioService.allUsuarios()).thenReturn(usuarios);

        // WHEN, THEN
        // Realizamos una petición GET a /registrados
        // y verificamos que aparecen los IDs y emails de los usuarios

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("ana.garcia@gmail.com"),
                        containsString("pepito.perez@gmail.com"),
                        containsString("1"),
                        containsString("2")
                )));
    }

    @Test
    public void listadoUsuariosVacio() throws Exception {
        // GIVEN
        // Moqueamos el método allUsuarios para que devuelva una lista vacía

        List<UsuarioData> usuarios = new ArrayList<>();
        when(usuarioService.allUsuarios()).thenReturn(usuarios);

        // WHEN, THEN
        // Realizamos una petición GET a /registrados
        // y verificamos que aparece el mensaje de que no hay usuarios

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay usuarios registrados")));
    }
}