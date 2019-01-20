package unirio.citytracksrt.modelo.entidade;

import android.location.*;

import java.util.*;

import static unirio.citytracksrt.utils.Utils.*;

public class LocalizacaoFactory {

    private Localizacao localizacao;

    public Localizacao constroiLocalizacao(String idDispositivo, PropositosDeViagem propositosDeViagem, ModosDeTransporte modoDeTransporteSelecionado, Location novaLocalizacao, Calendar timestampDaNovaLocalizacao, String modeloDispositivo, String versaoAndroid, Location localizacaoAtual, int totalDePontos, long diferencaEmSegundos, Localizacao pontoAtual){
        localizacao = new Localizacao();

        localizacao.setAltitude((float) novaLocalizacao.getAltitude());
        localizacao.setLatitude((float) novaLocalizacao.getLatitude());
        localizacao.setLongitude((float) novaLocalizacao.getLongitude());
        localizacao.setPrecisao(novaLocalizacao.getAccuracy());
        localizacao.setTimestampDaMedicao(timestampDaNovaLocalizacao);
        localizacao.setVelocidadeMedia(novaLocalizacao.getSpeed());
        localizacao.setModoDeTransporteColetado(modoDeTransporteSelecionado);
        localizacao.setPropositosDeViagem(propositosDeViagem);
        localizacao.setIdDispositivo(idDispositivo);
        localizacao.setModeloDispositivo(modeloDispositivo);
        localizacao.setVersaoAndroid(versaoAndroid);

        //So sera possivel calcular distancia e velocidade com 2 ou mais pontos
        if (totalDePontos > 0) {

            //calcular distancia, tempo e velocidade
            localizacao.setTempo(diferencaEmSegundos);
            localizacao.setDistancia(calcularDistancia(localizacaoAtual, novaLocalizacao));
            localizacao.setVelocidade(localizacao.getDistancia() / localizacao.getTempo());

            //calcular direção, dividindo por 10 e considerando somente a parte inteira para diminuir a sensibilidade
            localizacao.setDirecao(calcularDirecao(localizacaoAtual, novaLocalizacao));

            //so sera possivel calcular a aceleracao com 3 ou mais pontos
            if (totalDePontos > 1) {
                localizacao.setAceleracao(localizacao.getVelocidade() - pontoAtual.getVelocidade());
            }
        }
        return localizacao;
    }

    private int calcularDirecao(Location localizacaoAtual, Location novaLocalizacao) {
        return Math.round(localizacaoAtual.bearingTo(novaLocalizacao) / 10);
    }

}
