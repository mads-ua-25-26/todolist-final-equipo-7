package madstodolist.repository;

import madstodolist.model.Tarea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TareaRepository extends CrudRepository<Tarea, Long> {

    // Obtener todas las tareas de un usuario (solo tareas raíz, sin subtareas)
    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.tareaPadre IS NULL")
    List<Tarea> findTareasRaizByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Obtener tareas raíz filtrando por si están completadas o no
    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.tareaPadre IS NULL AND t.completada = :completada")
    List<Tarea> findTareasRaizByUsuarioIdAndCompletada(@Param("usuarioId") Long usuarioId, @Param("completada") Boolean completada);

    // Obtener todas las tareas de un usuario (incluyendo subtareas)
    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId")
    List<Tarea> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Obtener las subtareas de una tarea específica
    @Query("SELECT t FROM Tarea t WHERE t.tareaPadre.id = :tareaId")
    List<Tarea> findSubtareasByTareaId(@Param("tareaId") Long tareaId);
}