package cinemauq.servicio;

import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.EstadoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.tarjeta.Movimiento;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EstadisticasServicio {

    private final Repositorio<Compra, Integer> compras;
    private final Repositorio<Funcion, Integer> funciones;
    private final Repositorio<Usuario, Integer> usuarios;

    public EstadisticasServicio(Repositorio<Compra, Integer> compras,
                                Repositorio<Funcion, Integer> funciones,
                                Repositorio<Usuario, Integer> usuarios) {
        this.compras = compras;
        this.funciones = funciones;
        this.usuarios = usuarios;
    }

    public double ventasTotales(LocalDate desde, LocalDate hasta) {
        double suma = 0;
        for (Compra c : comprasValidas()) {
            if (enRango(c.getFecha().toLocalDate(), desde, hasta)) {
                suma += c.calcularTotal();
            }
        }
        return suma;
    }

    // sala -> porcentaje de ocupación (funciones PROGRAMADAS del rango)
    public Map<Integer, Double> ocupacionPorSala(LocalDate desde, LocalDate hasta) {
        Map<Integer, int[]> acum = new TreeMap<>();
        for (Funcion f : funciones.listar()) {
            if (f.getEstado() != EstadoFuncion.PROGRAMADA || !enRango(f.getFecha(), desde, hasta)) {
                continue;
            }
            int[] d = acum.computeIfAbsent(f.getSala().getNumero(), k -> new int[2]);
            for (AsientoFuncion a : f.getAsientos()) {
                d[1]++;
                if (!a.estaLibre()) {
                    d[0]++;
                }
            }
        }
        Map<Integer, Double> resultado = new LinkedHashMap<>();
        for (Map.Entry<Integer, int[]> e : acum.entrySet()) {
            int[] d = e.getValue();
            resultado.put(e.getKey(), d[1] == 0 ? 0.0 : d[0] * 100.0 / d[1]);
        }
        return resultado;
    }

    public List<String> peliculasMasVendidas(int n) {
        Map<String, Integer> conteo = new TreeMap<>();
        for (Compra c : comprasValidas()) {
            conteo.merge(c.getFuncion().getPelicula().getTitulo(), c.getAsientos().size(), Integer::sum);
        }
        return ranking(conteo, n, "entradas");
    }

    public List<String> productosMasVendidos(int n) {
        Map<String, Integer> conteo = new TreeMap<>();
        for (Compra c : comprasValidas()) {
            for (ItemConfiteria item : c.getConfiteria()) {
                conteo.merge(item.getNombre(), 1, Integer::sum);
            }
        }
        return ranking(conteo, n, "unidades");
    }

    public double totalReembolsos(LocalDate desde, LocalDate hasta) {
        double suma = 0;
        for (Usuario u : usuarios.listar()) {
            if (u instanceof Cliente) {
                for (Movimiento m : ((Cliente) u).getTarjeta().getMovimientos()) {
                    if (m.getTipo() == TipoMovimiento.REEMBOLSO
                            && enRango(m.getFecha().toLocalDate(), desde, hasta)) {
                        suma += m.getMonto();
                    }
                }
            }
        }
        return suma;
    }

    // De mayor a menor, formato "Nombre (N unidad)"
    private List<String> ranking(Map<String, Integer> conteo, int n, String unidad) {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(conteo.entrySet());
        lista.sort((a, b) -> b.getValue() - a.getValue());
        List<String> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(n, lista.size()); i++) {
            resultado.add(lista.get(i).getKey() + " (" + lista.get(i).getValue() + " " + unidad + ")");
        }
        return resultado;
    }

    private List<Compra> comprasValidas() {
        List<Compra> lista = new ArrayList<>();
        for (Compra c : compras.listar()) {
            String estado = c.getEstado().getNombre();
            if (estado.equals("PAGADA") || estado.equals("UTILIZADA")) {
                lista.add(c);
            }
        }
        return lista;
    }

    private boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }
}