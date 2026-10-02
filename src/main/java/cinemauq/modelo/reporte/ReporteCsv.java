package cinemauq.modelo.reporte;

public class ReporteCsv implements Reporte{

    @Override
    public String generar (String datos) {

        return "factura; cliente; pelicula; asientos; total\n" + datos;
    }
}
