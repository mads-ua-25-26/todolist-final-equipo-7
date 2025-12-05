package madstodolist.service;

import madstodolist.model.Etiqueta;
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

    @Test
    public void crearYRecuperarEtiquetas() {
        // WHEN
        etiquetaService.crearEtiqueta("Prioridad Alta", "red");
        etiquetaService.crearEtiqueta("Fácil", "green");

        // THEN
        List<Etiqueta> etiquetas = etiquetaService.findAll();
        assertThat(etiquetas).hasSize(2);
        assertThat(etiquetas.get(0).getNombre()).isEqualTo("Prioridad Alta");
    }
}