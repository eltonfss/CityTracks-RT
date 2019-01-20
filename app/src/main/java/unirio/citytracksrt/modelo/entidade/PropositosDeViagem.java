package unirio.citytracksrt.modelo.entidade;

import java.util.*;

public enum PropositosDeViagem {
        CASA(0), TRABALHO(1), EDUCACAO(2), COMPRAS(2), LAZER(3), OUTRO(4);

    private int valor;
    private static Map<Integer,PropositosDeViagem> mapa = new HashMap<>();

    private PropositosDeViagem(int valor) {
        this.valor = valor;
    }

    static {
        for (PropositosDeViagem propositosDeViagem : PropositosDeViagem.values()) {
            mapa.put(propositosDeViagem.valor, propositosDeViagem);
        }
    }

    public static PropositosDeViagem valueOf(int propositoDaViagem) {
        return mapa.get(propositoDaViagem);
    }

    public int getValor() {
        return valor;
    }
}
