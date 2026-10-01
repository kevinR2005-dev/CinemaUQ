package cinemauq.repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;



public class RepositorioMemoria<T, ID> implements Repositorio<T, ID>{

    private final Map<ID, T> datos = new LinkedHashMap<>();
    private final Function<T, ID> extractorId;


    public RepositorioMemoria(Function<T, ID>extractorId){
        this.extractorId = extractorId;

    }

    @Override
    public void guardar(T t){
        datos.put(extractorId.apply(t), t);
    }

    @Override
    public T buscarPorId(ID id){
        T encontrado = datos.get(id);
        if(encontrado == null){
            throw new cinemauq.excepcion.EntidadNoEncontradaException("No existe una entidad con id:" + id);
        }
        return encontrado;
    }

    @Override
    public List<T> listar(){
        return new ArrayList<>(datos.values());
    }
}
