package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquiposController {
    @Autowired
    EquipoService equipoService;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/equipos")
    public String listadoDeEquipos(Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        model.addAttribute("usuario", usuario);

        List<EquipoData> equipoDataList = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipoDataList);
        return "listadoEquipos";
    }

    @GetMapping("/equipos/{id}/usuarios")
    public String listadoDeUsuariosDeEquipo(@PathVariable Long id, Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        EquipoData equipo = equipoService.recuperarEquipo(id);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(id);

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        return "equipoUsuarios";
    }

    @GetMapping("/equipos/{idEquipo}/usuarios/{idUsuario}/agregar")
    public String agregarUsuarioAEquipo(@PathVariable Long idEquipo, @PathVariable Long idUsuario, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);
        return "redirect:/equipos";
    }

    @GetMapping("/equipos/nuevo")
    public String formularioNuevoEquipo(Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        model.addAttribute("equipo", new EquipoData());
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipo(@ModelAttribute EquipoData equipoData, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        equipoService.crearEquipo(equipoData.getNombre());
        return "redirect:/equipos";
    }

    @GetMapping("/equipos/{idEquipo}/usuarios/{idUsuario}/eliminar")
    public String eliminarUsuarioDeEquipo(@PathVariable Long idEquipo, @PathVariable Long idUsuario, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        equipoService.eliminarUsuarioDeEquipo(idEquipo, idUsuario);
        return "redirect:/equipos/" + idEquipo + "/usuarios";
    }

    @GetMapping("/equipos/{idEquipo}/editar")
    public String formularioEditarEquipo(@PathVariable Long idEquipo, Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null || idEquipo == null) {
            return "redirect:/login";
        }
        if(!usuarioService.findById(idUsuarioLogeado).getAdmin()) {
            return "redirect:/equipos";
        }
        EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
        model.addAttribute("equipo", equipo);
        return "formEditarEquipo";
    }

    @PostMapping("/equipos/{idEquipo}/editar")
    public String editarEquipo(@PathVariable Long idEquipo, @ModelAttribute EquipoData equipoData, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }
        if(!usuarioService.findById(idUsuarioLogeado).getAdmin()) {
            return "redirect:/equipos";
        }

        equipoService.editarNombreEquipo(idEquipo, equipoData.getNombre());
        return "redirect:/equipos";
    }
}