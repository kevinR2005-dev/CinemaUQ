package cinemauq.modelo.reporte;

public class ReporteTxt implements Reporte {

    // Sin título ni encabezado propios: los datos ya los traen
    @Override
    public String generar(String datos) {
        return datos.replace(";", " | ");
    }
}