package unirio.citytracksrt.controle;

import android.app.*;
import android.app.ActivityManager.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v4.content.*;
import android.widget.*;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;

import java.io.*;
import java.text.*;
import java.util.*;

import unirio.citytracksrt.modelo.entidade.*;
import unirio.citytracksrt.modelo.persistencia.*;
import unirio.citytracksrt.utils.inferencia.*;

import static unirio.citytracksrt.utils.Utils.*;

public class CityTracksRTService extends Service
{

    //Tag para identificação das mensagens no LOG
    protected static final String TAG = "city-tracks-rt";

    //Atributos para coleta de dados de localização

    /**
     * O intervalo desejado para as atualizações de localização. Inexato. Atualizações podem ser mais ou menos frequentes.
     */
    public static final long INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS = 1000;

    /**
     * A taxa mais alta para atualizações de localização ativas. Exata. Atualizações nunca serão mais frequentes que este valor
     */
    public static final long INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS = INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS;

    /**
     * Chaves para armazenamento do estado da atividade no Bundle
     * */
    protected final static String CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO = "requesting-location-updates-key";
    protected final static String CHAVE_DE_LOCALIZACAO = "location-key";
    protected final static String CHAVE_STRING_ULTIMA_HORA_ATUALIZADA = "last-updated-time-string-key";

    /**
     * Prove um ponto de entrada para os serviços Google Play
     */
    protected GoogleApiClient clienteDaGoogleApi;

    /**
     * Armazena parâmetros de requisições para o FusedLocationProviderApi
     */
    protected LocationRequest requisicaoDeLocalizacao;

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
     * Representa uma localização geográfica
     */
    protected Location locationAtual;

    /**
     * Timestamp quando a localização foi atualizada representada como um Calendar
     */
    private Calendar timestampDaUltimaAtualizacao;

    //Atributos para comunicação e persistência

    /**
     * Objetos para envio e recebimendo de mensagens da MainActivity
     */
    private LocalBroadcastManager broadcaster;
    private BroadcastReceiver receiver;
    public static final String CITYTRACKSRT_RESULT = "unirio.citytracksrt.CityTracksRTService.REQUEST_PROCESSED";

    /**
     * Objetos para persistencia em arquivo JSON
     */
    private JSONDAO<Chunk> chunkJSONDAO;
    private JSONDAO<Localizacao> localizacoesJSONDAO;

    /**
     * Objeto para comunicação com WebService
     */
    private WebServiceFacade webServiceFacade;

    //Atributos para pré-processamento e classificação do modo de transporte

    /**
     * Armazena os pontos capturados e faz as sumarizacoes
     */
    protected ChunkBuilder chunkBuilder;

    /**
     * armazena localizacoes brutas
     */
    private List<Localizacao> localizacoes = new ArrayList<>();

    /**
     * ultimo Localizacao registrado
     */
    private Localizacao localizacaoAtual;

    /**
     * armazena a opção de modo de transporte selecionada pelo usuário
     */
    private ModosDeTransporte modoDeTransporteSelecionado;

    /**
     * armazena a opção de proposito de viagem selecionada pelo usuário
     */
    private PropositosDeViagem propositoDeViagemSelecionado;

    /**
     * Classe de inferência de modo de transporte
     * */
    private Classificador classificador;

    /**
     * Id do dispositivo utilizado
     */
    private String idDispositivo;

    /**
     * Lista de chunks coletados
     */
    private List<Chunk> chunks;

    private int chunkCounter;
    private int locationCounter;
    private JSONDAO<Integer> chunkCounterDAO;
    private JSONDAO<Integer> locationCounterDAO;
    private String urlEnderecoServidor;

    //Classe aninhada que faz o trabalho sujo

    /**
     * Classe que recebe e processa os dados de localização (Faz o trabalho sujo)
     * O Serviço instancia um objeto desta classe e faz a interface com a MainActivity
     * Mantive como classe aninhada pois ela faz modificações em vários atributos do Service
     */
    private class LocationListener implements ResultCallback<LocationSettingsResult>, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    {

        public LocationListener()
        {
            idDispositivo = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            inicializarAtributos();
            enviarDadosViaWebService();
            construirGoogleApiClient();
            criarRequisicaoDeLocalizacao();
        }

        /**
         * Constroi um GoogleApiClient. Utiliza o método {@code #addApi} para efetuar requisições a LocationServices API
         */
        protected synchronized void construirGoogleApiClient() {
            clienteDaGoogleApi = new GoogleApiClient.Builder(getBaseContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }

        private void inicializarAtributos() {

            solicitandoAtualizacoesDeLocalizacao = false;
            horaDaUltimaAtualizacao = "";
            chunks = new ArrayList<>();
            iniciarNovoChunk();
            classificador = new Classificador();
            localizacoesJSONDAO = new JSONDAO<>(getBaseContext(), "localizacoes");
            chunkJSONDAO = new JSONDAO<>(getBaseContext(),"chunks");

            chunkCounterDAO = new JSONDAO<>(getBaseContext(), "chunkCounter");
            locationCounterDAO = new JSONDAO<>(getBaseContext(), "locationCounter");

            inicializarContadores();

            treinarClassificadores();
        }

        private void treinarClassificadores() {

            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        classificador.treinarAlgoritmos(getBaseContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {

                thread.start();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Erro ao treinar classificadores! Contate os desenvolvedores.", Toast.LENGTH_SHORT).show();
            }
        }

        private void inicializarContadores() {
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

        /**
         * Solicitar atualizações de localização da FusedLocationApi
         */
        protected void iniciarAtualizacoesDeLocalizacao() {
            // O argumento final para {@code requestLocationUpdates()} é um LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            LocationServices.FusedLocationApi.requestLocationUpdates(clienteDaGoogleApi, requisicaoDeLocalizacao, (com.google.android.gms.location.LocationListener) this);
            solicitandoAtualizacoesDeLocalizacao = true;
        }

        /**
         * Configura a requisição de localização. Android tem duas configurações de requisição de localização:
         * {@code ACCESS_COARSE_LOCATION} e {@code ACCESS_FINE_LOCATION}. Estas configurações controlam a precisão da localização atual
         * Neste projeto é utilizado ACESS_FINE_LOCATION, conforme definido no AndroidManifest.xml
         * <p>
         * Quando a configuração ACCESS_FINE_LOCATION é especificada, em conjunto com um intervalo de atualização curto (5 segundos),
         * a Fused Location Provider API retorna atualizações de localização com precisão de alguns pés.
         * <p>
         * Estas configurações são apropriadas para mapeamento de aplicações que mostram localizações em tempo-real
         */
        protected void criarRequisicaoDeLocalizacao() {
            requisicaoDeLocalizacao = new LocationRequest();

            // Configura o intervalo desejado de atualizações de localização ativas. Este intervalo é inexato.
            // É possível que não seja recebida nenhuma atualização se nenhum recurso de localização estiver disponível,
            // ou então pode ser que as atualizações sejam recebidas mais lentamente do que o solicitado.
            // É possível também que sejam recebidas atualizações mais rapidamente do que o solicitado
            // se outras aplicações estiverem solicitando localizações em um intervalo mais curto.
            requisicaoDeLocalizacao.setInterval(INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS);

            // Configura o intervalo mais curto para que atualizações de localização ativas sejam realizadas.
            // Este intervalo é exato, e a aplicação nunca vai receber atualizações em intervalos menores que este.
            requisicaoDeLocalizacao.setFastestInterval(INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS);

            requisicaoDeLocalizacao.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public void onLocationChanged(Location novaLocalizacao)
        {


            Calendar timestampDaNovaLocalizacao = Calendar.getInstance();
            timestampDaNovaLocalizacao.clear(Calendar.MILLISECOND);

            MemoryInfo memoryInfo = getMemoryInfo();

            if (memoryInfo.lowMemory) {
                localizacoesJSONDAO.create(localizacoes);
                localizacoes = new ArrayList<>();
                chunkJSONDAO.create(chunks);
                chunks = new ArrayList<>();
                Toast.makeText(getBaseContext(), "Memoria RAM cheia! Dados persistidos em JSON.", Toast.LENGTH_SHORT).show();
            }

            //processamento do ponto
            capturarLocalizacao(novaLocalizacao, timestampDaNovaLocalizacao);

            locationAtual = novaLocalizacao;
            horaDaUltimaAtualizacao = DateFormat.getTimeInstance().format(new Date(timestampDaNovaLocalizacao.getTimeInMillis()));
            timestampDaUltimaAtualizacao = timestampDaNovaLocalizacao;

            //verificar se chunkBuilder atingiu o limite de pontos
            if (chunkBuilder.isCheio()) {
                if (chunkBuilder.isValido()) {
                    chunkBuilder.sumarizar();

                    //salvar chunkBuilder em JSON ou CSV
                    Chunk chunk = chunkBuilder.getChunk();

                    try {
                        chunk  = classificador.classificar(chunk);
                    } catch (Exception e) {
                    }

                    chunks.add(chunk);
                    chunkCounter++;

                    enviarDadosViaWebService();

                }

                iniciarNovoChunk();
            }

            sendResult();

        }

        private void iniciarNovoChunk() {
            chunkBuilder = new ChunkBuilder(
                    Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)
                    , Build.MODEL
                    , Build.VERSION.RELEASE + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName()

            );
            localizacaoAtual = null;
            localizacaoAtual = null;

            if (modoDeTransporteSelecionado != null) {
                chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
            }

        }

        private void capturarLocalizacao(Location novaLocalizacao, Calendar timestampDaNovaLocalizacao) {

            long diferencaEmSegundos = 0;

            if (timestampDaUltimaAtualizacao != null && timestampDaNovaLocalizacao != null) {
                diferencaEmSegundos = calcularDiferencaEmSegundos(timestampDaNovaLocalizacao, timestampDaUltimaAtualizacao);
            }

            LocalizacaoFactory localizacaoFactory = new LocalizacaoFactory();

            if(diferencaEmSegundos > 0 && novaLocalizacao.getAccuracy() > 0.0){

                //adicionar localizacoes ao list de localizacoes sem tratamento (raw)
                String modeloDispositivo = Build.MODEL;
                String versaoAndroid = Build.VERSION.RELEASE + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
                localizacaoAtual = localizacaoFactory.constroiLocalizacao(idDispositivo, propositoDeViagemSelecionado, modoDeTransporteSelecionado,novaLocalizacao, timestampDaNovaLocalizacao, modeloDispositivo, versaoAndroid, locationAtual, chunkBuilder.getTotalDePontos(), diferencaEmSegundos, localizacaoAtual);
                localizacoes.add(localizacaoAtual);
                locationCounter++;
                // ignorar localizacoes com erro de precisao maior que 200 metros ou igual a 0.0 ou com Timestamps iguais
                if (novaLocalizacao.getAccuracy() < ChunkBuilder.LIMITE_DE_PRECISAO) {

                    // se o chunkBuilder nao foi isDescartado por limite de tempo de parada
                    if (!chunkBuilder.isDescartado()) {
                        //adicionar localizacao ao chunks
                        chunkBuilder.adicionarPonto(localizacaoAtual);
                    } else {

                        iniciarNovoChunk();
                    }

                }
            }



        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            // Se a localização inicial não foi solicitada anteriormente, nós utilizamos FusedLocationApi.getLastLocation() para captura-la.
            // Se foi solicitada previamente, nos armazenamos seu valor no Bundle e procuramos por ele no OnCreate().
            // Não solicitaremos esta localização novamente a não ser que o usuário solicite atualizações de localizações pressionando o botão Iniciar Atualizações
            //
            // Porque nos armazenamos o valor da localização atual no Bundle, se o usuário executar a atividade, mudar de localização,
            // e depois modificar a orientação do dispositivo, a localização original é exibida uma vez que a actividade e re-criada
            if (localizacaoAtual == null) {
                Location localizacao = LocationServices.FusedLocationApi.getLastLocation(clienteDaGoogleApi);
                if (localizacao != null) {
                    timestampDaUltimaAtualizacao = Calendar.getInstance();
                    timestampDaUltimaAtualizacao.clear(Calendar.MILLISECOND);
                    capturarLocalizacao(localizacao, timestampDaUltimaAtualizacao);
                    horaDaUltimaAtualizacao = DateFormat.getTimeInstance().format(new Date());
                    locationAtual = localizacao;
                }
            }

            iniciarAtualizacoesDeLocalizacao();
        }

        @Override
        public void onConnectionSuspended(int i) {
            // A conexão com os serviços Google Play foi perdida por algum motivo.
            // Nós chamamos connect() para tentar reestabelecer a conexão.
            clienteDaGoogleApi.connect();
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // Consultar o javadoc de ConnectionResult para ver quais códigos de erro podem ser retornados em onConnectionFailed.
        }

        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

        }
    }

    //Métodos sobrecarregados

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        LocationListener locationListener = new LocationListener();
        clienteDaGoogleApi.connect();
        if(intent != null) {
            String modoDeTransporte = intent.getStringExtra("modoDeTransporte");
            if(modoDeTransporte != null) {
                modoDeTransporteSelecionado = ModosDeTransporte.valueOf(modoDeTransporte);
                chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
            }
            String propositoDeViagem = intent.getStringExtra("propositoDeViagem");
            if(propositoDeViagem != null) {
                propositoDeViagemSelecionado = PropositosDeViagem.valueOf(propositoDeViagem);
            }
            String enderecoServidor = intent.getStringExtra("enderecoServidor");
            if(enderecoServidor != null){
                urlEnderecoServidor = enderecoServidor;
            }

            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter(MainActivity.CITYTRACKSRT_RESULT)
            );
        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        broadcaster = LocalBroadcastManager.getInstance(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String modoDeTransporte = intent.getStringExtra(MainActivity.CITYTRACKSRT_MESSAGE);
                if(modoDeTransporte != null) {
                    modoDeTransporteSelecionado = ModosDeTransporte.valueOf(modoDeTransporte);
                    chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
                }
                String propositoDeViagem = intent.getStringExtra("propositoDeViagem");
                if(propositoDeViagem != null) {
                    propositoDeViagemSelecionado = PropositosDeViagem.valueOf(propositoDeViagem);
                }
                String enderecoServidor = intent.getStringExtra("enderecoServidor");
                if(enderecoServidor != null) {
                    urlEnderecoServidor = enderecoServidor;
                }


            }
        };

        //instanciar objeto de comunicação com web servie
        webServiceFacade = new WebServiceFacade(getBaseContext());
    }


    @Override
    public boolean stopService(Intent name) {
        enviarDadosViaWebService();
        persistirDadosNãoEnviados();
        clienteDaGoogleApi.disconnect();
        return super.stopService(name);
    }

    @Override
    public void onDestroy()
    {
        enviarDadosViaWebService();
        persistirDadosNãoEnviados();
        clienteDaGoogleApi.disconnect();
    }



    private void persistirDadosNãoEnviados() {
        if(chunks.size() > 0){
            chunkJSONDAO.create(chunks);
        }

        if(localizacoes.size() > 0){
            localizacoesJSONDAO.create(localizacoes);
        }
    }

    //Métodos utilitários

    /**
     * Envia dados atualizados para a MainActivity a cada nova localização
     */
    public void sendResult() {

        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putBoolean(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO, solicitandoAtualizacoesDeLocalizacao);
        savedInstanceState.putParcelable(CHAVE_DE_LOCALIZACAO, locationAtual);
        savedInstanceState.putString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA, horaDaUltimaAtualizacao);
        savedInstanceState.putInt("chunkCounter", chunkCounter);
        savedInstanceState.putInt("locationCounter", locationCounter);

        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtras(savedInstanceState);

        broadcaster.sendBroadcast(intent);
    }

    /**
     * @return objeto que contem o status da memória RAM
     */
    private MemoryInfo getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    /**
     * Envia todos os chunks e localizações coletados até o momento via WebService
     */
    private void enviarDadosViaWebService() {

        if(webServiceFacade.isConectadoViaWiFi()) {

            //enviar chunks e localizacoes armazenados em memoria RAM via WS
            chunks = webServiceFacade.enviarChunks(chunks,urlEnderecoServidor);
            //Log.i(TAG, "Chunks da RAM enviados - Total remanescente: " + chunks.size());
            localizacoes = webServiceFacade.enviarLocalizacoes(localizacoes,urlEnderecoServidor);
            //Log.i(TAG, "Localizacoes da RAM enviados - Total remanescente: " + localizacoes.size());

            try {
                //Carregar chunks salvos em arquivo para memoria RAM e enviar via WS
                chunks.addAll(chunkJSONDAO.retrieveAll(Chunk.class));
                //Log.i(TAG, "Chunks JSON recuperados - Total recuperado: " + chunks.size());
                chunks = webServiceFacade.enviarChunks(chunks,urlEnderecoServidor);
                //Log.i(TAG, "Chunks JSON enviados - Total remanescente: " + chunks.size());
                chunkJSONDAO.deleteAll(Chunk.class);
                //Log.i(TAG, "Chunks JSON removidos");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            try {
                //Carregar localizacoes salvas em arquivo para memoria RAM e enviar via WS
                localizacoes.addAll(localizacoesJSONDAO.retrieveAll(Localizacao.class));
                //Log.i(TAG, "Localizacoes JSON recuperados - Total recuperado: " + localizacoes.size());
                localizacoes = webServiceFacade.enviarLocalizacoes(localizacoes,urlEnderecoServidor);
                //Log.i(TAG, "Localizacoes JSON enviados - Total remanescente: " + localizacoes.size());
                localizacoesJSONDAO.deleteAll(Localizacao.class);
                //Log.i(TAG, "Localizacoes JSON removidas");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }

    }


}