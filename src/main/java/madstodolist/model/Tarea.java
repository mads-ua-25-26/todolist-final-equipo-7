package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.io.Serializable;
import java.util.Objects;
import java.util.HashSet;
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
    @Column(name = "position")
    private Integer position;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @NotNull
    // Relación muchos-a-uno entre tareas y usuario
    @ManyToOne
    // Nombre de la columna en la BD que guarda físicamente
    // el ID del usuario con el que está asociado una tarea
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación muchos a muchos con Etiquetas
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tarea_etiquetas", joinColumns = @JoinColumn(name = "tarea_id"), inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    private Set<Etiqueta> etiquetas = new HashSet<>();

    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Tarea() {
    }

    // Al crear una tarea la asociamos automáticamente a un usuario
    public Tarea(Usuario usuario, String titulo) {
        this.titulo = titulo;
        setUsuario(usuario); // Esto añadirá la tarea a la lista de tareas del usuario
    }

    // Constructor con descripción
    public Tarea(Usuario usuario, String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        setUsuario(usuario); // Esto añadirá la tarea a la lista de tareas del usuario
    }

    // Getters y setters básicos

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

    // Getters y setters de la relación muchos-a-uno con Usuario

    public Usuario getUsuario() {
        return usuario;
    }

    // Getters y setters de Etiquetas
    public Set<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(Set<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas;
    }

    // Método para establecer la relación con el usuario

    public void setUsuario(Usuario usuario) {
        // Comprueba si el usuario ya está establecido
        if (this.usuario != usuario) {
            this.usuario = usuario;
            // Añade la tarea a la lista de tareas del usuario
            usuario.addTarea(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tarea tarea = (Tarea) o;
        if (id != null && tarea.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, tarea.id);
        // si no comparamos por campos obligatorios
        return titulo.equals(tarea.titulo) &&
                usuario.equals(tarea.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo, usuario);
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    // Métodos que ayudan a la entidad Etiqueta
    public void addEtiqueta(Etiqueta etiqueta) {
        // Al ser un Set, si ya existe no la duplica
        this.etiquetas.add(etiqueta);
    }

    public void removeEtiqueta(Etiqueta etiqueta) {
        this.etiquetas.remove(etiqueta);
    }
}
