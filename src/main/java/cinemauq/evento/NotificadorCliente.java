package cinemauq.evento;

import cinemauq.modelo.Cliente;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificadorCliente implements Observador {
    // id de cliente -> lista de textos "[fecha] mensaje"
    private final Map<Integer, List<String>> notificaciones = new HashMap<>();

    @Override
    public void actualizar(EventoCine e) {
        if (e.getCliente() == null) {
            return; // evento sin destinatario
        }
        notificaciones
                .computeIfAbsent(e.getCliente().getId(), k -> new ArrayList<>())
                .add("[" + e.getFecha() + "] " + e.getMensaje());
    }

    public List<String> obtener(Cliente c) {
        List<String> lista = notificaciones.get(c.getId());
        if (lista == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(lista);
    }
}