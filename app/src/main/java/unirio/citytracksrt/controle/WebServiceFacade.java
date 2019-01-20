package unirio.citytracksrt.controle;

import android.content.*;
import android.net.*;

import com.google.gson.*;
import com.loopj.android.http.*;

import org.apache.http.entity.*;

import java.util.*;

import unirio.citytracksrt.modelo.entidade.*;
import unirio.citytracksrt.modelo.persistencia.*;

public class WebServiceFacade {

    protected static final String TAG = "city-tracks-rt";

    protected static final String URL_DEFAULT = "http://citytracks.intelliurb.org/citytracks-rt/";

    protected Gson gson;

    protected Context context;

    protected JSONDAO<Chunk> chunkJSONDAO;
    protected JSONDAO<Localizacao> localizacaoJSONDAO;

    private AsyncHttpClient httpClient;

    private int status;

    WebServiceFacade(Context baseContext){
        gson = new Gson();
        context = baseContext;
        httpClient = new AsyncHttpClient();
        chunkJSONDAO = new JSONDAO<>(context,"chunksBackup");
        localizacaoJSONDAO = new JSONDAO<>(context,"localizacoesBackup");
    }

    /**
     * Enviar todos os chunks via WS
     * @return lista de chunks que não puderam ser enviados
     */
    public List<Chunk> enviarChunks(List<Chunk> chunks, String urlServidor){

        if((isConectadoViaWiFi() || isConectadoViaRedeMovel()) && urlServidor != null) {

            List<Chunk> chunksEnviados = new ArrayList<>();

            for (Chunk chunk : chunks) {

                try {

                    post(new StringEntity(gson.toJson(chunk).toString()), urlServidor, "chunks");
                    chunksEnviados.add(chunk);

                } catch (Exception e) {

                    break;

                }

            }

            chunkJSONDAO.create(chunksEnviados);
            chunks.removeAll(chunksEnviados);

        }

        return chunks;

    }

    /**
     * Enviar todas as localizacoes via WS
     * @return lista de localizacoes que não puderam ser enviadas
     */
    public List<Localizacao> enviarLocalizacoes(List<Localizacao> localizacoes, String urlServidor){

        if((isConectadoViaWiFi() || isConectadoViaRedeMovel()) && urlServidor != null) {

            List<Localizacao> localizacoesEnviadas = new ArrayList<>();

            for (Localizacao localizacao : localizacoes) {

                try {

                    post(new StringEntity(gson.toJson(localizacao).toString()), urlServidor, "locations");
                    localizacoesEnviadas.add(localizacao);

                } catch (Exception e) {
                    break;
                }

            }

            localizacaoJSONDAO.create(localizacoesEnviadas);
            localizacoes.removeAll(localizacoesEnviadas);

        }



        return localizacoes;

    }

    private void post(StringEntity json, String url, String resource) {

        String uri = url + resource;
        //Log.i(TAG, url);
        httpClient.post(context, uri, json, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
            }
        });
    }


    /**
     * @return true se há conexão WiFi, false se não há
     */
    public boolean isConectadoViaWiFi() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * @return true se há conexão de rede móvel, false se não há
     */
    public boolean isConectadoViaRedeMovel() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (networkInfo != null && networkInfo.isConnected());
    }
}
