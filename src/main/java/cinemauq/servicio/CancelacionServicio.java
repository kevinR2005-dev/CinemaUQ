package cinemauq.servicio;

import cinemauq.evento.EventoCine;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.TipoEvento;
import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.EstadoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Rol;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.cancelacion.PoliticaReembolso;
import cinemauq.modelo.cancelacion.ReembolsoParcial;
import cinemauq.modelo.cancelacion.ReembolsoTotal;
import cinemauq.modelo.cancelacion.SinReembolso;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CancelacionServicio {

    private final Repositorio<Compra, Integer> compras;
    private final Repositorio<Funcion, Integer> funciones;
    private final PagoServicio pagoServicio;
    private final PuntosServicio puntosServicio;
    private final PublicadorEventos publicador;

    public CancelacionServicio(Repositorio<Compra, Integer> compras,
                               Repositorio<Funcion, Integer> funciones,
                               PagoServicio pagoServicio,
                               PuntosServicio puntosServicio,
                               PublicadorEventos publicador) {
        this.compras = compras;
        this.funciones = funciones;
        this.pagoServicio = pagoServicio;
        this.puntosServicio = puntosServicio;
        this.publicador = publicador;
    }

    public double cancelarCompra(Cliente c, int numeroFactura, LocalDateTime ahora) {
        // 1) buscar la compra (lanza EntidadNoEncontradaException)
        Compra compra = compras.buscarPorId(numeroFactura);

        // 2) debe ser del cliente
        if (compra.getCliente().getId() != c.getId()) {
            throw new AccesoDenegadoException("La compra no pertenece al cliente");
        }

        // 3) si la función ya pasó, la compra pasa a UTILIZADA
        compra.actualizarEstado(ahora);

        // 4) elegir política y calcular el reembolso (SinReembolso lanza aquí, antes de cambiar nada)
        Funcion funcion = funciones.buscarPorId(compra.getFuncion().getId());
        boolean porCine = funcion.getEstado() == EstadoFuncion.CANCELADA;
        PoliticaReembolso politica = elegirPolitica(funcion, ahora, porCine);
        double reembolso = politica.calcular(compra.calcularTotal());

        // 5) el State decide si la compra se puede cancelar (lanza si no está PAGADA)
        compra.cancelar();

        // 6) solo ahora se cambian cosas: asientos, dinero, puntos y aviso
        for (String codigo : compra.getAsientos()) {
            funcion.getAsiento(codigo).liberar();
        }
        if (reembolso > 0) {
            pagoServicio.reembolsar(c, reembolso, "Reembolso factura #" + numeroFactura);
        }
        puntosServicio.revertirCompra(c, compra);
        publicador.publicar(new EventoCine(TipoEvento.CANCELACION, c,
                "Compra #" + numeroFactura + " cancelada. Reembolso de $" + reembolso, ahora));
        return reembolso;
    }

    public int cancelarFuncion(Usuario solicitante, int idFuncion, LocalDateTime ahora) {
        if (solicitante.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo el administrador puede cancelar funciones");
        }
        Funcion funcion = funciones.buscarPorId(idFuncion); // lanza si no existe
        funcion.cancelar();

        // Copia de la lista para no modificar mientras se recorre
        int reembolsadas = 0;
        for (Compra compra : new ArrayList<>(compras.listar())) {
            boolean esDeLaFuncion = compra.getFuncion().getId() == idFuncion;
            boolean estaPagada = compra.getEstado().getNombre().equals("PAGADA");
            if (esDeLaFuncion && estaPagada) {
                cancelarCompra(compra.getCliente(), compra.getNumeroFactura(), ahora); // 100 %
                reembolsadas++;
            }
        }
        return reembolsadas;
    }

    // Único lugar con if por horas
    private PoliticaReembolso elegirPolitica(Funcion f, LocalDateTime ahora, boolean porCine) {
        if (porCine) {
            return new ReembolsoTotal();
        }
        double horas = ChronoUnit.MINUTES.between(ahora, f.getFechaHora()) / 60.0;
        if (horas > 24) {
            return new ReembolsoTotal();
        }
        if (horas >= 2) {
            return new ReembolsoParcial(80);
        }
        return new SinReembolso();
    }
}