package cinemauq.modelo.reporte;

public class GeneradorTxt extends GeneradorReporte{

    @Override
    protected Reporte crearReporte() {
        return new ReporteTxt();
        }
}
