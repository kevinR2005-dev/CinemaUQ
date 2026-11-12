package cinemauq.evento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistroActividad implements Observador {
    private final List<EventoCine> eventos = new ArrayList<>();

    @Override
    public void actualizar(EventoCine e) {
        eventos.add(e);
    }

    public List<EventoCine> getEventos() {
        return Collections.unmodifiableList(eventos);
    }
}