package cinemauq.modelo.reporte;

public class GeneradorCsv extends GeneradorReporte {

    @Override
    protected Reporte crearReporte(){
        return new ReporteCsv();
    }

}
