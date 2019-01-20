package unirio.citytracksrt.modelo.entidade;

import java.util.*;

/**
 * Classe que representa um chunkBuilder, estrutura que agrupa um conjunto de localizacoes e calcula as informações de sumarização necessárias
 * para utilizar as técnicas de classificação de modo de transporte
 */
public class Chunk {

    //Identificado do Dispositivo utilizado para coleta
    private String idDispositivo;

    //Modelo do smartphone
    private String modeloDispositivo;

    //Versao do SO
    private String versaoAndroid;

    //Lista de localizacoes do chunkBuilder
    private List<Localizacao> localizacoes;

    //campos calculados com base nos localizacoes armazenados na lista
    private float velocidadeMaxima;
    private float velocidadeMedia;
    private int numeroDeParadas;
    private int tempoDeParada;
    private int numeroDeMudancasDeDirecao;
    private float aceleracaoMaxima;
    private float tempoDeParadaMedio; //Sugerido
    private int numeroDeInterpolacoes;//Sugerido
    private int totalDePontosCapturados;

    //campo para coleta do modo de transporte
    private ModosDeTransporte modoDeTransporteColetado;
    private ModosDeTransporte modoDeTransporteRandomForest;
    private ModosDeTransporte modoDeTransporteRedeNeural;
    private TiposDeModosDeTransporte modoDeTransporteSMO;
    private ModosDeTransporteNaoMotorizados modosDeTransporteNaoMotorizadosBayesNet;
    private ModosDeTransporteNaoMotorizados modosDeTransporteNaoMotorizadosDecisionTable;
    private ModosDeTransporteMotorizados modosDeTransporteMotorizadosDecisionTable;

    public Chunk() {
        setVelocidadeMaxima(0);
        setVelocidadeMedia(0);
        setNumeroDeParadas(0);
        setNumeroDeMudancasDeDirecao(0);
        setLocalizacoes(new ArrayList<Localizacao>());
        setAceleracaoMaxima(0);
        setTempoDeParadaMedio(0);
        setNumeroDeInterpolacoes(0);
        setTempoDeParada(0);
        setTotalDePontosCapturados(0);
        modoDeTransporteColetado = ModosDeTransporte.CAMINHANDO;
        modoDeTransporteRedeNeural = ModosDeTransporte.CAMINHANDO;
        modoDeTransporteRandomForest = ModosDeTransporte.CAMINHANDO;
    }

    public float getVelocidadeMaxima() {
        return velocidadeMaxima;
    }

    public float getVelocidadeMedia() {
        return velocidadeMedia;
    }

    public int getTempoDeParada() {
        return tempoDeParada;
    }

    public int getNumeroDeMudancasDeDirecao() {
        return numeroDeMudancasDeDirecao;
    }

    public List<Localizacao> getLocalizacoes() {
        return localizacoes;
    }

    public int getNumeroDeParadas() {
        return numeroDeParadas;
    }

    public float getAceleracaoMaxima() {
        return aceleracaoMaxima;
    }

    public float getTempoDeParadaMedio() {
        return tempoDeParadaMedio;
    }

    public int getNumeroDeInterpolacoes() {
        return numeroDeInterpolacoes;
    }

    public int getTotalDePontosCapturados() {
        return totalDePontosCapturados;
    }

    public ModosDeTransporte getModoDeTransporteColetado() {
        return modoDeTransporteColetado;
    }

    public void setModoDeTransporteColetado(ModosDeTransporte modoDeTransporteColetado) {
        this.modoDeTransporteColetado = modoDeTransporteColetado;
    }

    public void setNumeroDeInterpolacoes(int numeroDeInterpolacoes) {
        this.numeroDeInterpolacoes = numeroDeInterpolacoes;
    }

    public void setLocalizacoes(List<Localizacao> localizacoes) {
        this.localizacoes = localizacoes;
    }

    public void setTotalDePontosCapturados(int totalDePontosCapturados) {
        this.totalDePontosCapturados = totalDePontosCapturados;
    }

    public void setVelocidadeMaxima(float velocidadeMaxima) {
        this.velocidadeMaxima = velocidadeMaxima;
    }

    public void setVelocidadeMedia(float velocidadeMedia) {
        this.velocidadeMedia = velocidadeMedia;
    }

    public void setNumeroDeParadas(int numeroDeParadas) {
        this.numeroDeParadas = numeroDeParadas;
    }

    public void setTempoDeParada(int tempoDeParada) {
        this.tempoDeParada = tempoDeParada;
    }

    public void setNumeroDeMudancasDeDirecao(int numeroDeMudancasDeDirecao) {
        this.numeroDeMudancasDeDirecao = numeroDeMudancasDeDirecao;
    }

    public void setAceleracaoMaxima(float aceleracaoMaxima) {
        this.aceleracaoMaxima = aceleracaoMaxima;
    }

    public void setTempoDeParadaMedio(float tempoDeParadaMedio) {
        this.tempoDeParadaMedio = tempoDeParadaMedio;
    }

    public ModosDeTransporte getModoDeTransporteRedeNeural() {
        return modoDeTransporteRedeNeural;
    }
    public ModosDeTransporte getModoDeTransporteRandomForest() {
        return modoDeTransporteRandomForest;
    }

    public void setModoDeTransporteRedeNeural(ModosDeTransporte modoDeTransporteRedeNeural) {
        this.modoDeTransporteRedeNeural = modoDeTransporteRedeNeural;
    }
    public void setModoDeTransporteRandomForest(ModosDeTransporte modoDeTransporteRandomForest) {
        this.modoDeTransporteRandomForest = modoDeTransporteRandomForest;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public TiposDeModosDeTransporte getModoDeTransporteSMO() {
        return modoDeTransporteSMO;
    }

    public void setModoDeTransporteSMO(TiposDeModosDeTransporte modoDeTransporteSMO) {
        this.modoDeTransporteSMO = modoDeTransporteSMO;
    }

    public ModosDeTransporteNaoMotorizados getModosDeTransporteNaoMotorizadosBayesNet() {
        return modosDeTransporteNaoMotorizadosBayesNet;
    }

    public void setModosDeTransporteNaoMotorizadosBayesNet(ModosDeTransporteNaoMotorizados modosDeTransporteNaoMotorizadosBayesNet) {
        this.modosDeTransporteNaoMotorizadosBayesNet = modosDeTransporteNaoMotorizadosBayesNet;
    }

    public ModosDeTransporteNaoMotorizados getModosDeTransporteNaoMotorizadosDecisionTable() {
        return modosDeTransporteNaoMotorizadosDecisionTable;
    }

    public void setModosDeTransporteNaoMotorizadosDecisionTable(ModosDeTransporteNaoMotorizados modosDeTransporteNaoMotorizadosDecisionTable) {
        this.modosDeTransporteNaoMotorizadosDecisionTable = modosDeTransporteNaoMotorizadosDecisionTable;
    }

    public ModosDeTransporteMotorizados getModosDeTransporteMotorizadosDecisionTable() {
        return modosDeTransporteMotorizadosDecisionTable;
    }

    public void setModosDeTransporteMotorizadosDecisionTable(ModosDeTransporteMotorizados modosDeTransporteMotorizadosDecisionTable) {
        this.modosDeTransporteMotorizadosDecisionTable = modosDeTransporteMotorizadosDecisionTable;
    }

    public String getModeloDispositivo() {
        return modeloDispositivo;
    }

    public void setModeloDispositivo(String modeloDispositivo) {
        this.modeloDispositivo = modeloDispositivo;
    }

    public String getVersaoAndroid() {
        return versaoAndroid;
    }

    public void setVersaoAndroid(String versaoAndroid) {
        this.versaoAndroid = versaoAndroid;
    }

    @Override
    public String toString() {
        return getVelocidadeMaxima() + "," + getAceleracaoMaxima() + "," + getNumeroDeMudancasDeDirecao() + ", " + modoDeTransporteColetado.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;

        if (o == null || !(o instanceof Chunk)) {
            return false;
        }

        Chunk chunk = (Chunk) o;

        return idDispositivo.equals(chunk.getIdDispositivo())
                && velocidadeMaxima == chunk.getVelocidadeMaxima()
                && velocidadeMedia == chunk.getVelocidadeMedia()
                && aceleracaoMaxima == chunk.getAceleracaoMaxima()
                && numeroDeParadas == chunk.getNumeroDeParadas()
                && tempoDeParada == chunk.getTempoDeParada()
                && tempoDeParadaMedio == chunk.getTempoDeParadaMedio()
                && numeroDeInterpolacoes == chunk.getNumeroDeInterpolacoes()
                && totalDePontosCapturados == chunk.getTotalDePontosCapturados();
    }

}
