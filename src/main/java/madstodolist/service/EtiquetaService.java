package madstodolist.service;

import madstodolist.model.Etiqueta;
import madstodolist.model.Usuario;
import madstodolist.repository.EtiquetaRepository;
import madstodolist.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EtiquetaService {

    @Autowired
    EtiquetaRepository etiquetaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Etiqueta> findAllByUsuario(Long usuarioId) {
        return etiquetaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Etiqueta findById(Long id) {
        return etiquetaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Etiqueta crearEtiqueta(Long usuarioId, String nombre, String color) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Etiqueta etiqueta = new Etiqueta(usuario, nombre, color);
        return etiquetaRepository.save(etiqueta);
    }

    // Método para guardar cambios masivos (Edición de etiquetas)
    @Transactional
    public void guardarEtiquetas(Long usuarioId, List<Etiqueta> etiquetasModificadas) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminamos las etiquetas que no vengan en la lista (si quisieras borrado implícito)
        // O simplemente actualizamos/creamos las que vienen:

        for (Etiqueta et : etiquetasModificadas) {
            if (et.getId() != null) {
                // Actualizar existente
                Etiqueta existente = etiquetaRepository.findById(et.getId()).orElse(null);
                if (existente != null && existente.getUsuario().getId().equals(usuarioId)) {
                    existente.setNombre(et.getNombre());
                    existente.setColor(et.getColor());
                    etiquetaRepository.save(existente);
                }
            } else {
                // Crear nueva que viene del formulario sin ID
                et.setUsuario(usuario);
                etiquetaRepository.save(et);
            }
        }
    }
}