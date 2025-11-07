
package madstodolist.controller;

import javax.servlet.http.HttpSession;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavbarAdvice {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        return managerUserSession.usuarioLogeado() != null;
    }

    @ModelAttribute("username")
    public String username() {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            return (usuario != null) ? usuario.getNombre() : "";
        }
        return "";
    }

    @ModelAttribute("usuarioId")
    public Long usuarioId() {
        return managerUserSession.usuarioLogeado();
    }
}