package madstodolist.service;

import madstodolist.model.Etiqueta;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EtiquetaServiceTest {

    @Autowired
    EtiquetaService etiquetaService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test
    public void crearYRecuperarEtiquetasPersonales() {
        // GIVEN
        Usuario usuario = new Usuario("test@ua.es");
        usuario.setPassword("123");
        usuario = usuarioRepository.save(usuario);

        // WHEN
        etiquetaService.crearEtiqueta(usuario.getId(), "Universidad", "blue");
        etiquetaService.crearEtiqueta(usuario.getId(), "Gimnasio", "red");

        // THEN
        List<Etiqueta> etiquetas = etiquetaService.findAllByUsuario(usuario.getId());
        assertThat(etiquetas).hasSize(2);
        assertThat(etiquetas.get(0).getUsuario().getId()).isEqualTo(usuario.getId());
    }
}