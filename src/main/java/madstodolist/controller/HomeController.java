package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    // Página de "Acerca de" (/about)
    @GetMapping("/about")
    public String aboutPage(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario != null) {
            // Usuario logeado
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", usuario.getNombre());
        } else {
            // Usuario no logeado
            model.addAttribute("isLoggedIn", false);
        }

        return "about"; // plantilla about.html
    }

    // Página de perfil del usuario (/perfil)
    @GetMapping("/perfil")
    public String perfilUsuario(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        // Si no hay usuario logueado, redirigir al login
        if (idUsuario == null) {
            return "redirect:/login";
        }

        // Obtener los datos del usuario
        UsuarioData usuario = usuarioService.findById(idUsuario);

        // Agregar el usuario al modelo
        model.addAttribute("usuario", usuario);

        return "perfil"; // plantilla perfil.html
    }
}

