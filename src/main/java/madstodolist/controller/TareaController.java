package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import madstodolist.model.Etiqueta;
import madstodolist.service.EtiquetaService;
import madstodolist.dto.EtiquetaData;
import madstodolist.service.TareaServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TareaController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    EtiquetaService etiquetaService;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }

    private Map<String, String> obtenerColores() {
        Map<String, String> colores = new HashMap<>();
        colores.put("Gris", "#6c757d");
        colores.put("Marrón", "#795548");
        colores.put("Naranja", "#fd7e14");
        colores.put("Amarillo", "#ffc107");
        colores.put("Verde", "#28a745");
        colores.put("Azul", "#007bff");
        colores.put("Violeta", "#6f42c1");
        colores.put("Rosa", "#e83e8c");
        colores.put("Rojo", "#dc3545");
        colores.put("Negro", "#343a40");
        return colores;
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value = "id") Long idUsuario,
            @ModelAttribute TareaData tareaData, Model model,
            HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        List<Etiqueta> etiquetas = etiquetaService.findAllByUsuario(idUsuario);
        model.addAttribute("etiquetas", etiquetas);

        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value = "id") Long idUsuario,
            @ModelAttribute TareaData tareaData,
            @RequestParam(value = "etiquetaIds", required = false) List<Long> etiquetaIds,
            Model model, RedirectAttributes flash,
            HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);

        TareaData tarea = tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo(),
                tareaData.getDescripcion(), tareaData.getFechaFinalizacion());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            tareaService.actualizarEtiquetas(tarea.getId(), etiquetaIds);
        }

        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value = "id") Long idUsuario, Model model, HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);

        List<TareaData> tareas = tareaService.tareasUsuarioPorEstado(idUsuario, false);

        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        model.addAttribute("today", java.time.LocalDate.now());
        model.addAttribute("tomorrow", java.time.LocalDate.now().plusDays(1));
        return "listaTareas";
    }

    // Listado de tareas completadas
    @GetMapping("/usuarios/{id}/tareas/completadas")
    public String listadoTareasCompletadas(@PathVariable(value = "id") Long idUsuario, Model model,
            HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);

        // Pedimos solo las completadas
        List<TareaData> tareas = tareaService.tareasUsuarioPorEstado(idUsuario, true);

        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        // Podemos reutilizar la vista listaTareas o crear una específica.
        // Para empezar reutilizamos, pero pasamos un flag para ocultar botones de
        // edición si quieres.
        model.addAttribute("esListadoCompletadas", true);
        return "listaTareas";
    }

    // Acción de completar tarea
    @PostMapping("/tareas/{id}/completar")
    public String completarTarea(@PathVariable(value = "id") Long idTarea, RedirectAttributes flash) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        comprobarUsuarioLogeado(tarea.getUsuarioId());

        try {
            tareaService.completarTarea(idTarea);
            flash.addFlashAttribute("mensaje", "Tarea completada correctamente");
        } catch (TareaServiceException e) {
            // Si falla (ej: tiene subtareas pendientes), mostramos el error
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value = "id") Long idTarea, @ModelAttribute TareaData tareaData,
            Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        comprobarUsuarioLogeado(tarea.getUsuarioId());
        model.addAttribute("tarea", tarea);
        model.addAttribute("etiquetas", etiquetaService.findAllByUsuario(tarea.getUsuarioId()));

        tareaData.setTitulo(tarea.getTitulo());
        tareaData.setDescripcion(tarea.getDescripcion());
        tareaData.setFechaFinalizacion(tarea.getFechaFinalizacion());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value = "id") Long idTarea,
            @ModelAttribute TareaData tareaData,
            @RequestParam(value = "etiquetaIds", required = false) List<Long> etiquetaIds,
            Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        Long idUsuario = tarea.getUsuarioId();
        comprobarUsuarioLogeado(idUsuario);

        tareaService.modificaTarea(idTarea, tareaData.getTitulo(), tareaData.getDescripcion(),
                tareaData.getFechaFinalizacion());

        if (etiquetaIds != null) {
            tareaService.actualizarEtiquetas(idTarea, etiquetaIds);
        }

        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    // Endpoint DELETE para compatibilidad con tests antiguos si existen
    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    public String borrarTarea(@PathVariable(value = "id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null)
            throw new TareaNotFoundException();
        comprobarUsuarioLogeado(tarea.getUsuarioId());
        tareaService.borraTarea(idTarea);
        return "";
    }

    // Endpoint POST para la web (sin JS)
    @PostMapping("/tareas/{id}/borrar")
    public String borrarTareaPost(@PathVariable(value = "id") Long idTarea, RedirectAttributes flash) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea != null) {
            comprobarUsuarioLogeado(tarea.getUsuarioId());
            tareaService.borraTarea(idTarea);
            flash.addFlashAttribute("mensaje", "Tarea borrada");
            return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
        }
        return "redirect:/login";
    }

    // Listar tareas borradas (Papelera)
    @GetMapping("/usuarios/{id}/tareas/borradas")
    public String listadoTareasBorradas(@PathVariable(value = "id") Long idUsuario, Model model, HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);

        List<TareaData> tareas = tareaService.allTareasBorradasUsuario(idUsuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        // Usaremos una nueva plantilla 'tareasBorradas'
        return "tareasBorradas";
    }

    // Acción de restaurar tarea
    @PostMapping("/tareas/{id}/restaurar")
    public String restaurarTarea(@PathVariable(value = "id") Long idTarea, RedirectAttributes flash) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.restaurarTarea(idTarea);
        flash.addFlashAttribute("mensaje", "Tarea restaurada correctamente");

        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas/borradas";
    }

    @PostMapping("/tareas/restaurar-batch")
    public String restaurarTareasBatch(@RequestParam("ids") List<Long> ids, RedirectAttributes flash) {
        if (ids == null || ids.isEmpty()) {
            return "redirect:/login"; // O manejar error
        }

        // Comprobar usuario de la primera tarea (asumimos todas del mismo usuario
        // contextualmente)
        TareaData primera = tareaService.findById(ids.get(0));
        if (primera != null) {
            comprobarUsuarioLogeado(primera.getUsuarioId());
            tareaService.restaurarTareas(ids);
            flash.addFlashAttribute("mensaje", "Tareas restauradas correctamente");
            return "redirect:/usuarios/" + primera.getUsuarioId() + "/tareas/borradas";
        }
        return "redirect:/login";
    }

    @PostMapping("/tareas/borrar-definitivamente-batch")
    public String borrarTareasDefinitivamenteBatch(@RequestParam("ids") List<Long> ids, RedirectAttributes flash) {
        if (ids == null || ids.isEmpty()) {
            return "redirect:/login";
        }

        TareaData primera = tareaService.findById(ids.get(0));
        if (primera != null) {
            comprobarUsuarioLogeado(primera.getUsuarioId());
            tareaService.borrarTareasDefinitivamente(ids);
            flash.addFlashAttribute("mensaje", "Tareas eliminadas definitivamente");
            return "redirect:/usuarios/" + primera.getUsuarioId() + "/tareas/borradas";
        }
        return "redirect:/login";
    }

    @PostMapping("/tareas/guardarOrden")
    public String guardarOrdenGlobal(HttpServletRequest request, RedirectAttributes flash) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null)
            return "redirect:/login";

        Map<String, List<Long>> mapaOrden = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (String key : parameterMap.keySet()) {
            if (key.startsWith("orden_")) {
                List<Long> ids = Arrays.stream(parameterMap.get(key))
                        .map(s -> {
                            try {
                                return Long.parseLong(s);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                mapaOrden.put(key, ids);
            }
        }
        tareaService.actualizarOrdenDesdeMap(usuarioId, mapaOrden);
        flash.addFlashAttribute("mensaje", "Orden actualizado correctamente");
        return "redirect:/usuarios/" + usuarioId + "/tareas";
    }

    @PostMapping("/tareas/{id}/subtareas/nueva")
    public String crearSubtarea(@PathVariable(value = "id") Long idTareaPadre,
            @RequestParam("titulo") String titulo,
            RedirectAttributes flash) {
        TareaData padre = tareaService.findById(idTareaPadre);
        if (padre != null) {
            comprobarUsuarioLogeado(padre.getUsuarioId());
            tareaService.nuevaSubtarea(idTareaPadre, titulo);
            flash.addFlashAttribute("mensaje", "Subtarea añadida");
            return "redirect:/usuarios/" + padre.getUsuarioId() + "/tareas";
        }
        return "redirect:/login";
    }

    @GetMapping("/usuarios/{id}/etiquetas")
    public String editarEtiquetas(@PathVariable(value = "id") Long idUsuario, Model model) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);

        List<Etiqueta> etiquetas = etiquetaService.findAllByUsuario(idUsuario);
        EtiquetaData etiquetaData = new EtiquetaData();
        etiquetaData.setEtiquetas(etiquetas);

        model.addAttribute("usuario", usuario);
        model.addAttribute("etiquetaData", etiquetaData);
        model.addAttribute("colores", obtenerColores());

        return "formEditarEtiquetas";
    }

    @PostMapping("/usuarios/{id}/etiquetas")
    public String guardarEtiquetas(@PathVariable(value = "id") Long idUsuario,
            @ModelAttribute EtiquetaData data,
            RedirectAttributes flash) {
        comprobarUsuarioLogeado(idUsuario);

        etiquetaService.guardarEtiquetas(idUsuario, data.getEtiquetas());

        flash.addFlashAttribute("mensaje", "Etiquetas actualizadas correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }
}