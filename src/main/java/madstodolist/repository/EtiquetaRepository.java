package madstodolist.repository;

import madstodolist.model.Etiqueta;
import org.springframework.data.repository.CrudRepository;

public interface EtiquetaRepository extends CrudRepository<Etiqueta, Long> {
}