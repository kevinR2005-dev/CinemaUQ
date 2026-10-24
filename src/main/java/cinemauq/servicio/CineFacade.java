package cinemauq.servicio;


import cinemauq.excepcion.EntidadNoEncontradaException;
import cinemauq.modelo.*;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.tarjeta.Movimiento;

import java.util.List;

public class CineFacade {

    private final AuthServicio auth;
    private final CarteleraServicio cartelera;
    private final CompraServicio compras;
    private final TarjetaServicio tarjetas;
    private final List<ItemConfiteria> catalogo;

    public CineFacade(AuthServicio auth, CarteleraServicio cartelera,
                      CompraServicio compras, TarjetaServicio tarjetas,
                      List<ItemConfiteria> catalogo) {
        this.auth = auth;
        this.cartelera = cartelera;
        this.compras = compras;
        this.tarjetas = tarjetas;
        this.catalogo = catalogo;
    }

    public Usuario iniciarSesion(String correo, String contrasena) {
        return auth.iniciarSesion(correo, contrasena);
    }

    public List<Funcion> listarCartelera() {
        return cartelera.listarFunciones();
    }

    public Funcion buscarFuncion(int id) {
        for (Funcion f : cartelera.listarFunciones()) {
            if (f.getId() == id) {
                return f;
            }
        }
        throw new EntidadNoEncontradaException("No existe la funcion " + id);
    }

    public List<AsientoFuncion> asientosDisponibles(int idFuncion) {
        return cartelera.asientosDisponibles(idFuncion);
    }

    public List<ItemConfiteria> listarConfiteria() {
        return catalogo;
    }

    public Compra comprar(Compra.Builder b, double descuentoPorcentaje) {
        return compras.confirmarCompra(b, descuentoPorcentaje);
    }

    public void recargar(Usuario solicitante, Cliente destino, double monto) {
        tarjetas.recargar(solicitante, destino, monto);
    }

    public double consultarSaldo(Cliente c) {
        return tarjetas.consultarSaldo(c);
    }

    public List<Movimiento> consultarMovimientos(Cliente c) {
        return tarjetas.consultarMovimientos(c);
    }

    public List<Compra> consultarHistorial(Cliente c) {
        return compras.historial(c);
    }

    public List<Cliente> listarClientes() {
        return auth.listarClientes();
    }
}
