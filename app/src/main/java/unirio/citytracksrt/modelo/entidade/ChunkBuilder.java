package unirio.citytracksrt.modelo.entidade;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static unirio.citytracksrt.utils.Utils.calcularDiferencaEmSegundos;

/*
 * Classe que utiliza o padrão Builder para simplificar a criação e manipulação do Chunk
 */
public class ChunkBuilder implements Serializable {

    //Maximo de localizacoes e tambem o tempo máximo de um chunkBuilder
    public static final int MAX_PONTOS = 90;

    //Mínimo de localizacoes que um chunkBuilder válido deve ter
    public static final int MIN_PONTOS = MAX_PONTOS / 10;

    // tempo máximo de parada em um chunkBuilder,
    // se o chunkBuilder tiver parada superior a este valor ele é isDescartado
    public static final int LIMITE_DE_PARADA = 20;

    // Velocidade mínima para que um ponto não seja considerado uma pausa
    public static final double LIMIAR_DE_DETECAO_DE_MOVIMENTO = 0.4;

    // precisão máxima em metros que uma localização pode ter
    public static final int LIMITE_DE_PRECISAO = 200;//20

    private Chunk chunk;
    private List<Localizacao> localizacoes;
    private int numeroDeInterpolacoes;
    private int totalDePontosCapturados;
    private boolean sumarizado;
    private Localizacao localizacaoDeInicioDaParada;
    private float tempoDeParada;

    /**
     * indica se o chunkBuilder atual foi isDescartado por violar algum dos thresholds
     */
    private boolean descartado;

    public ChunkBuilder(String idDispositivo, String modeloDispositivo, String versaoAndroid) {
        chunk = new Chunk();
        localizacoes = new ArrayList<Localizacao>();
        numeroDeInterpolacoes = 0;
        totalDePontosCapturados = 0;
        sumarizado = false;
        localizacaoDeInicioDaParada = null;
        tempoDeParada = 0;
        descartado = false;
        chunk.setIdDispositivo(idDispositivo);
        chunk.setModeloDispositivo(modeloDispositivo);
        chunk.setVersaoAndroid(versaoAndroid);
    }

    /**
     * Adiciona localizacao executando logica de interpolação de localizacoes com velocidade constante
     */
    public void adicionarPonto(Localizacao localizacao) {

        //se já houverem localizacoes na lista
        if (getLocalizacoes().size() > 0) {

            //Retorna o ultimo localizacao da lista
            Localizacao localizacaoAnterior = getLocalizacoes().get(getLocalizacoes().size() - 1);

            //copia o timeStamp do localizacaoAnterior
            Calendar timeStamp = (Calendar) localizacaoAnterior.getTimestampDaMedicao().clone();

            //calcula a diferenca em segundos entre o localizacao atual e o localizacao anterior
            long diferencaEmSegundos = calcularDiferencaEmSegundos(localizacao.getTimestampDaMedicao(), timeStamp);

            //insere novos localizacoes com velocidade igual a do localizacaoAnterior
            // até que a diferença entre todos os localizacoes seja de 1 segundo sem ultrapassar o limite de localizacoes
            while (diferencaEmSegundos > 1 && getLocalizacoes().size() < MAX_PONTOS) {

                //adiciona um segundo ao timestamp
                timeStamp.add(Calendar.SECOND, 1);

                // instancia um novo localizacao com base nas características do localizacaoAnterior
                Localizacao novaLocalizacao = new Localizacao();
                novaLocalizacao.setParada(localizacaoAnterior.isParada());
                novaLocalizacao.setTempo(1);
                novaLocalizacao.setVelocidade(localizacaoAnterior.getVelocidade());
                novaLocalizacao.setAceleracao(0);
                novaLocalizacao.setAltitude(localizacaoAnterior.getAltitude());
                novaLocalizacao.setDistancia(localizacaoAnterior.getDistancia());
                novaLocalizacao.setPrecisao(localizacaoAnterior.getPrecisao());
                novaLocalizacao.setTimestampDaMedicao(timeStamp);
                novaLocalizacao.setDirecao(localizacaoAnterior.getDirecao());

                //adiciona o novaLocalizacao na lista
                getLocalizacoes().add(novaLocalizacao);

                //incrementa o contador de interpolações

                numeroDeInterpolacoes = getNumeroDeInterpolacoes() + 1;

                //decrementa a diferença em segundos
                diferencaEmSegundos--;

                localizacaoAnterior = novaLocalizacao;

            }

            if (getLocalizacoes().size() < MAX_PONTOS) {
                localizacao.setParada(isParada(localizacao));
                localizacao.setTempo(calcularDiferencaEmSegundos(localizacao.getTimestampDaMedicao(), localizacaoAnterior.getTimestampDaMedicao()));
                getLocalizacoes().add(localizacao);
                totalDePontosCapturados = getTotalDePontosCapturados() + 1;
            }

        } else {
            //adicionar primeiro localizacao
            localizacao.setParada(isParada(localizacao));
            getLocalizacoes().add(localizacao);
            totalDePontosCapturados = getTotalDePontosCapturados() + 1;
        }


        //ordena os localizacoes por timeStamp
        Collections.sort(getLocalizacoes());

        chunk.setLocalizacoes(getLocalizacoes());
        chunk.setNumeroDeInterpolacoes(getNumeroDeInterpolacoes());
        chunk.setTotalDePontosCapturados(getTotalDePontosCapturados());

    }

    /**
     * calcula os atributos de sumarização do chunkBuilder com base na lista de localizacoes
     */
    public void sumarizar() {

        int numeroDeParadas = 0;
        int tempoDeParada = 0;
        float velocidadeMaxima = 0;
        float aceleracaoMaxima = 0;
        int numeroDeMudancasDeDirecao = 0;
        float velocidadeMedia = 0;
        float tempoDeParadaMedio = 0;

        //utiliza somente os 90 primeiros registros, os demais são descartados
        localizacoes = getLocalizacoes().subList(0, MAX_PONTOS);

        float velocidadeTotal = 0;

        Localizacao localizacaoAnterior = getLocalizacoes().get(0);

        //As informacoes foram calculadas no mesmo for para aumentar a performance

        for (Localizacao p : getLocalizacoes()) {

            if (p.isParada()) {
                if (!localizacaoAnterior.isParada()) {
                    numeroDeParadas++;
                }
                tempoDeParada++;
            } else {

                //processamento da velocidade maxima

                if (p.getVelocidade() > velocidadeMaxima) {
                    velocidadeMaxima = p.getVelocidade();
                }

                //processamento aceleracao maxima
                if (p.getAceleracao() > aceleracaoMaxima) {
                    aceleracaoMaxima = p.getAceleracao();
                }

                //processaento mudancas de direcao
                if (p.getDirecao() != localizacaoAnterior.getDirecao()) {
                    numeroDeMudancasDeDirecao++;
                }

                //processamento da velocidade media
                velocidadeTotal += p.getVelocidade();
                localizacaoAnterior = p;
            }

        }

        //processamento velocidade media
        velocidadeMedia = velocidadeTotal / getLocalizacoes().size();

        //processamento tempo de parada media
        if (numeroDeParadas > 0 && tempoDeParada > 0) {
            tempoDeParadaMedio = tempoDeParada / numeroDeParadas;
        }

        //atualiza os campos do Chunk
        chunk.setNumeroDeParadas(numeroDeParadas);
        chunk.setTempoDeParada(tempoDeParada);
        chunk.setVelocidadeMaxima(velocidadeMaxima);
        chunk.setAceleracaoMaxima(aceleracaoMaxima);
        chunk.setNumeroDeMudancasDeDirecao(numeroDeMudancasDeDirecao);
        chunk.setVelocidadeMedia(velocidadeMedia);
        chunk.setTempoDeParadaMedio(tempoDeParadaMedio);
        sumarizado = true;

    }

    public boolean isSumarizado() {
        return sumarizado;
    }

    public boolean isValido() {
        return getTotalDePontos() >= MIN_PONTOS;
    }

    public boolean isCheio() {
        return getTotalDePontos() >= MAX_PONTOS;
    }

    public boolean isDescartado() {
        return descartado;
    }

    private boolean isParada(Localizacao localizacao) {
        if (localizacao.getDistancia() < LIMIAR_DE_DETECAO_DE_MOVIMENTO) {
            adicionarParada(localizacao);
            return true;
        } else {
            if (getLocalizacaoDeInicioDaParada() != null) {
                localizacaoDeInicioDaParada = null;
            }
        }
        return false;
    }

    private void adicionarParada(Localizacao localizacao) {
        //verifica o ja existe uma parada anterior a esse localizacao
        if (getLocalizacaoDeInicioDaParada() == null) {
            localizacaoDeInicioDaParada = localizacao;
        } else {
            //verifica se o limite de tempo parado foi atingido e caso seja descarta o chunkBuilder atual e inicia um novo chunkBuilder
            verificarLimiteDeParada(localizacao);
        }
    }

    private void verificarLimiteDeParada(Localizacao localizacao) {
        tempoDeParada = calcularDiferencaEmSegundos(localizacao.getTimestampDaMedicao(), getLocalizacaoDeInicioDaParada().getTimestampDaMedicao());
        if (getTempoDeParada() > LIMITE_DE_PARADA) {
            descartado = true;
        } else {
            descartado = false;
        }
    }

    public int getTotalDePontos() {
        return getNumeroDeInterpolacoes() + getTotalDePontosCapturados();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setModoDeTransporteColetado(ModosDeTransporte modoDeTransporteColetado) {
        chunk.setModoDeTransporteColetado(modoDeTransporteColetado);
    }

    public List<Localizacao> getLocalizacoes() {
        return localizacoes;
    }

    public int getNumeroDeInterpolacoes() {
        return numeroDeInterpolacoes;
    }

    public int getTotalDePontosCapturados() {
        return totalDePontosCapturados;
    }

    /**
     * Armazena o ponto da ultima parada
     */
    public Localizacao getLocalizacaoDeInicioDaParada() {
        return localizacaoDeInicioDaParada;
    }

    /**
     * contador da parada atual
     */
    public float getTempoDeParada() {
        return tempoDeParada;
    }
}
