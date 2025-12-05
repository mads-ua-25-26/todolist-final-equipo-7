package madstodolist.service;

import madstodolist.model.Etiqueta;
import madstodolist.repository.EtiquetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EtiquetaService {

    @Autowired
    EtiquetaRepository etiquetaRepository;

    @Transactional(readOnly = true)
    public List<Etiqueta> findAll() {
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetaRepository.findAll().forEach(etiquetas::add);
        return etiquetas;
    }

    @Transactional(readOnly = true)
    public Etiqueta findById(Long id) {
        return etiquetaRepository.findById(id)
                .orElseThrow(() -> new EtiquetaServiceException("Etiqueta no encontrada con id " + id));
    }

    @Transactional
    public Etiqueta crearEtiqueta(String nombre, String color) {
        Etiqueta etiqueta = new Etiqueta(nombre, color);
        return etiquetaRepository.save(etiqueta);
    }
}