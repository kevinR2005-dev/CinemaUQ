package cinemauq.app;

import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Combo;
import cinemauq.modelo.Compra;
import cinemauq.modelo.ConsecutivoFactura;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.reporte.GeneradorCsv;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.GeneradorTxt;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.CarteleraServicio;
import cinemauq.servicio.CompraServicio;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {

        // ---------- 1. ARMADO DEL SISTEMA ----------
        // ÚNICO lugar del proyecto donde se llama getInstancia() (Singleton)
        ConsecutivoFactura consecutivo = ConsecutivoFactura.getInstancia();

        Repositorio<Pelicula, Integer> repoPeliculas = new RepositorioMemoria<>(Pelicula::getId);
        Repositorio<Funcion, Integer> repoFunciones = new RepositorioMemoria<>(Funcion::getId);
        Repositorio<Compra, Integer> repoCompras = new RepositorioMemoria<>(Compra::getNumeroFactura);

        CarteleraServicio carteleraServicio = new CarteleraServicio(repoFunciones, repoPeliculas);
        CompraServicio compraServicio = new CompraServicio(repoCompras, consecutivo);

        DatosIniciales datos = new DatosIniciales();
        datos.cargar(repoPeliculas, repoFunciones);

        Cliente nicolas = datos.getClientes().get(0);
        Cliente kevin = datos.getClientes().get(1);
        Combo combo = datos.getCombos().get(0);
        Funcion f1 = repoFunciones.buscarPorId(1);
        Funcion f2 = repoFunciones.buscarPorId(2);
        Funcion f3 = repoFunciones.buscarPorId(3);

        titulo("Datos cargados: " + repoPeliculas.listar().size() + " peliculas, "
                + repoFunciones.listar().size() + " funciones, "
                + datos.getClientes().size() + " clientes, "
                + datos.getCombos().size() + " combos");

        // ---------- 2. BUILDER + SINGLETON ----------
        titulo("BUILDER: compra minima y compra completa");

        // Compra mínima: solo los obligatorios
        Compra minima = compraServicio.confirmarCompra(new Compra.Builder()
                .conCliente(nicolas)
                .conFuncion(f1)
                .conAsiento("A1"));
        System.out.println("Compra minima   -> factura #" + minima.getNumeroFactura()
                + " | total: $" + minima.calcularTotal());

        // Compra completa: obligatorios + combo opcional
        Compra completa = compraServicio.confirmarCompra(new Compra.Builder()
                .conCliente(kevin)
                .conFuncion(f1)
                .conAsiento("D7")
                .conAsiento("D8")
                .conCombo(combo));
        System.out.println("Compra completa -> factura #" + completa.getNumeroFactura()
                + " | total: $" + completa.calcularTotal());

        // ---------- 3. COMPRAS INVALIDAS ----------
        titulo("COMPRAS INVALIDAS (el sistema las rechaza sin caerse)");

        // a) Sin cliente -> lo rechaza build()/validar()
        try {
            compraServicio.confirmarCompra(new Compra.Builder()
                    .conFuncion(f1)
                    .conAsiento("A2"));
        } catch (IllegalStateException e) {
            System.out.println("Sin cliente         -> " + e.getMessage());
        }

        // b) Mas de 8 asientos -> RN-03
        try {
            Compra.Builder muchos = new Compra.Builder().conCliente(nicolas).conFuncion(f1);
            for (int i = 1; i <= 9; i++) {
                muchos.conAsiento("C" + i);
            }
            compraServicio.confirmarCompra(muchos);
        } catch (IllegalStateException e) {
            System.out.println("Mas de 8 asientos   -> " + e.getMessage());
        }

        // c) Asiento ya vendido -> RN-02
        try {
            compraServicio.confirmarCompra(new Compra.Builder()
                    .conCliente(kevin)
                    .conFuncion(f1)
                    .conAsiento("A1"));
        } catch (AsientoNoDisponibleException e) {
            System.out.println("Asiento ya vendido  -> " + e.getMessage());
        }

        // RN-06: las compras rechazadas NO gastaron numero de factura
        Compra tercera = compraServicio.confirmarCompra(new Compra.Builder()
                .conCliente(nicolas)
                .conFuncion(f2)
                .conAsiento("B2"));
        System.out.println("Siguiente compra valida -> factura #" + tercera.getNumeroFactura()
                + " (debe ser #3: ninguna rechazada gasto numero)");

        // ---------- 4. PROTOTYPE ----------
        titulo("PROTOTYPE: duplicar una funcion (copia profunda)");

        Funcion copia = carteleraServicio.duplicarFuncion(
                3, 5, LocalDate.now().plusDays(3), LocalTime.of(16, 0));

        // Vendemos D7 SOLO en la original
        f3.getAsiento("D7").reservar();

        System.out.println("D7 libre en la original? " + f3.getAsiento("D7").estaLibre()
                + "  (esperado: false)");
        System.out.println("D7 libre en la copia?    " + copia.getAsiento("D7").estaLibre()
                + "  (esperado: true)");

        // ---------- 5. FACTORY METHOD ----------
        titulo("FACTORY METHOD: el mismo reporte en TXT y en CSV");

        String datosVentas = compraServicio.datosVentas();
        GeneradorReporte txt = new GeneradorTxt();
        GeneradorReporte csv = new GeneradorCsv();

        System.out.println(txt.emitir(datosVentas));
        System.out.println();
        System.out.println(csv.emitir(datosVentas));
    }

    // Imprime un separador con titulo para leer mejor la salida
    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}