package cinemauq.app;

import cinemauq.modelo.*;
import cinemauq.modelo.confiteria.CategoriaProducto;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.confiteria.Producto;
import cinemauq.modelo.estado.Cancelada;
import cinemauq.modelo.estado.Utilizada;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatosIniciales {

    private final List<Cliente> clientes = new ArrayList<>();
    private Administrador administrador;
    private final List<Producto> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();

    public void cargar(Repositorio<Pelicula, Integer> peliculas,
                       Repositorio<Funcion, Integer> funciones,
                       Repositorio<Usuario, Integer> usuarios,
                       Repositorio<Compra, Integer> compras) {

        // Peliculas
        Pelicula avengers = new Pelicula(1, "Avengers", 143, "Accion");
        Pelicula interestelar = new Pelicula(2, "Interestelar", 169, "Ciencia ficcion");
        Pelicula dune = new Pelicula(3, "Dune", 155, "Ciencia ficcion");
        peliculas.guardar(avengers);
        peliculas.guardar(interestelar);
        peliculas.guardar(dune);

        // Salas y funciones
        Sala sala1 = new Sala(1, 8, 12);
        Sala sala2 = new Sala(2, 6, 10);
        LocalDate hoy = LocalDate.now();
        funciones.guardar(new Funcion(1, avengers, sala1, hoy.plusDays(1), LocalTime.of(19, 0), 20000));
        funciones.guardar(new Funcion(2, interestelar, sala1, hoy.plusDays(1), LocalTime.of(21, 30), 20000));
        funciones.guardar(new Funcion(3, dune, sala2, hoy.plusDays(2), LocalTime.of(18, 0), 18000));
        funciones.guardar(new Funcion(4, avengers, sala2, hoy.plusDays(2), LocalTime.of(20, 30), 18000));

        // Clientes: uno con saldo alto y otro con saldo bajo (para probar saldo insuficiente)
        Cliente nicolas = new Cliente(1, "Nicolas Sanchez", "nicolas@correo.com", "1234");
        nicolas.getTarjeta().acreditar(500000, "Saldo inicial");
        Cliente kevin = new Cliente(2, "Kevin Robin", "kevin@correo.com", "1234");
        kevin.getTarjeta().acreditar(5000, "Saldo inicial");
        clientes.add(nicolas);
        clientes.add(kevin);

        // Administrador
        administrador = new Administrador(3, "Administrador", "admin@cinemauq.com", "admin123");

        usuarios.guardar(nicolas);
        usuarios.guardar(kevin);
        usuarios.guardar(administrador);

        // Productos
        Producto gaseosa = new Producto("Gaseosa", 5000, CategoriaProducto.BEBIDA);
        Producto crispetas = new Producto("Crispetas", 12000, CategoriaProducto.COMIDA);
        Producto perro = new Producto("Perro caliente", 9000, CategoriaProducto.COMIDA);
        Producto chocolate = new Producto("Chocolate", 3000, CategoriaProducto.DULCE);
        Producto agua = new Producto("Agua", 3500, CategoriaProducto.BEBIDA);
        productos.add(gaseosa);
        productos.add(crispetas);
        productos.add(perro);
        productos.add(chocolate);
        productos.add(agua);

        // Combos armados con Composite
        Combo clasico = new Combo("Combo Clasico");
        clasico.agregar(gaseosa);
        clasico.agregar(crispetas);

        Combo familiar = new Combo("Combo Familiar");   // contiene otro combo
        familiar.agregar(clasico);
        familiar.agregar(perro);
        familiar.agregar(chocolate);

        combos.add(clasico);
        combos.add(familiar);

        // ===== ENTREGA 3 =====
        agregarDatosEntrega3(funciones, compras, avengers, interestelar, dune,
                sala1, sala2, nicolas, kevin, crispetas, clasico);
    }

    private void agregarDatosEntrega3(Repositorio<Funcion, Integer> funciones,
                                      Repositorio<Compra, Integer> compras,
                                      Pelicula p1, Pelicula p2, Pelicula p3,
                                      Sala s1, Sala s2,
                                      Cliente nicolas, Cliente kevin,
                                      ItemConfiteria producto, ItemConfiteria combo) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = LocalDate.now();

        // Funciones para probar cancelacion: 30 h (100 %), 5 h (80 %), 1 h (rechazo)
        funciones.guardar(new Funcion(11, p1, s1, ahora.plusHours(30).toLocalDate(),
                ahora.plusHours(30).toLocalTime(), 20000));
        funciones.guardar(new Funcion(12, p2, s1, ahora.plusHours(5).toLocalDate(),
                ahora.plusHours(5).toLocalTime(), 20000));
        funciones.guardar(new Funcion(13, p3, s2, ahora.plusHours(1).toLocalDate(),
                ahora.plusHours(1).toLocalTime(), 20000));

        // Funciones pasadas, en distintas semanas de los ultimos 2 meses
        Funcion f101 = pasada(101, p1, s1, hoy.minusDays(55));
        Funcion f102 = pasada(102, p2, s2, hoy.minusDays(48));
        Funcion f103 = pasada(103, p3, s1, hoy.minusDays(40));
        Funcion f104 = pasada(104, p1, s2, hoy.minusDays(30));
        Funcion f105 = pasada(105, p2, s1, hoy.minusDays(20));
        Funcion f106 = pasada(106, p3, s2, hoy.minusDays(10));
        for (Funcion f : new Funcion[]{f101, f102, f103, f104, f105, f106}) {
            funciones.guardar(f);
        }

        // 12 compras historicas: UTILIZADA, salvo 1011 y 1012 que quedan CANCELADA
        historica(compras, 1001, nicolas, f101, "A1", 40000, null, false);
        historica(compras, 1002, kevin, f101, "A2", 20000, producto, false);
        historica(compras, 1003, nicolas, f102, "B1", 40000, combo, false);
        historica(compras, 1004, kevin, f102, "B2", 20000, null, false);
        historica(compras, 1005, nicolas, f103, "C1", 20000, null, false);
        historica(compras, 1006, nicolas, f103, "C2", 38000, producto, false);
        historica(compras, 1007, kevin, f104, "D1", 40000, combo, false);
        historica(compras, 1008, nicolas, f104, "D2", 20000, null, false);
        historica(compras, 1009, nicolas, f105, "E1", 40000, null, false);
        historica(compras, 1010, kevin, f106, "F1", 22000, producto, false);
        historica(compras, 1011, nicolas, f105, "E5", 20000, null, true);
        historica(compras, 1012, nicolas, f106, "F5", 20000, null, true);

        // Recargas historicas
        nicolas.getTarjeta().acreditar(300000, "Recarga", TipoMovimiento.RECARGA, ahora.minusDays(50));
        nicolas.getTarjeta().acreditar(200000, "Recarga", TipoMovimiento.RECARGA, ahora.minusDays(25));
        kevin.getTarjeta().acreditar(10000, "Recarga", TipoMovimiento.RECARGA, ahora.minusDays(15));

        // Puntos: lotes vigentes con distinto vencimiento y un lote vencido
        nicolas.getPuntos().acumular(30, hoy.minusMonths(14), 1001);              // VENCIDO hace 2 meses
        nicolas.getPuntos().acumular(120, hoy.minusYears(1).plusDays(10), 1003);  // vence en 10 dias
        nicolas.getPuntos().acumular(200, hoy.minusMonths(6), 1005);              // vence en 6 meses
        nicolas.getPuntos().acumular(300, hoy.minusMonths(1), 1009);              // vence en 11 meses
        kevin.getPuntos().acumular(80, hoy.minusMonths(3), 1007);
    }

    private Funcion pasada(int id, Pelicula p, Sala s, LocalDate fecha) {
        return new Funcion(id, p, s, fecha, LocalTime.of(19, 0), 20000);
    }

    // Compra historica: reserva directo en la funcion y la guarda.
    // Si es cancelada, libera el asiento y deja su REEMBOLSO con fecha.
    private void historica(Repositorio<Compra, Integer> compras, int factura, Cliente c,
                           Funcion f, String asiento, double total,
                           ItemConfiteria extra, boolean cancelada) {
        LocalDateTime fecha = f.getFechaHora().minusDays(1);
        f.getAsiento(asiento).reservar();
        Compra.Builder b = new Compra.Builder()
                .conCliente(c).conFuncion(f).conAsiento(asiento)
                .conNumeroFactura(factura).conTotal(total)
                .conPuntosGanados((int) (total / 1000)).conFecha(fecha);
        if (extra != null) {
            b.conConfiteria(extra);
        }
        Compra compra = b.build();
        if (cancelada) {
            compra.setEstado(new Cancelada());
            f.getAsiento(asiento).liberar();
            c.getTarjeta().acreditar(total, "Reembolso factura #" + factura,
                    TipoMovimiento.REEMBOLSO, fecha.plusHours(2));
        } else {
            compra.setEstado(new Utilizada());
        }
        compras.guardar(compra);
    }

    public List<Cliente> getClientes() { return clientes; }

    public Administrador getAdministrador() { return administrador; }

    public List<Producto> getProductos() { return productos; }

    public List<Combo> getCombos() { return combos; }

    // Productos + combos, para el catalogo de la confiteria
    public List<ItemConfiteria> getCatalogo() {
        List<ItemConfiteria> catalogo = new ArrayList<>(productos);
        catalogo.addAll(combos);
        return catalogo;
    }
}