package unirio.citytracksrt.modelo.entidade;

import java.util.*;

public enum TiposDeModosDeTransporte {
    MOTORIZADO(0), NAO_MOTORIZADO(1);

    private int valor;
    private static Map<Integer, TiposDeModosDeTransporte> mapa = new HashMap<>();

    private TiposDeModosDeTransporte(int valor) {
        this.valor = valor;
    }

    static {
        for (TiposDeModosDeTransporte tipoDeModoDeTransporte : TiposDeModosDeTransporte.values()) {
            mapa.put(tipoDeModoDeTransporte.valor, tipoDeModoDeTransporte);
        }
    }

    public static TiposDeModosDeTransporte valueOf(int modoDeTransporte) {
        return mapa.get(modoDeTransporte);
    }

    public int getValor() {
        return valor;
    }
}
