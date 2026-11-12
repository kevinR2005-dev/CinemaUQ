package cinemauq.modelo.reporte;

public class ReporteCsv implements Reporte {

    // Los datos ya vienen con su encabezado: se devuelven tal cual
    @Override
    public String generar(String datos) {
        return datos;
    }
}