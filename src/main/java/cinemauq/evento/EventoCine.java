package cinemauq.evento;

import cinemauq.modelo.Cliente;
import java.time.LocalDateTime;

// Evento inmutable: solo atributos finales y getters
public class EventoCine {
    private final TipoEvento tipo;
    private final Cliente cliente;
    private final String mensaje;
    private final LocalDateTime fecha;

    public EventoCine(TipoEvento tipo, Cliente cliente, String mensaje, LocalDateTime fecha) {
        this.tipo = tipo;
        this.cliente = cliente;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public TipoEvento getTipo() { return tipo; }
    public Cliente getCliente() { return cliente; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
}