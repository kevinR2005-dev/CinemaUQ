package cinemauq.repositorio;

import java.util.List;

public interface Repositorio<T, ID> {
    void guardar(T t);

    T buscarPorId(ID id);

    List<T> listar();
}
