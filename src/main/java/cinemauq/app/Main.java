package cinemauq.app;

import cinemauq.evento.NotificadorCliente;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.RegistroActividad;
import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.excepcion.PuntosInsuficientesException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.ConsecutivoFactura;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.puntos.ReglaPorValor;
import cinemauq.modelo.puntos.TipoBeneficio;
import cinemauq.modelo.reporte.GeneradorCsv;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.GeneradorTxt;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.AuthServicio;
import cinemauq.servicio.CarteleraServicio;
import cinemauq.servicio.CancelacionServicio;
import cinemauq.servicio.CineFacade;
import cinemauq.servicio.CompraServicio;
import cinemauq.servicio.EstadisticasServicio;
import cinemauq.servicio.ExportadorReporte;
import cinemauq.servicio.PagoServicio;
import cinemauq.servicio.PuntosServicio;
import cinemauq.servicio.ReporteServicio;
import cinemauq.servicio.TarjetaServicio;
import cinemauq.servicio.TarjetaServicioProxy;
import cinemauq.servicio.TarjetaServicioReal;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        // ---------- 1. ARMADO DEL SISTEMA ----------
        // getInstancia() se llama UNA sola vez en este punto de arranque (Singleton)
        ConsecutivoFactura consecutivo = ConsecutivoFactura.getInstancia();

        Repositorio<Pelicula, Integer> repoPeliculas = new RepositorioMemoria<>(Pelicula::getId);
        Repositorio<Funcion, Integer> repoFunciones = new RepositorioMemoria<>(Funcion::getId);
        Repositorio<Usuario, Integer> repoUsuarios = new RepositorioMemoria<>(Usuario::getId);
        Repositorio<Compra, Integer> repoCompras = new RepositorioMemoria<>(Compra::getNumeroFactura);

        // Observer: el publicador avisa a los observadores registrados
        PublicadorEventos publicador = new PublicadorEventos();
        NotificadorCliente notificador = new NotificadorCliente();
        RegistroActividad registro = new RegistroActividad();
        publicador.registrar(notificador);
        publicador.registrar(registro);

        CarteleraServicio carteleraServicio = new CarteleraServicio(repoFunciones, repoPeliculas);
        PagoServicio pagoServicio = new PagoServicio();
        PuntosServicio puntosServicio = new PuntosServicio(new ReglaPorValor(1000), publicador);   // 1 punto por $1.000
        CompraServicio compraServicio = new CompraServicio(repoCompras, consecutivo, pagoServicio,
                puntosServicio, publicador);
        CancelacionServicio cancelacionServicio = new CancelacionServicio(repoCompras, repoFunciones,
                pagoServicio, puntosServicio, publicador);
        TarjetaServicio tarjetas = new TarjetaServicioProxy(new TarjetaServicioReal(publicador));   // Proxy envolviendo al real
        AuthServicio auth = new AuthServicio(repoUsuarios);
        ReporteServicio reporteServicio = new ReporteServicio(repoCompras, repoFunciones, repoUsuarios);
        EstadisticasServicio estadisticas = new EstadisticasServicio(repoCompras, repoFunciones, repoUsuarios);
        ExportadorReporte exportador = new ExportadorReporte();

        // Factory Method: un generador por formato
        Map<String, GeneradorReporte> generadores = new HashMap<>();
        generadores.put("TXT", new GeneradorTxt());
        generadores.put("CSV", new GeneradorCsv());

        DatosIniciales datos = new DatosIniciales();
        datos.cargar(repoPeliculas, repoFunciones, repoUsuarios, repoCompras);

        CineFacade facade = new CineFacade(auth, carteleraServicio, compraServicio, tarjetas,
                datos.getCatalogo(), puntosServicio, cancelacionServicio, reporteServicio,
                estadisticas, exportador, notificador, generadores);

        LocalDate hoy = LocalDate.now();

        // ---------- 2. SESION Y CLIENTE DE PRUEBA ----------
        titulo("SESION");
        Cliente nicolas = (Cliente) facade.iniciarSesion("nicolas@correo.com", "1234");
        Usuario admin = facade.iniciarSesion("admin@cinemauq.com", "admin123");
        System.out.println("Sesion iniciada: " + nicolas.getNombre() + " y " + admin.getNombre());

        // Cliente nuevo, sin puntos, para que la demo no dependa de los datos iniciales
        Cliente prueba = new Cliente(99, "Cliente Prueba", "prueba@correo.com", "1234");
        repoUsuarios.guardar(prueba);
        facade.recargar(admin, prueba, 400000);
        System.out.println("Administrador recarga a " + prueba.getNombre()
                + " -> saldo: " + dinero(facade.consultarSaldo(prueba)));

        Funcion f1 = facade.buscarFuncion(1);
        Funcion f2 = facade.buscarFuncion(2);
        Funcion f11 = facade.buscarFuncion(11);   // dentro de 30 horas
        Funcion f12 = facade.buscarFuncion(12);   // dentro de 5 horas
        Funcion f13 = facade.buscarFuncion(13);   // dentro de 1 hora

        // ---------- 3. PUNTOS: ACUMULACION (RN13) ----------
        titulo("PUNTOS: acumulacion por compra");
        Compra completa = facade.comprar(new Compra.Builder()
                        .conCliente(nicolas).conFuncion(f1)
                        .conAsiento("D7").conAsiento("D8")
                        .conConfiteria(datos.getCombos().get(0))
                        .conConfiteria(datos.getProductos().get(3)),
                CompraServicio.PROMOCION_PORCENTAJE);
        System.out.println("Compra #" + completa.getNumeroFactura()
                + " | subtotal: " + dinero(completa.calcularSubtotal())
                + " | total pagado: " + dinero(completa.calcularTotal())
                + " | puntos ganados: " + completa.getPuntosGanados());
        System.out.println("Puntos vigentes de " + nicolas.getNombre() + ": " + facade.consultarPuntos(nicolas));

        // ---------- 4. PUNTOS: REDENCION (RN14, RN15, RN16) ----------
        titulo("PUNTOS: redencion de una entrada (500 puntos)");
        Compra.Builder canje = new Compra.Builder()
                .conCliente(prueba).conFuncion(f1).conAsiento("B1")
                .conCanje(TipoBeneficio.ENTRADA, f1.getPrecio());

        try {
            facade.comprar(canje, 0);
        } catch (PuntosInsuficientesException e) {
            System.out.println("Sin puntos        -> " + e.getMessage());
        }

        prueba.getPuntos().acumular(500, hoy.minusYears(2), 0);   // lote VENCIDO
        try {
            facade.comprar(canje, 0);
        } catch (PuntosInsuficientesException e) {
            System.out.println("Con puntos vencidos -> " + e.getMessage());
        }

        prueba.getPuntos().acumular(600, hoy, 0);                 // lote vigente
        Compra conCanje = facade.comprar(canje, 0);
        System.out.println("Con 600 puntos vigentes -> compra #" + conCanje.getNumeroFactura()
                + " | total pagado: " + dinero(conCanje.calcularTotal())
                + " | puntos redimidos: " + conCanje.getPuntosRedimidos()
                + " | puntos que quedan: " + facade.consultarPuntos(prueba));

        // ---------- 5. CANCELACION (STRATEGY + STATE) ----------
        titulo("CANCELACION: reembolso segun el tiempo restante");

        Compra c30 = comprarUna(facade, prueba, f11, "A1");
        double r30 = facade.cancelarCompra(prueba, c30.getNumeroFactura());
        System.out.println("Funcion en 30 h -> reembolso " + dinero(r30) + " de "
                + dinero(c30.calcularTotal()) + " (100 %) | estado: " + c30.getEstado().getNombre()
                + " | A1 libre: " + f11.getAsiento("A1").estaLibre());

        Compra c5 = comprarUna(facade, prueba, f12, "A1");
        double r5 = facade.cancelarCompra(prueba, c5.getNumeroFactura());
        System.out.println("Funcion en 5 h  -> reembolso " + dinero(r5) + " de "
                + dinero(c5.calcularTotal()) + " (80 %)");

        Compra c1 = comprarUna(facade, prueba, f13, "A1");
        try {
            facade.cancelarCompra(prueba, c1.getNumeroFactura());
        } catch (CancelacionNoPermitidaException e) {
            System.out.println("Funcion en 1 h  -> " + e.getMessage());
        }

        try {
            facade.cancelarCompra(prueba, c30.getNumeroFactura());   // ya estaba cancelada
        } catch (CancelacionNoPermitidaException e) {
            System.out.println("Cancelar dos veces -> " + e.getMessage());
        }
        System.out.println("Saldo de " + prueba.getNombre() + ": " + dinero(facade.consultarSaldo(prueba)));

        // ---------- 6. CANCELAR UNA FUNCION (RN18) ----------
        titulo("CANCELACION DE FUNCION por el administrador");
        comprarUna(facade, nicolas, f2, "A1");
        comprarUna(facade, prueba, f2, "A2");

        try {
            facade.cancelarFuncion(nicolas, 2);
        } catch (AccesoDenegadoException e) {
            System.out.println("Un cliente intenta cancelar -> " + e.getMessage());
        }
        int reembolsadas = facade.cancelarFuncion(admin, 2);
        boolean sigueEnCartelera = facade.listarCartelera().stream().anyMatch(f -> f.getId() == 2);
        System.out.println("El administrador cancela la funcion 2 -> compras reembolsadas: " + reembolsadas
                + " | aparece en la cartelera: " + sigueEnCartelera + " (esperado: false)");

        // ---------- 7. OBSERVER ----------
        titulo("NOTIFICACIONES de " + prueba.getNombre());
        for (String notificacion : facade.consultarNotificaciones(prueba)) {
            System.out.println(notificacion);
        }
        System.out.println("Eventos en el registro de actividad: " + registro.getEventos().size());

        // ---------- 8. PROTOTYPE ----------
        titulo("PROTOTYPE: duplicar una funcion");
        Funcion f3 = facade.buscarFuncion(3);
        Funcion copia = carteleraServicio.duplicarFuncion(3, 50, hoy.plusDays(3), LocalTime.of(16, 0));
        f3.getAsiento("D7").reservar();
        System.out.println("D7 libre en la original? " + f3.getAsiento("D7").estaLibre() + " (esperado: false)");
        System.out.println("D7 libre en la copia?    " + copia.getAsiento("D7").estaLibre() + " (esperado: true)");

        // ---------- 9. REPORTES (FACTORY METHOD) Y ESTADISTICAS ----------
        titulo("REPORTE DE VENTAS: TXT y CSV");
        LocalDate desde = hoy.minusMonths(3);
        System.out.println(facade.emitirReporte(TipoReporte.VENTAS_PERIODO, desde, hoy, "TXT"));
        System.out.println();
        System.out.println(facade.emitirReporte(TipoReporte.VENTAS_PERIODO, desde, hoy, "CSV"));

        Path ruta = Path.of("reportes", "reporte-ventas.txt");
        facade.exportarReporte(TipoReporte.VENTAS_PERIODO, desde, hoy, "TXT", ruta);
        System.out.println();
        System.out.println("Reporte exportado en: " + ruta.toAbsolutePath());

        titulo("ESTADISTICAS");
        System.out.println("Ventas totales: " + dinero(facade.ventasTotales(desde, hoy)));
        facade.ocupacionPorSala(desde, hoy).forEach((sala, porcentaje) ->
                System.out.println("Ocupacion sala " + sala + ": " + String.format("%.1f", porcentaje) + " %"));
        System.out.println("Peliculas mas vendidas: " + facade.peliculasMasVendidas(3));
        System.out.println("Productos mas vendidos: " + facade.productosMasVendidos(3));
        System.out.println("Total reembolsado: " + dinero(facade.totalReembolsos(desde, hoy)));
    }

    // Compra una entrada sin descuentos (para las demos de cancelacion)
    private static Compra comprarUna(CineFacade facade, Cliente cliente, Funcion funcion, String asiento) {
        return facade.comprar(new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion).conAsiento(asiento), 0);
    }

    private static String dinero(double valor) {
        return "$" + Math.round(valor);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}