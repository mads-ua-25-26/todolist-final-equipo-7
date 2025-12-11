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

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value="id") Long idUsuario,
                                 @ModelAttribute TareaData tareaData, Model model,
                                 HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value="id") Long idUsuario, @ModelAttribute TareaData tareaData,
                             Model model, RedirectAttributes flash,
                             HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo(), tareaData.getDescripcion());
        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "listaTareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        comprobarUsuarioLogeado(tarea.getUsuarioId());
        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        tareaData.setDescripcion(tarea.getDescripcion());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }
        Long idUsuario = tarea.getUsuarioId();
        comprobarUsuarioLogeado(idUsuario);

        tareaService.modificaTarea(idTarea, tareaData.getTitulo(), tareaData.getDescripcion());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    // Endpoint DELETE para compatibilidad con tests antiguos si existen
    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) throw new TareaNotFoundException();
        comprobarUsuarioLogeado(tarea.getUsuarioId());
        tareaService.borraTarea(idTarea);
        return "";
    }

    // Endpoint POST para la web (sin JS)
    @PostMapping("/tareas/{id}/borrar")
    public String borrarTareaPost(@PathVariable(value="id") Long idTarea, RedirectAttributes flash) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea != null) {
            comprobarUsuarioLogeado(tarea.getUsuarioId());
            tareaService.borraTarea(idTarea);
            flash.addFlashAttribute("mensaje", "Tarea borrada");
            return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
        }
        return "redirect:/login";
    }

    @PostMapping("/tareas/guardarOrden")
    public String guardarOrdenGlobal(HttpServletRequest request, RedirectAttributes flash) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        Map<String, List<Long>> mapaOrden = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (String key : parameterMap.keySet()) {
            if (key.startsWith("orden_")) {
                List<Long> ids = Arrays.stream(parameterMap.get(key))
                        .map(s -> {
                            try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
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
    public String crearSubtarea(@PathVariable(value="id") Long idTareaPadre,
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
}