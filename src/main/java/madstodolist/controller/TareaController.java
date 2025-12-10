package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.TareaServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import madstodolist.model.Etiqueta;
import madstodolist.service.EtiquetaService;
import madstodolist.dto.EtiquetaData;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        TareaData tarea = tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo(), tareaData.getDescripcion(),
                tareaData.getFechaFinalizacion());

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
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "listaTareas";
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

    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    // La anotación @ResponseBody sirve para que la cadena devuelta sea la
    // resupuesta
    // de la petición HTTP, en lugar de una plantilla thymeleaf
    public String borrarTarea(@PathVariable(value = "id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        return "";
    }

    @PostMapping("/tareas/reordenar")
    @ResponseBody
    public ResponseEntity<?> reordenarTareas(@RequestBody Map<String, List<Long>> payload,
            HttpSession session) {
        // Verificar que el usuario está autenticado
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        List<Long> orden = payload.get("orden");

        if (orden == null || orden.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Orden inválido"));
        }

        try {
            tareaService.actualizarOrden(idUsuario, orden);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Orden actualizado correctamente");

            return ResponseEntity.ok(response);

        } catch (TareaServiceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el orden"));
        }
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
