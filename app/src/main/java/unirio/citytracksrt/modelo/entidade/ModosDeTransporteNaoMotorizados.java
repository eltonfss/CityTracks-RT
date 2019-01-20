package unirio.citytracksrt.modelo.entidade;

import java.util.*;

public enum ModosDeTransporteNaoMotorizados {
    CAMINHANDO(0), BICICLETA(1);

    private int valor;
    private static Map<Integer,ModosDeTransporteNaoMotorizados> mapa = new HashMap<>();

    private ModosDeTransporteNaoMotorizados(int valor) {
        this.valor = valor;
    }

    static {
        for (ModosDeTransporteNaoMotorizados modoDeTransporteNaoMotorizado : ModosDeTransporteNaoMotorizados.values()) {
            mapa.put(modoDeTransporteNaoMotorizado.valor, modoDeTransporteNaoMotorizado);
        }
    }

    public static ModosDeTransporteNaoMotorizados valueOf(int modoDeTransporteNaoMotorizado) {
        return mapa.get(modoDeTransporteNaoMotorizado);
    }

    public int getValor() {
        return valor;
    }

}
