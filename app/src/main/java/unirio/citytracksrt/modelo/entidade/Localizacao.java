package unirio.citytracksrt.modelo.entidade;

import java.util.*;

/**
    Classe que representa um registro de localização
 */
public class Localizacao implements Comparable<Localizacao> {

    private String idDispositivo;

    private String modeloDispositivo;

    private String versaoAndroid;

    private Calendar timestampDaMedicao;

    private float altitude;
    private float latitude;
    private float longitude;

    private boolean isParada;

    //Direção em graus
    private int direcao;

    //velocidade em metros por segundo
    private float velocidade;

    //velocidade registrada pelo sensor
    private float velocidadeMedia;

    //precisao em metros
    private float precisao;

    //distancia em metros
    private float distancia;

    //tempo em segundos
    private long tempo;

    //aceleracao em metros^2/segundo
    private float aceleracao;

    private ModosDeTransporte modoDeTransporteColetado;

    private PropositosDeViagem propositosDeViagem;

    public Localizacao() {
        this.setIdDispositivo("");
        this.timestampDaMedicao = null;
        this.direcao = 0;
        this.altitude = 0;
        this.velocidade = 0;
        this.precisao = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.distancia = 0;
        this.tempo = 0;
        this.aceleracao = 0;
        this.setParada(false);
        this.modoDeTransporteColetado = null;
        this.propositosDeViagem = null;
    }

    public PropositosDeViagem getPropositosDeViagem() {
        return propositosDeViagem;
    }

    public void setPropositosDeViagem(PropositosDeViagem propositosDeViagem) {
        this.propositosDeViagem = propositosDeViagem;
    }

    public Calendar getTimestampDaMedicao() {
        return timestampDaMedicao;
    }

    public void setTimestampDaMedicao(Calendar timestampDaMedicao) {
        this.timestampDaMedicao = timestampDaMedicao;
    }

    public ModosDeTransporte getModoDeTransporteColetado() {
        return modoDeTransporteColetado;
    }

    public void setModoDeTransporteColetado(ModosDeTransporte modoDeTransporteColetado) {
        this.modoDeTransporteColetado = modoDeTransporteColetado;
    }

    public int getDirecao() {
        return direcao;
    }

    public void setDirecao(int direcao) {
        this.direcao = direcao;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(float velocidade) {
        this.velocidade = velocidade;
    }

    public float getPrecisao() {
        return precisao;
    }

    public void setPrecisao(float precisao) {
        this.precisao = precisao;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    public long getTempo() {
        return tempo;
    }

    public void setTempo(long tempo) {
        this.tempo = tempo;
    }

    public float getAceleracao() {
        return aceleracao;
    }

    public void setAceleracao(float aceleracao) {
        this.aceleracao = aceleracao;
    }

    public boolean isParada() {
        return isParada;
    }

    public void setParada(boolean parada) {
        isParada = parada;
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

    public float getVelocidadeMedia() {
        return velocidadeMedia;
    }

    public void setVelocidadeMedia(float velocidadeMedia) {
        this.velocidadeMedia = velocidadeMedia;
    }

    @Override
    public int compareTo(Localizacao localizacao) {
        if (timestampDaMedicao.before(localizacao.getTimestampDaMedicao())) {
            return -1;
        } else if (timestampDaMedicao.after(localizacao.getTimestampDaMedicao())) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;

        if (o == null || !(o instanceof Localizacao)) {
            return false;
        }

        Localizacao localizacao = (Localizacao) o;

        return idDispositivo.equals(localizacao.getIdDispositivo())
                && timestampDaMedicao.equals(localizacao.timestampDaMedicao);
                /*
                && velocidade == localizacao.getVelocidade()
                && aceleracao == localizacao.getAceleracao()
                && isParada == localizacao.isParada()
                && direcao == localizacao.getDirecao()
                && latitude == localizacao.getLatitude()
                && longitude == localizacao.getLongitude()
                && altitude == localizacao.getAltitude();*/
    }



}
