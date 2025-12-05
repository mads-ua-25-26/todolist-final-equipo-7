package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.repository.TareaRepository;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.dto.TareaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea, String descripcionTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }

        // Obtener la última posición para asignar a la nueva tarea
        List<Tarea> tareasExistentes = tareaRepository.findByUsuarioId(idUsuario);
        int nuevaPosicion = tareasExistentes.isEmpty() ? 1 :
                tareasExistentes.stream()
                        .mapToInt(t -> t.getPosition() != null ? t.getPosition() : 0)
                        .max()
                        .orElse(0) + 1;

        Tarea tarea = new Tarea(usuario, tituloTarea, descripcionTarea);
        tarea.setPosition(nuevaPosicion);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    // Método para listar tareas ordenadas por position
    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        // Obtener las entidades Tarea
        List<Tarea> tareas = tareaRepository.findByUsuarioId(idUsuario);

        // Inicializar posiciones si no existen
        inicializarPosicionesSiNecesario(tareas);

        // Ordenar por position
        tareas.sort((t1, t2) -> {
            Integer pos1 = t1.getPosition() != null ? t1.getPosition() : Integer.MAX_VALUE;
            Integer pos2 = t2.getPosition() != null ? t2.getPosition() : Integer.MAX_VALUE;
            int comparacion = pos1.compareTo(pos2);

            // Si tienen la misma posición, ordenar por ID como fallback
            if (comparacion == 0) {
                return t1.getId().compareTo(t2.getId());
            }
            return comparacion;
        });

        // Convertir a TareaData
        return tareas.stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo, String nuevaDescripcion) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
        tarea.setDescripcion(nuevaDescripcion);
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional(readOnly = true)
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }

    @Transactional
    public void actualizarOrden(Long idUsuario, List<Long> orden) {
        // Verificar que todas las tareas pertenecen al usuario
        for (int i = 0; i < orden.size(); i++) {
            Long tareaId = orden.get(i);
            Tarea tarea = tareaRepository.findById(tareaId)
                    .orElseThrow(() -> new TareaServiceException("Tarea no encontrada: " + tareaId));

            // Verificar permisos
            if (!tarea.getUsuario().getId().equals(idUsuario)) {
                throw new TareaServiceException("No tienes permiso para modificar esta tarea");
            }

            // Actualizar la posición (índice + 1 para empezar en 1)
            tarea.setPosition(i + 1);
            tareaRepository.save(tarea);
        }
    }

    @Transactional
    public void inicializarPosicionesSiNecesario(List<Tarea> tareas) {
        boolean necesitaInicializacion = tareas.stream()
                .anyMatch(t -> t.getPosition() == null);

        if (necesitaInicializacion) {
            // Ordenar por ID para dar un orden inicial consistente
            tareas.sort((t1, t2) -> t1.getId().compareTo(t2.getId()));

            for (int i = 0; i < tareas.size(); i++) {
                Tarea tarea = tareas.get(i);
                if (tarea.getPosition() == null) {
                    tarea.setPosition(i + 1);
                    tareaRepository.save(tarea);
                }
            }
        }
    }
}