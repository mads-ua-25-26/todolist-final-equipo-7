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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // Crear una nueva tarea raíz para un usuario
    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea, String descripcionTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }

        // Obtener la última posición para asignar a la nueva tarea (solo tareas raíz)
        List<Tarea> tareasRaiz = tareaRepository.findTareasRaizByUsuarioId(idUsuario);
        int nuevaPosicion = tareasRaiz.isEmpty() ? 1 :
                tareasRaiz.stream()
                        .mapToInt(t -> t.getPosition() != null ? t.getPosition() : 0)
                        .max()
                        .orElse(0) + 1;

        Tarea tarea = new Tarea(usuario, tituloTarea, descripcionTarea);
        tarea.setPosition(nuevaPosicion);
        tareaRepository.save(tarea);
        return convertirATareaData(tarea);
    }

    // Crear una nueva subtarea para una tarea existente
    @Transactional
    public TareaData nuevaSubtarea(Long idTareaPadre, String tituloSubtarea) {
        logger.debug("Añadiendo subtarea '" + tituloSubtarea + "' a la tarea " + idTareaPadre);

        Tarea tareaPadre = tareaRepository.findById(idTareaPadre)
                .orElseThrow(() -> new TareaServiceException("Tarea padre no encontrada: " + idTareaPadre));

        Tarea subtarea = new Tarea(tareaPadre, tituloSubtarea);
        tareaRepository.save(subtarea);

        return convertirATareaData(subtarea);
    }

    // Listar todas las tareas raíz de un usuario (sin subtareas en la lista principal)
    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        List<Tarea> tareasRaiz = tareaRepository.findTareasRaizByUsuarioId(idUsuario);

        // Inicializar posiciones si no existen
        inicializarPosicionesSiNecesario(tareasRaiz);

        // Ordenar por position
        tareasRaiz.sort((t1, t2) -> {
            Integer pos1 = t1.getPosition() != null ? t1.getPosition() : Integer.MAX_VALUE;
            Integer pos2 = t2.getPosition() != null ? t2.getPosition() : Integer.MAX_VALUE;
            int comparacion = pos1.compareTo(pos2);

            if (comparacion == 0) {
                return t1.getId().compareTo(t2.getId());
            }
            return comparacion;
        });

        // Convertir a TareaData (incluyendo sus subtareas)
        return tareasRaiz.stream()
                .map(this::convertirATareaData)
                .collect(Collectors.toList());
    }

    // Obtener las subtareas de una tarea específica
    @Transactional(readOnly = true)
    public List<TareaData> obtenerSubtareas(Long idTarea) {
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaServiceException("Tarea no encontrada"));

        return tarea.getSubtareas().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(this::convertirATareaData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        return convertirATareaData(tarea);
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
        return convertirATareaData(tarea);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        // Al tener cascade = ALL y orphanRemoval = true, las subtareas se borrarán automáticamente
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
        // Verificar que todas las tareas pertenecen al usuario y son tareas raíz
        for (int i = 0; i < orden.size(); i++) {
            Long tareaId = orden.get(i);
            Tarea tarea = tareaRepository.findById(tareaId)
                    .orElseThrow(() -> new TareaServiceException("Tarea no encontrada: " + tareaId));

            // Verificar permisos
            if (!tarea.getUsuario().getId().equals(idUsuario)) {
                throw new TareaServiceException("No tienes permiso para modificar esta tarea");
            }

            // Verificar que es una tarea raíz
            if (!tarea.esRaiz()) {
                throw new TareaServiceException("Solo se pueden reordenar tareas raíz");
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

    // Método auxiliar para convertir Tarea a TareaData (incluyendo subtareas)
    private TareaData convertirATareaData(Tarea tarea) {
        TareaData tareaData = new TareaData();
        tareaData.setId(tarea.getId());
        tareaData.setTitulo(tarea.getTitulo());
        tareaData.setDescripcion(tarea.getDescripcion());
        tareaData.setUsuarioId(tarea.getUsuario().getId());
        tareaData.setPosition(tarea.getPosition());
        tareaData.setTareaPadreId(tarea.getTareaPadre() != null ? tarea.getTareaPadre().getId() : null);

        // Convertir subtareas recursivamente
        if (tarea.getSubtareas() != null && !tarea.getSubtareas().isEmpty()) {
            List<TareaData> subtareasData = tarea.getSubtareas().stream()
                    .sorted((a, b) -> a.getId().compareTo(b.getId()))
                    .map(this::convertirATareaData)
                    .collect(Collectors.toList());
            tareaData.setSubtareas(subtareasData);
        } else {
            tareaData.setSubtareas(new ArrayList<>());
        }

        return tareaData;
    }

    // Añadir este método a TareaService.java

    @Transactional
    public void actualizarOrdenDesdeMap(Long usuarioId, Map<String, List<Long>> mapaOrden) {
        // Iteramos sobre las claves (ej: "orden_root", "orden_15", etc.)
        for (Map.Entry<String, List<Long>> entry : mapaOrden.entrySet()) {
            String key = entry.getKey();
            List<Long> ids = entry.getValue();

            if (ids == null || ids.isEmpty()) continue;

            if (key.equals("orden_root")) {
                // Reordenar tareas raíz
                actualizarOrden(usuarioId, ids);
            } else if (key.startsWith("orden_")) {
                // Es una lista de subtareas (ej: "orden_15")
                try {
                    Long idPadre = Long.parseLong(key.replace("orden_", ""));
                    actualizarOrdenSubtareas(idPadre, ids);
                } catch (NumberFormatException e) {
                    // Ignorar claves mal formadas
                }
            }
        }
    }

    // Método auxiliar (asegúrate de tenerlo o adáptalo del paso anterior)
    @Transactional
    public void actualizarOrdenSubtareas(Long idTareaPadre, List<Long> ordenSubtareas) {
        // Verificar que la tarea padre existe
        Tarea padre = tareaRepository.findById(idTareaPadre).orElse(null);
        if (padre == null) return;

        for (int i = 0; i < ordenSubtareas.size(); i++) {
            Long subId = ordenSubtareas.get(i);
            Tarea subtarea = tareaRepository.findById(subId).orElse(null);

            // Validaciones de seguridad
            if (subtarea != null && subtarea.getTareaPadre() != null
                    && subtarea.getTareaPadre().getId().equals(idTareaPadre)) {
                subtarea.setPosition(i + 1);
                tareaRepository.save(subtarea);
            }
        }
    }
}