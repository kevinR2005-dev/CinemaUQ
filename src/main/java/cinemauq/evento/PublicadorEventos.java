package cinemauq.evento;

import java.util.ArrayList;
import java.util.List;

public class PublicadorEventos {
    private final List<Observador> observadores = new ArrayList<>();

    public void registrar(Observador o) {
        observadores.add(o);
    }

    // Avisa a todos; si uno falla, los demas igual reciben el evento
    public void publicar(EventoCine e) {
        for (Observador o : observadores) {
            try {
                o.actualizar(e);
            } catch (Exception ex) {
                // se ignora: un observador defectuoso no afecta a los demas
            }
        }
    }
}