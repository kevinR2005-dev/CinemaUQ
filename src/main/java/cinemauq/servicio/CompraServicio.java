package cinemauq.servicio;

import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.modelo.Compra;
import cinemauq.modelo.ConsecutivoFactura;
import cinemauq.modelo.Funcion;
import cinemauq.repositorio.Repositorio;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class CompraServicio {

    private final Repositorio<Compra, Integer> repositorio;
    private final ConsecutivoFactura consecutivo;

    public CompraServicio(Repositorio<Compra, Integer> repositorio, ConsecutivoFactura consecutivo) {
        this.repositorio = repositorio;
        this.consecutivo = consecutivo;
    }

    public Compra confirmarCompra(Compra.Builder b) {

        b.validar();


        Compra previa = b.build();
        Funcion funcion = previa.getFuncion();
        List<String> codigos = previa.getAsientos();


        Set<String> vistos = new HashSet<>();
        for (String codigo : codigos) {
            if (!vistos.add(codigo)) {
                throw new AsientoNoDisponibleException("Asiento repetido en la compra: " + codigo);
            }
            if (!funcion.getAsiento(codigo).estaLibre()) {
                throw new AsientoNoDisponibleException("El asiento " + codigo + " ya está vendido");
            }
        }


        for (String codigo : codigos) {
            funcion.getAsiento(codigo).reservar();
        }


        b.conNumeroFactura(consecutivo.siguiente());


        Compra compra = b.build();
        repositorio.guardar(compra);
        return compra;
    }


    public String datosVentas() {
        StringBuilder sb = new StringBuilder();
        for (Compra c : repositorio.listar()) {
            sb.append(c.getNumeroFactura()).append(";")
                    .append(c.getCliente().getNombre()).append(";")
                    .append(c.getFuncion().getPelicula().getTitulo()).append(";")
                    .append(String.join(",", c.getAsientos())).append(";")
                    .append(String.format(Locale.US, "%.2f", c.calcularTotal()))
                    .append("\n");
        }
        return sb.toString();
    }
}