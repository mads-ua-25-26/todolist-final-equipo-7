package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "subtareas")
public class Subtarea implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String texto;

    private Boolean completado = false;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    // Constructor vacío
    public Subtarea() {}

    public Subtarea(Tarea tarea, String texto) {
        this.tarea = tarea;
        this.texto = texto;
        this.completado = false;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }
    public Tarea getTarea() { return tarea; }
    public void setTarea(Tarea tarea) { this.tarea = tarea; }
}