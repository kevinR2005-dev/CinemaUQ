package main.servicio;

import CinemaUQ.excepcion.AsientoNoDisponibleException;
import CinemaUQ.modelo.Compra;
import CinemaUQ.modelo.ConsecutivoFactura;
import CinemaUQ.modelo.Funcion;
import CinemaUQ.repositorio.Repositorio;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class CompraServicio {

    private final Repositorio<Compra, Integer> repositorio;
    private final ConsecutivoFactura consecutivo; // el Singleton llega por constructor

    public CompraServicio(Repositorio<Compra, Integer> repositorio, ConsecutivoFactura consecutivo) {
        this.repositorio = repositorio;
        this.consecutivo = consecutivo;
    }

    public Compra confirmarCompra(Compra.Builder b) {
        // 1) Validar los datos (lanza IllegalStateException si algo falta)
        b.validar();


        Compra previa = b.build();
        Funcion funcion = previa.getFuncion();
        List<String> codigos = previa.getAsientos();

        // 2a) Verificar que TODOS estén libres antes de reservar nada (RN-02)
        Set<String> vistos = new HashSet<>();
        for (String codigo : codigos) {
            if (!vistos.add(codigo)) {
                throw new AsientoNoDisponibleException("Asiento repetido en la compra: " + codigo);
            }
            if (!funcion.getAsiento(codigo).estaLibre()) {
                throw new AsientoNoDisponibleException("El asiento " + codigo + " ya está vendido");
            }
        }

        // 2b) Ahora sí reservarlos
        for (String codigo : codigos) {
            funcion.getAsiento(codigo).reservar();
        }

        // 3) El número de factura se pide al final: una compra rechazada no gasta número (RN-06)
        b.conNumeroFactura(consecutivo.siguiente());

        // 4) Crear y guardar
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