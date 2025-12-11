package madstodolist.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Data Transfer Object para la clase Tarea
public class TareaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private String descripcion;
    private Long usuarioId;
    private Integer position;
    private Long tareaPadreId; // ID de la tarea padre (null si es raíz)
    private List<TareaData> subtareas; // Lista de subtareas

    // Constructor vacío
    public TareaData() {
        this.subtareas = new ArrayList<>();
    }

    // Getters y setters
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Long getTareaPadreId() {
        return tareaPadreId;
    }

    public void setTareaPadreId(Long tareaPadreId) {
        this.tareaPadreId = tareaPadreId;
    }

    public List<TareaData> getSubtareas() {
        return subtareas;
    }

    public void setSubtareas(List<TareaData> subtareas) {
        this.subtareas = subtareas;
    }

    // Método auxiliar para verificar si es una tarea raíz
    public boolean esRaiz() {
        return tareaPadreId == null;
    }

    // Sobreescribimos equals y hashCode para que dos tareas sean iguales
    // si tienen el mismo ID (ignoramos el resto de atributos)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TareaData)) return false;
        TareaData tareaData = (TareaData) o;
        return Objects.equals(id, tareaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}