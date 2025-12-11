package madstodolist.repository;

import madstodolist.model.Etiqueta;
import madstodolist.model.Usuario; // Import necesario
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

    @Autowired
    UsuarioRepository usuarioRepository; // Necesitamos esto para guardar el usuario

    @Test
    public void crearEtiqueta() {
        // GIVEN: Creamos un usuario dummy (no hace falta guardarlo en BD para este test unitario básico)
        Usuario usuario = new Usuario("test@ua.es");

        // WHEN: Creamos la etiqueta pasándole el usuario
        Etiqueta etiqueta = new Etiqueta(usuario, "Trabajo", "red");

        // THEN
        assertThat(etiqueta.getNombre()).isEqualTo("Trabajo");
        assertThat(etiqueta.getColor()).isEqualTo("red");
        assertThat(etiqueta.getUsuario()).isEqualTo(usuario);
    }

    @Test
    @Transactional
    public void guardarEtiquetaEnBaseDatos() {
        // GIVEN: Un usuario guardado en BD
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        // WHEN: Creamos la etiqueta vinculada al usuario y guardamos
        Etiqueta etiqueta = new Etiqueta(usuario, "Urgente", "orange");
        etiquetaRepository.save(etiqueta);

        // THEN
        assertThat(etiqueta.getId()).isNotNull();

        // Y podemos recuperarla
        Etiqueta etiquetaBD = etiquetaRepository.findById(etiqueta.getId()).orElse(null);
        assertThat(etiquetaBD).isNotNull();
        assertThat(etiquetaBD.getNombre()).isEqualTo("Urgente");
        assertThat(etiquetaBD.getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    public void comprobarIgualdadEtiquetas() {
        // GIVEN
        Usuario usuario = new Usuario("test@ua");

        Etiqueta e1 = new Etiqueta(usuario, "Java", "blue");
        Etiqueta e2 = new Etiqueta(usuario, "Java", "blue");
        Etiqueta e3 = new Etiqueta(usuario, "Python", "green");

        // Sin ID, son iguales si tienen mismo nombre, color Y usuario
        assertThat(e1).isEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
    }
}