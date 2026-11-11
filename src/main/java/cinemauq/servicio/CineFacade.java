package cinemauq.servicio;


import cinemauq.evento.NotificadorCliente;
import cinemauq.excepcion.EntidadNoEncontradaException;
import cinemauq.modelo.*;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.puntos.LotePuntos;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.modelo.tarjeta.Movimiento;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CineFacade {

    private final AuthServicio auth;
    private final CarteleraServicio cartelera;
    private final CompraServicio compras;
    private final TarjetaServicio tarjetas;
    private final List<ItemConfiteria> catalogo;
    private final PuntosServicio puntos;
    private final CancelacionServicio cancelacion;
    private final ReporteServicio reportes;
    private final EstadisticasServicio estadisticas;
    private final ExportadorReporte exportador;
    private final NotificadorCliente notificador;
    private final Map<String, GeneradorReporte> generadores;

    public CineFacade(AuthServicio auth, CarteleraServicio cartelera,
                      CompraServicio compras, TarjetaServicio tarjetas,
                      List<ItemConfiteria> catalogo, PuntosServicio puntos,
                      CancelacionServicio cancelacion, ReporteServicio reportes,
                      EstadisticasServicio estadisticas, ExportadorReporte exportador,
                      NotificadorCliente notificador,
                      Map<String, GeneradorReporte> generadores) {
        this.auth = auth;
        this.cartelera = cartelera;
        this.compras = compras;
        this.tarjetas = tarjetas;
        this.catalogo = catalogo;
        this.puntos = puntos;
        this.cancelacion = cancelacion;
        this.reportes = reportes;
        this.estadisticas = estadisticas;
        this.exportador = exportador;
        this.notificador = notificador;
        this.generadores = generadores;
    }


    public Usuario iniciarSesion(String correo, String contrasena) {
        Usuario usuario = auth.iniciarSesion(correo, contrasena);
        if (usuario instanceof Cliente cliente) {
            puntos.avisarVencimientos(cliente, LocalDate.now());   // publica el aviso si hay
        }
        return usuario;
    }

    // ---------- Cartelera ----------
    // Solo las funciones programadas (las canceladas no se muestran)
    public List<Funcion> listarCartelera() {
        List<Funcion> programadas = new ArrayList<>();
        for (Funcion f : cartelera.listarFunciones()) {
            if (f.getEstado() == EstadoFuncion.PROGRAMADA) {
                programadas.add(f);
            }
        }
        return programadas;
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

    // ---------- Compra ----------
    public Compra comprar(Compra.Builder b, double descuentoPorcentaje) {
        return compras.confirmarCompra(b, descuentoPorcentaje);
    }

    public List<Compra> consultarHistorial(Cliente c) {
        return compras.historial(c);
    }

    // ---------- Cancelacion ----------
    public double cancelarCompra(Cliente c, int numeroFactura) {
        return cancelacion.cancelarCompra(c, numeroFactura, LocalDateTime.now());
    }

    public int cancelarFuncion(Usuario admin, int idFuncion) {
        return cancelacion.cancelarFuncion(admin, idFuncion, LocalDateTime.now());
    }

    // ---------- Tarjeta ----------
    public void recargar(Usuario solicitante, Cliente destino, double monto) {
        tarjetas.recargar(solicitante, destino, monto);
    }

    public double consultarSaldo(Cliente c) {
        return tarjetas.consultarSaldo(c);
    }

    public List<Movimiento> consultarMovimientos(Cliente c) {
        return tarjetas.consultarMovimientos(c);
    }

    public List<Cliente> listarClientes() {
        return auth.listarClientes();
    }

    // ---------- Puntos y notificaciones ----------
    public int consultarPuntos(Cliente c) {
        return c.getPuntos().puntosVigentes(LocalDate.now());
    }

    public List<LotePuntos> consultarLotes(Cliente c) {
        return c.getPuntos().getLotes();
    }

    public List<String> consultarNotificaciones(Cliente c) {
        return notificador.obtener(c);
    }

    // ---------- Reportes ----------
    public String emitirReporte(TipoReporte tipo, LocalDate desde, LocalDate hasta, String formato) {
        GeneradorReporte generador = generadores.get(formato);
        if (generador == null) {
            throw new IllegalArgumentException("Formato de reporte no soportado: " + formato);
        }
        return generador.emitir(reportes.datos(tipo, desde, hasta));
    }

    public void exportarReporte(TipoReporte tipo, LocalDate desde, LocalDate hasta,
                                String formato, Path ruta) {
        exportador.exportar(emitirReporte(tipo, desde, hasta, formato), ruta);
    }

    public Map<String, Double> serieReporte(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        return reportes.serie(tipo, desde, hasta);
    }

    // ---------- Estadisticas ----------
    public double ventasTotales(LocalDate desde, LocalDate hasta) {
        return estadisticas.ventasTotales(desde, hasta);
    }

    public Map<Integer, Double> ocupacionPorSala(LocalDate desde, LocalDate hasta) {
        return estadisticas.ocupacionPorSala(desde, hasta);
    }

    public List<String> peliculasMasVendidas(int n) {
        return estadisticas.peliculasMasVendidas(n);
    }

    public List<String> productosMasVendidos(int n) {
        return estadisticas.productosMasVendidos(n);
    }

    public double totalReembolsos(LocalDate desde, LocalDate hasta) {
        return estadisticas.totalReembolsos(desde, hasta);
    }
}
