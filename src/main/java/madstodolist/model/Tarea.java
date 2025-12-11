package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tareas")
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String titulo;

    private String descripcion;

    private Integer position;

    // Relación con Usuario (muchas tareas pertenecen a un usuario)
    @NotNull
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación recursiva: una tarea puede tener subtareas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_padre_id")
    private Tarea tareaPadre;

    // Relación recursiva: una tarea puede ser padre de muchas subtareas
    @OneToMany(mappedBy = "tareaPadre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Tarea> subtareas = new HashSet<>();

    // Constructor vacío necesario para JPA
    public Tarea() {}

    // Constructor para tarea raíz (sin padre)
    public Tarea(Usuario usuario, String titulo) {
        this.usuario = usuario;
        this.titulo = titulo;
        usuario.getTareas().add(this);
    }

    // Constructor con descripción
    public Tarea(Usuario usuario, String titulo, String descripcion) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        usuario.getTareas().add(this);
    }

    // Constructor para subtarea
    public Tarea(Tarea tareaPadre, String titulo) {
        this.tareaPadre = tareaPadre;
        this.usuario = tareaPadre.getUsuario(); // Heredar el usuario del padre
        this.titulo = titulo;
        tareaPadre.getSubtareas().add(this);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Tarea getTareaPadre() {
        return tareaPadre;
    }

    public void setTareaPadre(Tarea tareaPadre) {
        this.tareaPadre = tareaPadre;
    }

    public Set<Tarea> getSubtareas() {
        return subtareas;
    }

    public void setSubtareas(Set<Tarea> subtareas) {
        this.subtareas = subtareas;
    }

    // Método auxiliar para añadir una subtarea
    public void addSubtarea(Tarea subtarea) {
        subtareas.add(subtarea);
        subtarea.setTareaPadre(this);
        subtarea.setUsuario(this.usuario); // Asegurar que hereda el usuario
    }

    // Método auxiliar para eliminar una subtarea
    public void removeSubtarea(Tarea subtarea) {
        subtareas.remove(subtarea);
        subtarea.setTareaPadre(null);
    }

    // Método para verificar si es una tarea raíz (no tiene padre)
    public boolean esRaiz() {
        return tareaPadre == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        if (id != null && tarea.id != null)
            return Objects.equals(id, tarea.id);
        // Si no tienen ID, comparar por título y usuario
        return Objects.equals(titulo, tarea.titulo) &&
                Objects.equals(usuario, tarea.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo);
    }
}