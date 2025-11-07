package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        // Completar
        Equipo equipoNuevo = new Equipo(nombre);  // Crea directamente la entidad
        equipoNuevo = equipoRepository.save(equipoNuevo);
        return modelMapper.map(equipoNuevo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        // Completar
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("El equipo no existe");
        }else {
            return modelMapper.map(equipo, EquipoData.class);
        }

    }

    public static void ordenarPorNombreSinCase(List<EquipoData> equipos) {
        Collections.sort(equipos,
                Comparator.comparing(EquipoData::getNombre, String.CASE_INSENSITIVE_ORDER));
    }

    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<EquipoData> equipos = new ArrayList<>();
        equipoRepository.findAll().forEach(
                equipo -> {equipos.add(modelMapper.map(equipo, EquipoData.class));
                });
        ordenarPorNombreSinCase(equipos);
        return equipos;

    }

    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) { throw new EquipoServiceException("El equipo no existe"); }
        Usuario usuario = usuarioRepository.findById(id1).orElse(null);
        if (usuario == null) { throw new EquipoServiceException("El usuario no existe"); }
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) { throw new EquipoServiceException("El equipo no existe"); }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long id) {
        List<EquipoData> equipos = new ArrayList<>();
        if(!usuarioRepository.findById(id).isPresent()){throw new EquipoServiceException("El usuario no existe");}

        equipoRepository.findAll().forEach(equipo -> {
            if(equipo.getUsuarios().contains(usuarioRepository.findById(id).orElse(null))){
                equipos.add(modelMapper.map(equipo, EquipoData.class));
            }
        });
        return equipos;
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("El equipo no existe");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new EquipoServiceException("El usuario no existe");
        }
        // Llama al método del modelo que gestiona la relación
        equipo.removeUsuario(usuario);
    }

    @Transactional
    public void editarNombreEquipo(Long idEquipo, String nombre) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));
        equipo.setNombre(nombre);
        // El save() es redundante si estás dentro de @Transactional,
        // pero lo dejamos por claridad
        equipoRepository.save(equipo);
        entityManager.flush();
    }
}