package unirio.citytracksrt.utils.inferencia;


import android.content.*;
import android.support.annotation.*;

import java.util.*;

import unirio.citytracksrt.modelo.entidade.*;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.*;
import weka.core.converters.*;

public class Classificador {

	private final String TRAIN_REDE_NEURAL_FILENAME = "chunksTreinamentoRedeNeural.arff";
	private final String TRAIN_SMO_FILENAME = "chunksTreinamentoSMO.arff";
	private final String TRAIN_MOTORIZADO_FILENAME = "chunksTreinamentoMotorizado.arff";
	private final String TRAIN_NAO_MOTORIZADO_FILENAME = "chunksTreinamentoNaoMotorizado.arff";

	private Classifier randomForest;
	private Classifier multilayerPerceptron;
	private Classifier smo;
	private Classifier decisionTableMotorizado;
	private Classifier decisionTableNaoMotorizado;
	private Classifier bayesNet;

	private Instances trainModosDeTransporte;
	private Instances trainTiposDeModosDeTransporte;
	private Instances trainModosDeTransporteMotorizados;
	private Instances trainModosDeTransporteNaoMotorizados;

	public Classificador(){
			randomForest = new RandomForest();
			multilayerPerceptron = new MultilayerPerceptron();
			smo = new SMO();
			decisionTableMotorizado = new DecisionTable();
			decisionTableNaoMotorizado = new DecisionTable();
			bayesNet = new BayesNet();
	}

	public Chunk classificar(Chunk chunk) throws Exception {

		chunk.setModoDeTransporteRandomForest(ModosDeTransporte.valueOf(classificarPorRandomForest(chunk)));
		chunk.setModoDeTransporteRedeNeural(ModosDeTransporte.valueOf(classificarPorRedeNeural(chunk)));
		chunk.setModoDeTransporteSMO(TiposDeModosDeTransporte.valueOf(classificarPorSMO(chunk)));

		if(chunk.getModoDeTransporteSMO().toString().equals(TiposDeModosDeTransporte.MOTORIZADO.toString())){
			chunk.setModosDeTransporteMotorizadosDecisionTable(ModosDeTransporteMotorizados.valueOf(classificarPorDecisionTableMotorizado(chunk)));
		}else if(chunk.getModoDeTransporteSMO().toString().equals(TiposDeModosDeTransporte.NAO_MOTORIZADO.toString())){
			chunk.setModosDeTransporteNaoMotorizadosBayesNet(ModosDeTransporteNaoMotorizados.valueOf(classificarPorBayesNetNaoMotorizado(chunk)));
			chunk.setModosDeTransporteNaoMotorizadosDecisionTable(ModosDeTransporteNaoMotorizados.valueOf(classificarPorDecisionTableNaoMotorizado(chunk)));
		}

		return chunk;

	}

	public String classificarPorRandomForest(Chunk chunk) throws Exception {

		String[] classes = new String[ModosDeTransporte.values().length];

		int i = 0;
		for (ModosDeTransporte m :	ModosDeTransporte.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = randomForest.classifyInstance(data.firstInstance());

		return trainModosDeTransporte.classAttribute().value((int) clsLabel);

	}

	public String classificarPorRedeNeural(Chunk chunk) throws Exception {

		String[] classes = new String[ModosDeTransporte.values().length];

		int i = 0;
		for (ModosDeTransporte m :	ModosDeTransporte.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = multilayerPerceptron.classifyInstance(data.firstInstance());

		return trainModosDeTransporte.classAttribute().value((int) clsLabel);

	}

	public String classificarPorSMO(Chunk chunk) throws Exception {

		String[] classes = new String[TiposDeModosDeTransporte.values().length];

		int i = 0;
		for (TiposDeModosDeTransporte m :TiposDeModosDeTransporte.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = smo.classifyInstance(data.firstInstance());

		return trainTiposDeModosDeTransporte.classAttribute().value((int) clsLabel);

	}

	public String classificarPorDecisionTableMotorizado(Chunk chunk) throws Exception {

		String[] classes = new String[ModosDeTransporteMotorizados.values().length];

		int i = 0;
		for (ModosDeTransporteMotorizados m :ModosDeTransporteMotorizados.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = decisionTableMotorizado.classifyInstance(data.firstInstance());

		return trainModosDeTransporteMotorizados.classAttribute().value((int) clsLabel);

	}

	public String classificarPorDecisionTableNaoMotorizado(Chunk chunk) throws Exception {

		String[] classes = new String[ModosDeTransporteNaoMotorizados.values().length];

		int i = 0;
		for (ModosDeTransporteNaoMotorizados m :ModosDeTransporteNaoMotorizados.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = decisionTableNaoMotorizado.classifyInstance(data.firstInstance());

		return trainModosDeTransporteNaoMotorizados.classAttribute().value((int) clsLabel);

	}

	public String classificarPorBayesNetNaoMotorizado(Chunk chunk) throws Exception {

		String[] classes = new String[ModosDeTransporteNaoMotorizados.values().length];

		int i = 0;
		for (ModosDeTransporteNaoMotorizados m :ModosDeTransporteNaoMotorizados.values()) {
			classes[i++] = m.toString();
		}

		Instances data = getInstances(chunk, classes);

		double clsLabel = bayesNet.classifyInstance(data.firstInstance());

		return trainModosDeTransporteNaoMotorizados.classAttribute().value((int) clsLabel);

	}

	@NonNull
	public Instances getInstances(Chunk chunk, String[] classes) {
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2);

		Attribute velocidadeMaxima = new Attribute("velocidade_maxima");
		Attribute aceleracaoMaxima = new Attribute("aceleracao_maxima");
		Attribute numeroDeMudancasDeDirecao = new Attribute("mudancas_de_direcao");

		ArrayList<String> classVal = new ArrayList<String>();
		for (String classe : classes) {
			classVal.add(classe);
		}

		Attribute modoDeTransporte = new Attribute("modo_de_transporte", classVal);

		attributeList.add(velocidadeMaxima);
		attributeList.add(aceleracaoMaxima);
		attributeList.add(numeroDeMudancasDeDirecao);
		attributeList.add(modoDeTransporte);

		Instances data = new Instances("chunks", attributeList, 0);

		Instance predictInstance = new DenseInstance(data.numAttributes());
		predictInstance.setValue(velocidadeMaxima, chunk.getVelocidadeMaxima());
		predictInstance.setValue(aceleracaoMaxima, chunk.getAceleracaoMaxima());
		predictInstance.setValue(numeroDeMudancasDeDirecao, chunk.getNumeroDeMudancasDeDirecao());

		data.add(predictInstance);
		data.setClassIndex(data.numAttributes()-1);
		return data;
	}

	public void treinarAlgoritmos(Context contexto) throws Exception {

		treinarRandomForest(contexto);

		treinarRedeNeural(contexto);

		treinarSMO(contexto);

		treinarMotorizado(contexto);

		treinarNaoMotorizado(contexto);

	}

	public void treinarNaoMotorizado(Context contexto) throws Exception {

		ConverterUtils.DataSource source = new ConverterUtils.DataSource(contexto.getAssets().open(TRAIN_NAO_MOTORIZADO_FILENAME));
		trainModosDeTransporteNaoMotorizados = source.getDataSet();
		trainModosDeTransporteNaoMotorizados.setClassIndex(trainModosDeTransporteNaoMotorizados.numAttributes() - 1);

		//treinar decision table
		decisionTableNaoMotorizado.buildClassifier(trainModosDeTransporteNaoMotorizados);

		//treinar bayes net
		bayesNet.buildClassifier(trainModosDeTransporteNaoMotorizados);
	}

	public void treinarMotorizado(Context contexto) throws Exception {
		ConverterUtils.DataSource source = new ConverterUtils.DataSource(contexto.getAssets().open(TRAIN_MOTORIZADO_FILENAME));
		trainModosDeTransporteMotorizados = source.getDataSet();
		trainModosDeTransporteMotorizados.setClassIndex(trainModosDeTransporteMotorizados.numAttributes() - 1);

		//treinar decision table
		decisionTableMotorizado.buildClassifier(trainModosDeTransporteMotorizados);
	}

	public void treinarSMO(Context contexto) throws Exception {
		ConverterUtils.DataSource source = new ConverterUtils.DataSource(contexto.getAssets().open(TRAIN_SMO_FILENAME));
		trainTiposDeModosDeTransporte = source.getDataSet();
		trainTiposDeModosDeTransporte.setClassIndex(trainTiposDeModosDeTransporte.numAttributes() - 1);

		//treinar smo
		smo.buildClassifier(trainTiposDeModosDeTransporte);
	}

	public void treinarRedeNeural(Context contexto) throws Exception {
		ConverterUtils.DataSource source = new ConverterUtils.DataSource(contexto.getAssets().open(TRAIN_REDE_NEURAL_FILENAME));
		trainModosDeTransporte = source.getDataSet();
		trainModosDeTransporte.setClassIndex(trainModosDeTransporte.numAttributes() - 1);

		//treinar multilayer perceptron
		MultilayerPerceptron mp = new MultilayerPerceptron();
		mp.setLearningRate(0.1);
		mp.setMomentum(0.2);
		mp.setTrainingTime(2000);
		mp.setHiddenLayers("3");
		mp.buildClassifier(trainModosDeTransporte);

		multilayerPerceptron = mp;
	}

	public void treinarRandomForest(Context contexto) throws Exception {
		ConverterUtils.DataSource source = new ConverterUtils.DataSource(contexto.getAssets().open(TRAIN_REDE_NEURAL_FILENAME));
		trainModosDeTransporte = source.getDataSet();
		trainModosDeTransporte.setClassIndex(trainModosDeTransporte.numAttributes() - 1);

		//treinar random forest
		randomForest.buildClassifier(trainModosDeTransporte);
	}

}
