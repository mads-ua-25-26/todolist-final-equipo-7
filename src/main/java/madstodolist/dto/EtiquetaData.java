package madstodolist.dto;

import madstodolist.model.Etiqueta;
import java.util.ArrayList;
import java.util.List;

public class EtiquetaData {
    private List<Etiqueta> etiquetas = new ArrayList<>();

    public List<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(List<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas;
    }
}