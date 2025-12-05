package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "etiquetas")
public class Etiqueta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    @NotNull
    private String color;

    public Etiqueta() {}

    public Etiqueta(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Etiqueta etiqueta = (Etiqueta) o;
        if (id != null && etiqueta.id != null)
            return Objects.equals(id, etiqueta.id);
        return Objects.equals(nombre, etiqueta.nombre) &&
                Objects.equals(color, etiqueta.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, color);
    }
}