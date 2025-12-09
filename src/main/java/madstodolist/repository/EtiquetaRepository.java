package madstodolist.repository;

import madstodolist.model.Etiqueta;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface EtiquetaRepository extends CrudRepository<Etiqueta, Long> {
    List<Etiqueta> findByUsuarioId(Long usuarioId);
}