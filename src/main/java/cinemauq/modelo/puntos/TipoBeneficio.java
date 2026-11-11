package cinemauq.modelo.puntos;

public enum TipoBeneficio {

    ENTRADA (500),
    PRODUCTO (150),
    COMBO (400);

    private final int costo;

    TipoBeneficio (int costo){
        this.costo = costo;
    }

    public int getCosto(){
        return costo;
    }

}
