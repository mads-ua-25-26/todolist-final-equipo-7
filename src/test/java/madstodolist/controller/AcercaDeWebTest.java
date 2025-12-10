package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AcercaDeWebTest {

        @Autowired
        private MockMvc mockMvc;
        @MockBean
        private UsuarioService usuarioService;

        @Test
        public void getAboutDevuelveNombreAplicacion() throws Exception {
                this.mockMvc.perform(get("/about"))
                                .andExpect(content().string(containsString("ToDoList")));
        }

        @Test
        public void getAboutDevuelveNombreAplicacionWithUserLogged() throws Exception {
                UsuarioData anaGarcia = new UsuarioData();
                anaGarcia.setNombre("Ana García");
                anaGarcia.setId(1L);

                when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
                when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                                .thenReturn(anaGarcia);
                this.mockMvc.perform(get("/about"))
                                .andExpect(content().string(containsString("ToDoList")));
        }

        @Test
        public void getLandingPageReturnsContent() throws Exception {
                this.mockMvc.perform(get("/"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Deja de olvidar cosas")));
        }
}
