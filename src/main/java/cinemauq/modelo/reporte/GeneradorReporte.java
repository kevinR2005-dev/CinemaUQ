package cinemauq.modelo.reporte;

public abstract class GeneradorReporte {

    protected abstract Reporte crearReporte();

    public final String emitir (String datos){
        Reporte reporte = crearReporte();
        return "===CinemaUQ===\n" + reporte.generar(datos) + "\n=== fin ===";
    }
}
