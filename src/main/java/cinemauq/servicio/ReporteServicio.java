package cinemauq.servicio;

import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.EstadoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.modelo.tarjeta.Movimiento;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReporteServicio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Repositorio<Compra, Integer> compras;
    private final Repositorio<Funcion, Integer> funciones;
    private final Repositorio<Usuario, Integer> usuarios;

    public ReporteServicio(Repositorio<Compra, Integer> compras,
                           Repositorio<Funcion, Integer> funciones,
                           Repositorio<Usuario, Integer> usuarios) {
        this.compras = compras;
        this.funciones = funciones;
        this.usuarios = usuarios;
    }

    // ================= TEXTO =================

    public String datos(TipoReporte t, LocalDate desde, LocalDate hasta) {
        return switch (t) {
            case VENTAS_PERIODO -> ventas(desde, hasta);
            case ENTRADAS_PELICULA -> entradas(desde, hasta);
            case OCUPACION_SALAS -> ocupacion(desde, hasta);
            case PRODUCTOS_VENDIDOS -> productos(desde, hasta);
            case RECARGAS -> movimientos("Recargas", TipoMovimiento.RECARGA, desde, hasta);
            case CANCELACIONES_REEMBOLSOS ->
                    movimientos("Cancelaciones y reembolsos", TipoMovimiento.REEMBOLSO, desde, hasta);
            case PUNTOS -> puntos(desde, hasta);
        };
    }

    private String ventas(LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        double suma = 0;
        for (Compra c : comprasValidas(desde, hasta)) {
            double total = c.calcularTotal();
            filas.add(c.getNumeroFactura() + ";" + c.getFecha().toLocalDate() + ";"
                    + c.getCliente().getNombre() + ";"
                    + c.getFuncion().getPelicula().getTitulo() + ";" + dinero(total));
            suma += total;
        }
        return armar("Ventas del periodo", desde, hasta, "factura;fecha;cliente;pelicula;total",
                filas, "TOTAL;;;;" + dinero(suma));
    }

    private String entradas(LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        double totalEntradas = 0;
        double suma = 0;
        for (Map.Entry<String, double[]> e : porPelicula(desde, hasta).entrySet()) {
            filas.add(e.getKey() + ";" + (int) e.getValue()[0] + ";" + dinero(e.getValue()[1]));
            totalEntradas += e.getValue()[0];
            suma += e.getValue()[1];
        }
        return armar("Entradas por pelicula", desde, hasta, "pelicula;entradas;total",
                filas, "TOTAL;" + (int) totalEntradas + ";" + dinero(suma));
    }

    private String ocupacion(LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        int vendidos = 0;
        int capacidad = 0;
        for (Map.Entry<Integer, int[]> e : porSala(desde, hasta).entrySet()) {
            int[] d = e.getValue();
            filas.add(e.getKey() + ";" + d[0] + ";" + d[1] + ";" + porcentaje(d[0], d[1]));
            vendidos += d[0];
            capacidad += d[1];
        }
        return armar("Ocupacion de salas", desde, hasta, "sala;vendidos;capacidad;porcentaje",
                filas, "TOTAL;" + vendidos + ";" + capacidad + ";" + porcentaje(vendidos, capacidad));
    }

    private String productos(LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        double unidades = 0;
        double suma = 0;
        for (Map.Entry<String, double[]> e : porProducto(desde, hasta).entrySet()) {
            filas.add(e.getKey() + ";" + (int) e.getValue()[0] + ";" + dinero(e.getValue()[1]));
            unidades += e.getValue()[0];
            suma += e.getValue()[1];
        }
        return armar("Productos vendidos", desde, hasta, "producto;unidades;total",
                filas, "TOTAL;" + (int) unidades + ";" + dinero(suma));
    }

    // Sirve para RECARGAS y para CANCELACIONES_REEMBOLSOS
    private String movimientos(String titulo, TipoMovimiento tipo, LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        double suma = 0;
        for (Cliente cl : clientes()) {
            for (Movimiento m : cl.getTarjeta().getMovimientos()) {
                if (m.getTipo() == tipo && enRango(m.getFecha().toLocalDate(), desde, hasta)) {
                    filas.add(m.getFecha().toLocalDate() + ";" + cl.getNombre() + ";" + dinero(m.getMonto()));
                    suma += m.getMonto();
                }
            }
        }
        filas.sort(null); // las fechas ISO se ordenan como texto
        return armar(titulo, desde, hasta, "fecha;cliente;monto", filas, "TOTAL;;" + dinero(suma));
    }

    private String puntos(LocalDate desde, LocalDate hasta) {
        List<String> filas = new ArrayList<>();
        int acumulados = 0;
        int redimidos = 0;
        for (Map.Entry<Cliente, int[]> e : puntosPorCliente(desde, hasta).entrySet()) {
            filas.add(e.getKey().getNombre() + ";" + e.getValue()[0] + ";" + e.getValue()[1]);
            acumulados += e.getValue()[0];
            redimidos += e.getValue()[1];
        }
        return armar("Puntos", desde, hasta, "cliente;acumulados;redimidos",
                filas, "TOTAL;" + acumulados + ";" + redimidos);
    }

    // Arma las 5 partes fijas del formato del contrato
    private String armar(String titulo, LocalDate desde, LocalDate hasta, String encabezado,
                         List<String> filas, String total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reporte: ").append(titulo).append("\n");
        sb.append("Generado: ").append(LocalDateTime.now().format(FORMATO)).append("\n");
        sb.append("Periodo: ").append(desde).append(" a ").append(hasta).append("\n");
        sb.append("Registros encontrados: ").append(filas.size()).append("\n");
        sb.append(encabezado).append("\n");
        for (String fila : filas) {
            sb.append(fila).append("\n");
        }
        sb.append(total);
        return sb.toString();
    }

    // ================= SERIES PARA GRAFICOS =================

    public Map<String, Double> serie(TipoReporte t, LocalDate desde, LocalDate hasta) {
        Map<String, Double> serie = new LinkedHashMap<>();
        switch (t) {
            case VENTAS_PERIODO -> {
                Map<String, Double> porDia = new TreeMap<>();
                for (Compra c : comprasValidas(desde, hasta)) {
                    porDia.merge(c.getFecha().toLocalDate().toString(), c.calcularTotal(), Double::sum);
                }
                serie.putAll(porDia);
            }
            case ENTRADAS_PELICULA -> {
                for (Map.Entry<String, double[]> e : porPelicula(desde, hasta).entrySet()) {
                    serie.put(e.getKey(), e.getValue()[0]);
                }
            }
            case OCUPACION_SALAS -> {
                for (Map.Entry<Integer, int[]> e : porSala(desde, hasta).entrySet()) {
                    int[] d = e.getValue();
                    serie.put("Sala " + e.getKey(), d[1] == 0 ? 0.0 : d[0] * 100.0 / d[1]);
                }
            }
            case PRODUCTOS_VENDIDOS -> {
                for (Map.Entry<String, double[]> e : porProducto(desde, hasta).entrySet()) {
                    serie.put(e.getKey(), e.getValue()[0]);
                }
            }
            case RECARGAS -> serie.putAll(montoPorDia(TipoMovimiento.RECARGA, desde, hasta));
            case CANCELACIONES_REEMBOLSOS -> serie.putAll(montoPorDia(TipoMovimiento.REEMBOLSO, desde, hasta));
            case PUNTOS -> {
                double acumulados = 0;
                double redimidos = 0;
                for (int[] d : puntosPorCliente(desde, hasta).values()) {
                    acumulados += d[0];
                    redimidos += d[1];
                }
                serie.put("Acumulados", acumulados);
                serie.put("Redimidos", redimidos);
            }
        }
        return serie;
    }

    private Map<String, Double> montoPorDia(TipoMovimiento tipo, LocalDate desde, LocalDate hasta) {
        Map<String, Double> porDia = new TreeMap<>();
        for (Cliente cl : clientes()) {
            for (Movimiento m : cl.getTarjeta().getMovimientos()) {
                if (m.getTipo() == tipo && enRango(m.getFecha().toLocalDate(), desde, hasta)) {
                    porDia.merge(m.getFecha().toLocalDate().toString(), m.getMonto(), Double::sum);
                }
            }
        }
        return porDia;
    }

    // ================= AUXILIARES =================

    // Solo compras PAGADA o UTILIZADA dentro del rango (incluye las dos fechas, RN22)
    private List<Compra> comprasValidas(LocalDate desde, LocalDate hasta) {
        List<Compra> lista = new ArrayList<>();
        for (Compra c : compras.listar()) {
            String estado = c.getEstado().getNombre();
            boolean valida = estado.equals("PAGADA") || estado.equals("UTILIZADA");
            if (valida && enRango(c.getFecha().toLocalDate(), desde, hasta)) {
                lista.add(c);
            }
        }
        lista.sort(Comparator.comparing(Compra::getFecha));
        return lista;
    }

    private boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private List<Cliente> clientes() {
        List<Cliente> lista = new ArrayList<>();
        for (Usuario u : usuarios.listar()) {
            if (u instanceof Cliente) {
                lista.add((Cliente) u);
            }
        }
        return lista;
    }

    // pelicula -> [entradas, total]
    private Map<String, double[]> porPelicula(LocalDate desde, LocalDate hasta) {
        Map<String, double[]> mapa = new TreeMap<>();
        for (Compra c : comprasValidas(desde, hasta)) {
            double[] d = mapa.computeIfAbsent(c.getFuncion().getPelicula().getTitulo(), k -> new double[2]);
            d[0] += c.getAsientos().size();
            d[1] += c.calcularTotal();
        }
        return mapa;
    }

    // producto -> [unidades, total]
    private Map<String, double[]> porProducto(LocalDate desde, LocalDate hasta) {
        Map<String, double[]> mapa = new TreeMap<>();
        for (Compra c : comprasValidas(desde, hasta)) {
            for (ItemConfiteria item : c.getConfiteria()) {
                double[] d = mapa.computeIfAbsent(item.getNombre(), k -> new double[2]);
                d[0] += 1;
                d[1] += item.getPrecio();
            }
        }
        return mapa;
    }

    // sala -> [vendidos, capacidad], solo funciones PROGRAMADAS del rango
    private Map<Integer, int[]> porSala(LocalDate desde, LocalDate hasta) {
        Map<Integer, int[]> mapa = new TreeMap<>();
        for (Funcion f : funciones.listar()) {
            if (f.getEstado() != EstadoFuncion.PROGRAMADA || !enRango(f.getFecha(), desde, hasta)) {
                continue;
            }
            int[] d = mapa.computeIfAbsent(f.getSala().getNumero(), k -> new int[2]);
            for (AsientoFuncion a : f.getAsientos()) {
                d[1]++;
                if (!a.estaLibre()) {
                    d[0]++;
                }
            }
        }
        return mapa;
    }

    // cliente -> [acumulados, redimidos] con las compras validas del rango
    private Map<Cliente, int[]> puntosPorCliente(LocalDate desde, LocalDate hasta) {
        Map<Cliente, int[]> mapa = new LinkedHashMap<>();
        for (Cliente cl : clientes()) {
            mapa.put(cl, new int[2]);
        }
        for (Compra c : comprasValidas(desde, hasta)) {
            for (Map.Entry<Cliente, int[]> e : mapa.entrySet()) {
                if (e.getKey().getId() == c.getCliente().getId()) {
                    e.getValue()[0] += c.getPuntosGanados();
                    e.getValue()[1] += c.getPuntosRedimidos();
                }
            }
        }
        return mapa;
    }

    private String dinero(double valor) {
        return String.format(Locale.US, "%.0f", valor);
    }

    private String porcentaje(int parte, int total) {
        double p = total == 0 ? 0 : parte * 100.0 / total;
        return String.format(Locale.US, "%.1f", p);
    }
}