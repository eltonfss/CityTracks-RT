package unirio.citytracksrt.modelo.entidade;

import java.util.HashMap;
import java.util.Map;

public enum ModosDeTransporte {
    CAMINHANDO(0), BICICLETA(1), CARRO(2), MOTO(3), ONIBUS(4);

    private int valor;
    private static Map<Integer,ModosDeTransporte> mapa = new HashMap<>();

    private ModosDeTransporte(int valor) {
        this.valor = valor;
    }

    static {
        for (ModosDeTransporte modoDeTransporte : ModosDeTransporte.values()) {
            mapa.put(modoDeTransporte.valor, modoDeTransporte);
        }
    }

    public static ModosDeTransporte valueOf(int modoDeTransporte) {
        return mapa.get(modoDeTransporte);
    }

    public int getValor() {
        return valor;
    }
}
