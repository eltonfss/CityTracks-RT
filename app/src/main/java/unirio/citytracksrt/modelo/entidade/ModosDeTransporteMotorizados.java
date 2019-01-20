package unirio.citytracksrt.modelo.entidade;


import java.util.*;

public enum ModosDeTransporteMotorizados{
    CARRO(0), MOTO(1), ONIBUS(2);

    private int valor;
    private static Map<Integer,ModosDeTransporteMotorizados> mapa = new HashMap<>();

    private ModosDeTransporteMotorizados(int valor) {
        this.valor = valor;
    }

    static {
        for (ModosDeTransporteMotorizados modoDeTransporteMotorizados : ModosDeTransporteMotorizados.values()) {
            mapa.put(modoDeTransporteMotorizados.valor, modoDeTransporteMotorizados);
        }
    }

    public static ModosDeTransporteMotorizados valueOf(int modoDeTransporteMotorizados) {
        return mapa.get(modoDeTransporteMotorizados);
    }

    public int getValor() {
        return valor;
    }
}
