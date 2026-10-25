package cinemauq.app;

import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.excepcion.CredencialesInvalidasException;
import cinemauq.excepcion.SaldoInsuficienteException;
import cinemauq.modelo.*;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.reporte.GeneradorCsv;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.GeneradorTxt;
import cinemauq.modelo.tarjeta.Movimiento;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {

        // ---------- 1. ARMADO DEL SISTEMA ----------
        // UNICO lugar (junto con CinemaUQApp) donde se llama getInstancia()
        ConsecutivoFactura consecutivo = ConsecutivoFactura.getInstancia();

        Repositorio<Pelicula, Integer> repoPeliculas = new RepositorioMemoria<>(Pelicula::getId);
        Repositorio<Funcion, Integer> repoFunciones = new RepositorioMemoria<>(Funcion::getId);
        Repositorio<Usuario, Integer> repoUsuarios = new RepositorioMemoria<>(Usuario::getId);
        Repositorio<Compra, Integer> repoCompras = new RepositorioMemoria<>(Compra::getNumeroFactura);

        CarteleraServicio carteleraServicio = new CarteleraServicio(repoFunciones, repoPeliculas);
        PagoServicio pagoServicio = new PagoServicio();
        CompraServicio compraServicio = new CompraServicio(repoCompras, consecutivo, pagoServicio);
        TarjetaServicio tarjetas = new TarjetaServicioProxy(new TarjetaServicioReal());   // Proxy envolviendo al real
        AuthServicio auth = new AuthServicio(repoUsuarios);

        DatosIniciales datos = new DatosIniciales();
        datos.cargar(repoPeliculas, repoFunciones, repoUsuarios);

        CineFacade facade = new CineFacade(auth, carteleraServicio, compraServicio, tarjetas, datos.getCatalogo());

        // ---------- 2. SESION ----------
        titulo("SESION");
        Cliente nicolas = (Cliente) facade.iniciarSesion("nicolas@correo.com", "1234");
        Cliente kevin = (Cliente) facade.iniciarSesion("kevin@correo.com", "1234");
        Usuario admin = facade.iniciarSesion("admin@cinemauq.com", "admin123");
        System.out.println("Sesion iniciada: " + nicolas.getNombre() + ", " + kevin.getNombre()
                + " y " + admin.getNombre());
        try {
            facade.iniciarSesion("nicolas@correo.com", "mala");
        } catch (CredencialesInvalidasException e) {
            System.out.println("Credenciales incorrectas -> " + e.getMessage());
        }

        Funcion f1 = facade.buscarFuncion(1);

        // ---------- 3. COMPRAS (Builder + Composite + Decorator) ----------
        titulo("COMPRAS: minima y completa");
        Compra minima = facade.comprar(new Compra.Builder()
                .conCliente(nicolas).conFuncion(f1).conAsiento("A1"), 0);
        System.out.println("Compra minima   -> factura #" + minima.getNumeroFactura()
                + " | total: $" + minima.calcularTotal());

        Compra completa = facade.comprar(new Compra.Builder()
                        .conCliente(nicolas).conFuncion(f1)
                        .conAsiento("D7").conAsiento("D8")
                        .conConfiteria(datos.getCombos().get(0))      // Combo Clasico
                        .conConfiteria(datos.getProductos().get(3)),  // Chocolate
                CompraServicio.PROMOCION_PORCENTAJE);
        System.out.println("Compra completa -> factura #" + completa.getNumeroFactura()
                + " | subtotal: $" + completa.calcularSubtotal()
                + " | total con 10% y cargo: $" + completa.calcularTotal());
        System.out.println("Saldo de Nicolas: $" + facade.consultarSaldo(nicolas));

        // ---------- 4. PAGO RECHAZADO (RN07 y RN10) ----------
        titulo("PAGO RECHAZADO: saldo insuficiente");
        try {
            facade.comprar(new Compra.Builder()
                    .conCliente(kevin).conFuncion(f1).conAsiento("C1"), 0);
        } catch (SaldoInsuficienteException e) {
            System.out.println("Kevin -> " + e.getMessage());
        }
        System.out.println("Asiento C1 sigue libre? " + f1.getAsiento("C1").estaLibre() + " (esperado: true)");

        // ---------- 5. PROXY: solo el administrador recarga (RN09) ----------
        titulo("PROXY: recarga de tarjetas");
        try {
            facade.recargar(kevin, kevin, 100000);
        } catch (AccesoDenegadoException e) {
            System.out.println("Cliente intenta recargar -> " + e.getMessage());
        }
        facade.recargar(admin, kevin, 100000);
        System.out.println("Administrador recarga a Kevin -> saldo: $" + facade.consultarSaldo(kevin));

        Compra tercera = facade.comprar(new Compra.Builder()
                .conCliente(kevin).conFuncion(f1).conAsiento("C1"), 0);
        System.out.println("Kevin compra ahora -> factura #" + tercera.getNumeroFactura()
                + " (debe ser #3: el pago rechazado no gasto numero)");

        // ---------- 6. MOVIMIENTOS ----------
        titulo("MOVIMIENTOS DE LA TARJETA DE NICOLAS");
        for (Movimiento m : facade.consultarMovimientos(nicolas)) {
            System.out.println(m.getTipo() + " | $" + m.getMonto()
                    + " | saldo: $" + m.getSaldoResultante() + " | " + m.getDetalle());
        }

        // ---------- 7. PROTOTYPE ----------
        titulo("PROTOTYPE: duplicar una funcion");
        Funcion f3 = facade.buscarFuncion(3);
        Funcion copia = carteleraServicio.duplicarFuncion(3, 5, LocalDate.now().plusDays(3), LocalTime.of(16, 0));
        f3.getAsiento("D7").reservar();
        System.out.println("D7 libre en la original? " + f3.getAsiento("D7").estaLibre() + " (esperado: false)");
        System.out.println("D7 libre en la copia?    " + copia.getAsiento("D7").estaLibre() + " (esperado: true)");

        // ---------- 8. FACTORY METHOD ----------
        titulo("REPORTES: TXT y CSV");
        String datosVentas = compraServicio.datosVentas();
        GeneradorReporte txt = new GeneradorTxt();
        GeneradorReporte csv = new GeneradorCsv();
        System.out.println(txt.emitir(datosVentas));
        System.out.println();
        System.out.println(csv.emitir(datosVentas));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}