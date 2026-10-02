package cinemauq.modelo.reporte;

public class ReporteTxt implements Reporte {

    @Override
    public String generar (String datos){
        return "Reporte de ventas - CinemaUQ (TXT)\n" + datos.replace(";", "|");
    }
}
