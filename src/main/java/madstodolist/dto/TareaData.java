package madstodolist.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import madstodolist.model.Etiqueta;

public class TareaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private String descripcion;
    private Long usuarioId;
    private Integer position;
    private Set<Etiqueta> etiquetas;
    private Boolean completada;
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate fechaFinalizacion;

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

    public Set<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(Set<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas;
    }

    public java.time.LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(java.time.LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
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

    public Boolean getCompletada() {
        return completada;
    }

    public void setCompletada(Boolean completada) {
        this.completada = completada;
    }

    // Método auxiliar para verificar si es una tarea raíz
    public boolean esRaiz() {
        return tareaPadreId == null;
    }

    // Sobreescribimos equals y hashCode para que dos tareas sean iguales
    // si tienen el mismo ID (ignoramos el resto de atributos)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TareaData))
            return false;
        TareaData tareaData = (TareaData) o;
        return Objects.equals(id, tareaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}