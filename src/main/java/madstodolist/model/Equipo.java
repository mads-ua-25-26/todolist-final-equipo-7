package madstodolist.model;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    // Declaramos el tipo de recuperación como LAZY.
    // No haría falta porque es el tipo por defecto en una
    // relación a muchos.
    // Al recuperar un equipo NO SE RECUPERA AUTOMÁTICAMENTE
    // la lista de usuarios. Sólo se recupera cuando se accede al
    // atributo 'usuarios'; entonces se genera una query en la
    // BD que devuelve todos los usuarios del equipo y rellena el
    // atributo.

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "equipo_usuario",                             // Define el nombre que tendra la tabla intermedia
        joinColumns = {@JoinColumn(name = "fk_equipo") },           // Apunta a la parte de la tabla donde estamos
        inverseJoinColumns = {@JoinColumn(name = "fk_usuario")})    // Apunta a la otra tabla, ambas a la fk, pues estamos creando una tabla nueva
    Set<Usuario> usuarios = new HashSet<>();

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void addUsuario(Usuario usuario) {
        // Hay que actualizar ambas colecciones, porque
        // JPA/Hibernate no lo hace automaticamente
        this.getUsuarios().add(usuario);
        usuario.getEquipos().add(this);
    }

    public void removeUsuario(Usuario usuario) {
        this.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(this);
    }


    public Equipo(){
        this.nombre = "";
        this.id = null;
    }
    public Equipo(String proyectoP1) {
        this.nombre = proyectoP1;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(long l) {
        this.id = l;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if (id != null && equipo.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // si no comparamos por campos obligatorios
        return nombre.equals(equipo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
