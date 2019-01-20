package unirio.citytracksrt.controle;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.*;
import java.util.*;

import unirio.citytracksrt.R;
import unirio.citytracksrt.modelo.entidade.*;
import unirio.citytracksrt.modelo.persistencia.*;


public class MainActivity extends FragmentActivity implements
         ResultCallback<LocationSettingsResult>, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {


    /**
     * O intervalo desejado para as atualizações de localização. Inexato. Atualizações podem ser mais ou menos frequentes.
     */
    public static final long INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS = 1000;

    /**
     * A taxa mais alta para atualizações de localização ativas. Exata. Atualizações nunca serão mais frequentes que este valor
     */
    public static final long INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS = INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS;

    protected static final String TAG = "city-tracks-rt";

    // Chave para armazenamento do estado da atividade no Bundle
    protected final static String CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO = "requesting-location-updates-key";
    protected final static String CHAVE_DE_LOCALIZACAO = "location-key";
    protected final static String CHAVE_STRING_ULTIMA_HORA_ATUALIZADA = "last-updated-time-string-key";
    protected final static String CHAVE_MODO_DE_TRANSPORTE_SELECIONADO = "last-selected-travel-mode";
    protected final static String CHAVE_PROPOSITO_DE_VIAGEM_SELECIONADO = "last-selected-travel-purpose";

    public static final  String CITYTRACKSRT_RESULT = "unirio.citytracksrt.REQUEST_PROCESSED";

    public static final  String CITYTRACKSRT_MESSAGE = "unirio.citytracksrt.CITYTRACKSRT_MSG";

    /**
     * Constante utilizada na janela de dialogo de configurações de localização
     */
    protected static final int CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO = 0x1;

    protected JSONDAO<Integer> chunkCounterDAO;

    protected JSONDAO<Integer> locationCounterDAO;

    protected JSONDAO<String> enderecoServidorDAO;


    // UI Widgets.
    protected Button iniciarAtualizacoesButton;
    protected Button pararAtualizacoesButton;
    protected Button enviarDadosColetadosButton;
    protected Button caminhandoButton;
    protected Button bicicletaButton;
    protected Button onibusButton;
    protected Button carroButton;
    protected Button motoButton;
    protected Button casaButton;
    protected Button trabalhoButton;
    protected Button educacaoButton;
    protected Button comprasButton;
    protected Button lazerButton;
    protected Button outroButton;
    protected TextView horaDaUltimaAtualizacaoTextView;
    protected TextView latitudeTextView;
    protected TextView longitudeTextView;
    protected TextView velocidadeMaximaTextView;
    protected TextView velocidadeMediaTextView;
    protected TextView numeroDeParadasTextView;
    protected TextView tempoDeParadaTextView;
    protected TextView aceleracaoMaximaTextView;
    protected TextView mudancasDeDirecaoTextView;
    protected TextView numeroDeInterpolacoesTextView;
    protected TextView tempoDeParadaMedioTextView;
    protected TextView totalDePontosTextView;
    protected TextView totalDePontosCapturadosTextView;
    protected Spinner modosDeTransporteSpinner;
    protected TextView idDispositivoTextView;
    protected TextView selecionarModoDeTransporteTextView;
    protected TextView selecionarPropositoDeViagemTextView;
    protected EditText enderecoServidorEditText;

    // Labels.
    protected String latitudeLabel;
    protected String longitudeLabel;
    protected String horaDaUltimaAtualizacaoLabel;
    protected String velocidadeMaximaLabel;
    protected String velocidadeMediaLabel;
    protected String numeroDeParadasLabel;
    protected String tempoDeParadaLabel;
    protected String aceleracaoMaximaLabel;
    protected String mudancasDeDirecaoLabel;
    protected String numeroDeInterpolacoesLabel;
    protected String tempoDeParadaMedioLabel;
    protected String totalDePontosLabel;
    protected String totalDePontosCapturadosLabel;
    protected MapFragment mMapFragment;
    protected Marker marker;

    /**
     * Prove um ponto de entrada para os serviços Google Play
     */
    protected GoogleApiClient clienteDaGoogleApi;

    /**
     * Armazena parâmetros de requisições para o FusedLocationProviderApi
     */
    protected LocationRequest requisicaoDeLocalizacao;

    /**
     * Armazena parametros das requisicoes de configurações de localização
     */
    private LocationSettingsRequest requisicaoDeConfiguracaoDeLocalizacao;

    /**
     * Representa uma localização geográfica
     */
    protected Location localizacaoAtual;

    /**
     * Rastreia o status da requisição de atualizações de localização.
     * O valor é modificado qunado o usuário pressiona os botões Inicar Atualizações ou Parar Atualizações
     */
    protected Boolean solicitandoAtualizacoesDeLocalizacao;

    /**
     * Hora quando a localização foi atualizada representada como uma String
     */
    protected String horaDaUltimaAtualizacao;

    /**
     * armazena a opção de modo de transporte selecionada pelo usuário
     */
    private ModosDeTransporte modoDeTransporteSelecionado;

    private PropositosDeViagem propositoDeViagemSelecionado;

    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcaster;
    private Intent servico;
    protected GoogleMap googleMap;
    protected Bundle instanceState;
    private Integer chunkCounter;
    private Integer locationCounter;
    private String enderecoServidor;
    private TextView totalChunksColetadosTextView;
    private TextView totalLocalizacoesColetadasTextView;



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(unirio.citytracksrt.R.layout.main_activity);

        if(clienteDaGoogleApi == null) {

            solicitandoAtualizacoesDeLocalizacao = false;
            horaDaUltimaAtualizacao = "";

            localizacaoAtual = null;
            chunkCounterDAO = new JSONDAO<>(this, "chunkCounter");
            locationCounterDAO = new JSONDAO<>(this, "locationCounter");
            enderecoServidorDAO = new JSONDAO<>(this, "enderecoServidor");

            carregarContadores();

            carregarServidor();


            localizarOsUIWidgets();

            configurarLabels();

            construirGoogleApiClient();
            verificarConfiguracoesDeLocalizacao();

            // Atualiza os valores utilizando os dados armazenados no Bundle
            atualizarValoresComBundle(savedInstanceState);
            instanceState = savedInstanceState;

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    atualizarValoresComBundle(intent.getExtras());
                }
            };

            broadcaster = LocalBroadcastManager.getInstance(this);


        }
    }

    private void carregarServidor() {
        try {
            List<String> listString = null;
            listString = enderecoServidorDAO.retrieveAll(String.class);
            if(listString.size() == 1){
                enderecoServidor = listString.get(0);
            }else{
                enderecoServidor = "http://citytracks.intelliurb.org/citytracks-rt/";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotification() {

        String modoDeTransporte = "Selecionar";

        String propositoDeViagem = "Selecionar";

        if(modoDeTransporteSelecionado != null) {
            modoDeTransporte = modoDeTransporteSelecionado.toString();
        }

        if(propositoDeViagemSelecionado != null){
            propositoDeViagem = propositoDeViagemSelecionado.toString();
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("CityTracks-RT")
                        .setContentText("Modo: " + modoDeTransporte + " - Propósito: " + propositoDeViagem)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    /**
     * Constroi um GoogleApiClient. Utiliza o método {@code #addApi} para efetuar requisições a LocationServices API
     */
    protected synchronized void construirGoogleApiClient() {
//        Log.i(TAG, "Construindo GoogleApiClient");
        clienteDaGoogleApi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarConfiguracoesDeLocalizacao();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(CityTracksRTService.CITYTRACKSRT_RESULT)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarValoresComBundle(instanceState);
        atualizarUI();
    }

    @Override
    protected void onPause() {
        if (modoDeTransporteSelecionado != null && propositoDeViagemSelecionado != null && instanceState != null){
            instanceState.putSerializable(CHAVE_MODO_DE_TRANSPORTE_SELECIONADO, modoDeTransporteSelecionado.toString());
            instanceState.putSerializable(CHAVE_PROPOSITO_DE_VIAGEM_SELECIONADO, modoDeTransporteSelecionado.toString());
            createNotification();
        }
        super.onPause();

    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        persistirContadoresEEnderecoServidor();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if(servico != null) {
            stopService(servico);
        }

        super.onDestroy();
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
//                Log.i(TAG, "Todas as configurações de localização foram satisfeitas");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                Log.i(TAG, "Configurações de localização não foram satisfeitas. Mostrar janela de diálogo para que o usuário atualize as configurações");

                try {
                    Toast.makeText(this, "Ative o compartilhamento de dados de localização!", Toast.LENGTH_SHORT).show();
                    status.startResolutionForResult(MainActivity.this, CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO);
                } catch (IntentSender.SendIntentException e) {
//                    Log.i(TAG, "PendingIntent incapaz de executar requisicao.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                Log.i(TAG, "Configurações de localização inadequadas, e não podem ser corrigidas aqui. Janela de dialogo não criada.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        Log.i(TAG, "Uuário concordou em fazer as mudanças de configurações de localizações necessárias.");
                        break;
                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "Usuário escolheu não realizar as mudanças de localização necessárias.");
                        break;
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //capturar modo de transporte informador pelo usuarioe

        //modoDeTransporteSelecionado = ModosDeTransporte.valueOf(position);
        //atualizarModoDeTransporte();

    }

    private void atualizarModoDeTransporte() {
        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtra(CITYTRACKSRT_MESSAGE, modoDeTransporteSelecionado.toString());
        broadcaster.sendBroadcast(intent);
    }

    private void atualizarPropositoDaViagem() {
        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtra("propositoDeViagem", propositoDeViagemSelecionado.toString());
        broadcaster.sendBroadcast(intent);
    }

    public void atualizarEnderecoDoServidor(View view){
        enderecoServidor = enderecoServidorEditText.getText().toString();
        enderecoServidorDAO.deleteAll(String.class);
        enderecoServidorDAO.create(enderecoServidor);
        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtra("enderecoServidor", enderecoServidor);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Armazena os dados da atividade no Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO, solicitandoAtualizacoesDeLocalizacao);
        savedInstanceState.putParcelable(CHAVE_DE_LOCALIZACAO, localizacaoAtual);
        savedInstanceState.putString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA, horaDaUltimaAtualizacao);
        instanceState = savedInstanceState;
        super.onSaveInstanceState(savedInstanceState);
    }

    private void configurarLabels() {
        latitudeLabel = getResources().getString(R.string.latitude_label);
        longitudeLabel = getResources().getString(R.string.longitude_label);
        horaDaUltimaAtualizacaoLabel = getResources().getString(R.string.hora_da_ultima_atualizacao_label);
        velocidadeMaximaLabel = getResources().getString(R.string.velocidade_maxima_label);
        velocidadeMediaLabel = getResources().getString(R.string.velocidade_media_label);
        numeroDeParadasLabel = getResources().getString(R.string.numero_de_paradas_label);
        tempoDeParadaLabel = getResources().getString(R.string.tempo_de_parada_label);
        aceleracaoMaximaLabel = getResources().getString(R.string.aceleracao_maxima_label);
        mudancasDeDirecaoLabel = getResources().getString(R.string.mudancas_de_direcao_label);
        numeroDeInterpolacoesLabel = getResources().getString(R.string.numero_de_interpolacoes);
        tempoDeParadaMedioLabel = getResources().getString(R.string.tempo_de_parada_medio);
        totalDePontosLabel = getResources().getString(R.string.total_de_pontos);
        totalDePontosCapturadosLabel = getResources().getString(R.string.total_de_pontos_capturados);
    }

    private void localizarOsUIWidgets() {
        
        iniciarAtualizacoesButton = (Button) findViewById(R.id.iniciar_atualizacoes_button);
        pararAtualizacoesButton = (Button) findViewById(R.id.parar_atualizacoes_button);
        enviarDadosColetadosButton = (Button) findViewById(R.id.enviar_dados_coletados);
        carroButton = (Button) findViewById(R.id.modo_de_transporte_carro);
        caminhandoButton = (Button) findViewById(R.id.modo_de_transporte_caminhando);
        bicicletaButton = (Button) findViewById(R.id.modo_de_transporte_bicicleta);
        onibusButton = (Button) findViewById(R.id.modo_de_transporte_onibus);
        motoButton = (Button) findViewById(R.id.modo_de_transporte_moto);
        casaButton = (Button) findViewById(R.id.proposito_de_viagem_casa);
        trabalhoButton = (Button) findViewById(R.id.proposito_de_viagem_trabalho);
        educacaoButton = (Button) findViewById(R.id.proposito_de_viagem_educacao);
        comprasButton = (Button) findViewById(R.id.proposito_de_viagem_compras);
        lazerButton = (Button) findViewById(R.id.proposito_de_viagem_lazer);
        outroButton = (Button) findViewById(R.id.proposito_de_viagem_outro);
        latitudeTextView = (TextView) findViewById(R.id.latitude_text);
        longitudeTextView = (TextView) findViewById(R.id.longitude_text);
        velocidadeMaximaTextView = (TextView) findViewById(R.id.velocidade_maxima);
        velocidadeMediaTextView = (TextView) findViewById(R.id.velocidade_media);
        numeroDeParadasTextView = (TextView) findViewById(R.id.numero_de_paradas);
        tempoDeParadaTextView = (TextView) findViewById(R.id.tempo_de_parada);
        horaDaUltimaAtualizacaoTextView = (TextView) findViewById(R.id.hora_da_ultima_atualizacao_text);
        aceleracaoMaximaTextView = (TextView) findViewById(R.id.aceleracao_maxima);
        mudancasDeDirecaoTextView = (TextView) findViewById(R.id.mudancas_de_direcao);
        tempoDeParadaMedioTextView = (TextView) findViewById(R.id.tempo_de_parada_medio);
        numeroDeInterpolacoesTextView = (TextView) findViewById(R.id.numero_de_interpolacoes);
        totalDePontosTextView = (TextView) findViewById(R.id.total_de_pontos);
        totalDePontosCapturadosTextView = (TextView) findViewById(R.id.total_de_pontos_capturados);
        idDispositivoTextView = (TextView) findViewById(R.id.id_dispositivo);
        idDispositivoTextView.setText("ID do Dispositivo: " + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        totalChunksColetadosTextView = (TextView) findViewById(R.id.total_chunks_coletados);
        totalLocalizacoesColetadasTextView = (TextView) findViewById(R.id.total_localizacoes_coletadas);
        selecionarModoDeTransporteTextView = (TextView) findViewById(R.id.selecionar_modo_de_transporte);
        selecionarPropositoDeViagemTextView = (TextView) findViewById(R.id.selecionar_proposito_da_viagem);
        enderecoServidorEditText = (EditText) findViewById(R.id.endereco_servidor);
        enderecoServidorEditText.setText(enderecoServidor);




//        mMapFragment = (MapFragment) getFragmentManager()
//                .findFragmentById(R.id.map);
//        mMapFragment.getMapAsync(this);

        //seleção de modo de transporte
//        modosDeTransporteSpinner = (Spinner) findViewById(R.id.modo_de_transporte_spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.modos_de_transporte_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        modosDeTransporteSpinner.setAdapter(adapter);
//        modosDeTransporteSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Atualiza os campos baseado-se nos dados armazenados o bundle
     *
     * @param savedInstanceState O estado da atividade salva no Bunde
     */
    private void atualizarValoresComBundle(Bundle savedInstanceState) {
        //Log.i(TAG, "Atualizando valores com bundle");
        if (savedInstanceState != null) {
            // Atualiza o valor de solicitandoAtualizacoesDeLocalizacao com o Bundle, e garante que
            // os botões Iniciar Atualizações e Parar Atualizações são habilitados ou desabilitados corretamente.
            if (savedInstanceState.keySet().contains(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO)) {
                solicitandoAtualizacoesDeLocalizacao = savedInstanceState.getBoolean(
                        CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO);
            }

            // Autaliza o valor de localizacaoAtual com o Bundle e atualiza a UI para mostrar latitude e longitude atualizadas
            if (savedInstanceState.keySet().contains(CHAVE_DE_LOCALIZACAO)) {
                // Uma vez que CHAVE_DE_LOCALIZACAO foi encontrada no Bundle, nos podemos ter certeza que localizacaoAtual não é nula
                localizacaoAtual = savedInstanceState.getParcelable(CHAVE_DE_LOCALIZACAO);
            }

            // Atualiza o valor de horaDaUltimaAtualizacao com o Bundle e atualiza a UI
            if (savedInstanceState.keySet().contains(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA)) {
                horaDaUltimaAtualizacao = savedInstanceState.getString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA);
            }

            if(savedInstanceState.keySet().contains(CHAVE_MODO_DE_TRANSPORTE_SELECIONADO)){
                modoDeTransporteSelecionado = ModosDeTransporte.valueOf(savedInstanceState.getString(CHAVE_MODO_DE_TRANSPORTE_SELECIONADO));
            }

            if(savedInstanceState.keySet().contains(CHAVE_PROPOSITO_DE_VIAGEM_SELECIONADO)){
                propositoDeViagemSelecionado = PropositosDeViagem.valueOf(savedInstanceState.getString(CHAVE_PROPOSITO_DE_VIAGEM_SELECIONADO));
            }

            if(savedInstanceState.keySet().contains("chunkCounter")){
                chunkCounter = savedInstanceState.getInt("chunkCounter");

            }

            if(savedInstanceState.keySet().contains("locationCounter")){
                locationCounter = savedInstanceState.getInt("locationCounter");
            }

            instanceState = savedInstanceState;
            atualizarUI();
        }
    }

    /**
     * Configura o botão Iniciar Atualizações e solicita o início das atualizações de localização.
     * Não faz nada se as atualizações já tiverem sido solicitadas.
     */
    public void iniciarAtualizacoesButtonHandler(View view) {

        if (!solicitandoAtualizacoesDeLocalizacao) {
            if(modoDeTransporteSelecionado != null && propositoDeViagemSelecionado != null && enderecoServidor != null) {
                solicitandoAtualizacoesDeLocalizacao = true;
                configurarEstadoHabilitadoDosButtons();
                servico = new Intent(this, CityTracksRTService.class)
                        .putExtra("modoDeTransporte", modoDeTransporteSelecionado.toString())
                        .putExtra("propositoDeViagem", propositoDeViagemSelecionado.toString())
                        .putExtra("enderecoServidor", enderecoServidor);
                startService(servico);
//                Log.i(TAG,"Chamou servico!");
            }else{
                Toast.makeText(this,"Por favor, selecione o modo de transporte utilizado, o propósito da viagem e informe a URL do servidor de coleta!",Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * Configura o botão Parar Atualizacoes, e solicita a remoção das atualizações de localização.
     * Não faz nada se as atualizações já nao tiverem sido requisitadas previamente.
     */
    public void paraAtualizacoesButtonHandler(View view) {
        if (solicitandoAtualizacoesDeLocalizacao) {
            solicitandoAtualizacoesDeLocalizacao = false;
            configurarEstadoHabilitadoDosButtons();
            stopService(servico);
            persistirContadoresEEnderecoServidor();
        }
    }

    public void enviarDadosColetados(View view){

        WebServiceFacade webServiceFacade = new WebServiceFacade(this);

        if(webServiceFacade.isConectadoViaWiFi() || webServiceFacade.isConectadoViaRedeMovel()) {

            try {
                JSONDAO<Chunk> chunkJSONDAO = new JSONDAO<>(this, "chunks");
                List<Chunk> chunks = chunkJSONDAO.retrieveAll(Chunk.class);
                int totalChunksJSON = chunks.size();
                if (totalChunksJSON > 0) {
                    chunks = webServiceFacade.enviarChunks(chunks, enderecoServidor);
                    chunkJSONDAO.deleteAll(Chunk.class);
                    chunkJSONDAO.create(chunks);
                    int totalChunksEnviados = totalChunksJSON - chunks.size();
                    Toast.makeText(this, totalChunksEnviados + " chunks enviados", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Todos os chunks coletados já foram enviados!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ioe) {
                Toast.makeText(this, "Erro ao enviar chunks para o servidor!", Toast.LENGTH_SHORT).show();
            }

            try {
                JSONDAO<Localizacao> localizacoesJSONDAO = new JSONDAO<>(this, "localizacoes");
                List<Localizacao> localizacoes = localizacoesJSONDAO.retrieveAll(Localizacao.class);
                int totalLocalizacoesJSON = localizacoes.size();
                if (totalLocalizacoesJSON > 0) {
                    localizacoes = webServiceFacade.enviarLocalizacoes(localizacoes, enderecoServidor);
                    localizacoesJSONDAO.deleteAll(Localizacao.class);
                    localizacoesJSONDAO.create(localizacoes);
                    int totalLocalizacoesEnviadas = totalLocalizacoesJSON - localizacoes.size();
                    Toast.makeText(this, totalLocalizacoesEnviadas + " localizações enviadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Todas as localizações coletadas já foram enviadas!", Toast.LENGTH_LONG).show();
                }
            } catch (IOException ioException) {
                Toast.makeText(this, "Erro ao enviar localizacoes para o servidor!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Conexão WiFi ou Rede Móvel indisponível!", Toast.LENGTH_SHORT).show();
        }
    }

    public void limparModosDeTransporte(){
        caminhandoButton.setEnabled(true);
        bicicletaButton.setEnabled(true);
        onibusButton.setEnabled(true);
        carroButton.setEnabled(true);
        motoButton.setEnabled(true);
    }

    public void limparPropositoDeViagem(){
        casaButton.setEnabled(true);
        trabalhoButton.setEnabled(true);
        educacaoButton.setEnabled(true);
        comprasButton.setEnabled(true);
        lazerButton.setEnabled(true);
        outroButton.setEnabled(true);
    }

    public void mudarPropositoDaViagemParaCasa(View view) {
        limparPropositoDeViagem();
        casaButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.CASA;
        atualizarPropositoDaViagem();
    }

    public void mudarPropositoDaViagemParaTrabalho(View view) {
        limparPropositoDeViagem();
        trabalhoButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.TRABALHO;
        atualizarPropositoDaViagem();
    }

    public void mudarPropositoDaViagemParaEducacao(View view) {
        limparPropositoDeViagem();
        educacaoButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.EDUCACAO;
        atualizarPropositoDaViagem();
    }

    public void mudarPropositoDaViagemParaCompras(View view) {
        limparPropositoDeViagem();
        comprasButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.COMPRAS;
        atualizarPropositoDaViagem();
    }

    public void mudarPropositoDaViagemParaLazer(View view) {
        limparPropositoDeViagem();
        lazerButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.LAZER;
        atualizarPropositoDaViagem();
    }

    public void mudarPropositoDaViagemParaOutro(View view) {
        limparPropositoDeViagem();
        outroButton.setEnabled(false);
        propositoDeViagemSelecionado = PropositosDeViagem.OUTRO;
        atualizarPropositoDaViagem();
    }

    public void mudarModoDeTransporteParaCaminhando(View view) {
        limparModosDeTransporte();
        caminhandoButton.setEnabled(false);
        modoDeTransporteSelecionado = ModosDeTransporte.CAMINHANDO;
        atualizarModoDeTransporte();
    }

    public void mudarModoDeTransporteParaBicicleta(View view) {
        limparModosDeTransporte();
        bicicletaButton.setEnabled(false);
        modoDeTransporteSelecionado = ModosDeTransporte.BICICLETA;
        atualizarModoDeTransporte();
    }


    public void mudarModoDeTransporteParaCarro(View view) {
        limparModosDeTransporte();
        carroButton.setEnabled(false);
        modoDeTransporteSelecionado = ModosDeTransporte.CARRO;
        atualizarModoDeTransporte();
    }

    public void mudarModoDeTransporteParaOnibus(View view) {
        limparModosDeTransporte();
        onibusButton.setEnabled(false);
        modoDeTransporteSelecionado = ModosDeTransporte.ONIBUS;
        atualizarModoDeTransporte();
    }

    public void mudarModoDeTransporteParaMoto(View view) {
        limparModosDeTransporte();
        motoButton.setEnabled(false);
        modoDeTransporteSelecionado = ModosDeTransporte.MOTO;
        atualizarModoDeTransporte();
    }

    private void atualizarModoDeTransporteSelecionado() {
        //modosDeTransporteSpinner.setSelection(modoDeTransporteSelecionado.getValor(),true);
    }

    /**
     * Garante que somente um botão é habilitado de cada vez.
     * O botão Iniciar Atualizações é habilitado se o usuário não estiver requisitando atualizações de localização.
     * O botão Parar Atualizações é habilitado se o usuário não estiver requisitando atualizações de localização.
     */
    private void configurarEstadoHabilitadoDosButtons() {
        if (solicitandoAtualizacoesDeLocalizacao) {
            iniciarAtualizacoesButton.setEnabled(false);
            pararAtualizacoesButton.setEnabled(true);
            enviarDadosColetadosButton.setEnabled(false);
        } else {
            iniciarAtualizacoesButton.setEnabled(true);
            pararAtualizacoesButton.setEnabled(false);
            enviarDadosColetadosButton.setEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
//
//        Log.i(TAG,"Map Ready!");
//        googleMap = map;
//
//        atualizarUI();
    }

    /**
     * Atualiza a latitude, a longitude, e a hora da ultima localizacao na UI.
     */
    private void atualizarUI() {

        configurarEstadoHabilitadoDosButtons();

        if (localizacaoAtual != null) {
//            atualizarMapa();
        }

        totalChunksColetadosTextView.setText("Chunks Classificados: " + chunkCounter);
        totalLocalizacoesColetadasTextView.setText("Localizações Coletadas: " + locationCounter);


    }

    private void carregarContadores() {
        try{
            List<Integer> list = chunkCounterDAO.retrieveAll(Integer.class);
            if(list.size() == 1){
                chunkCounter = list.get(0);
            }else{
                chunkCounter = 0;
            }
            list = locationCounterDAO.retrieveAll(Integer.class);
            if(list.size() == 1){
                locationCounter = list.get(0);
            }else{
                locationCounter = 0;
            }

        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    private void persistirContadoresEEnderecoServidor() {
        chunkCounterDAO.deleteAll(Integer.class);
        chunkCounterDAO.create(chunkCounter);
        locationCounterDAO.deleteAll(Integer.class);
        locationCounterDAO.create(locationCounter);
        enderecoServidorDAO.deleteAll(String.class);
        enderecoServidorDAO.create(enderecoServidor);
    }

//    private void atualizarMapa() {
//        LatLng latLng = new LatLng(localizacaoAtual.getLatitude(), localizacaoAtual.getLongitude());
//
//        if(marker == null) {
//            if(googleMap != null) {
//                marker = googleMap.addMarker(new MarkerOptions().position(latLng));
//            }else{
//                Log.i(TAG,"Map null for Marker!");
//            }
//        }else{
//            marker.setPosition(latLng);
//        }
//
//        if(googleMap != null) {
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//        }else{
//            Log.i(TAG,"Map null for Camera!");
//        }
//    }

    private void verificarConfiguracoesDeLocalizacao() {
        requisicaoDeLocalizacao = LocationRequest.create();
        requisicaoDeLocalizacao.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requisicaoDeLocalizacao.setInterval(INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS);
        requisicaoDeLocalizacao.setFastestInterval(INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(requisicaoDeLocalizacao);
        requisicaoDeConfiguracaoDeLocalizacao = builder.build();
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        clienteDaGoogleApi,
                        requisicaoDeConfiguracaoDeLocalizacao
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
