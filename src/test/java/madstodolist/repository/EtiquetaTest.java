package madstodolist.repository;

import madstodolist.model.Etiqueta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EtiquetaTest {

    @Autowired
    EtiquetaRepository etiquetaRepository;

    @Test
    public void crearEtiqueta() {
        // Una etiqueta nueva en memoria
        Etiqueta etiqueta = new Etiqueta("Trabajo", "red");

        // Los valores son correctos
        assertThat(etiqueta.getNombre()).isEqualTo("Trabajo");
        assertThat(etiqueta.getColor()).isEqualTo("red");
    }

    @Test
    @Transactional
    public void guardarEtiquetaEnBaseDatos() {
        // Una etiqueta nueva
        Etiqueta etiqueta = new Etiqueta("Urgente", "orange");

        // La guardamos en la BD
        etiquetaRepository.save(etiqueta);

        // Se le asigna un ID
        assertThat(etiqueta.getId()).isNotNull();

        // Y podemos recuperarla
        Etiqueta etiquetaBD = etiquetaRepository.findById(etiqueta.getId()).orElse(null);
        assertThat(etiquetaBD).isNotNull();
        assertThat(etiquetaBD.getNombre()).isEqualTo("Urgente");
        assertThat(etiquetaBD.getColor()).isEqualTo("orange");
    }

    @Test
    public void comprobarIgualdadEtiquetas() {
        // GIVEN
        Etiqueta e1 = new Etiqueta("Java", "blue");
        Etiqueta e2 = new Etiqueta("Java", "blue");
        Etiqueta e3 = new Etiqueta("Python", "green");

        // Sin ID, son iguales si tienen mismo nombre y color
        assertThat(e1).isEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
    }
}