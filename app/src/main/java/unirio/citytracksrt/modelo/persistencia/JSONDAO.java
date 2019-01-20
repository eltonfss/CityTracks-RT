package unirio.citytracksrt.modelo.persistencia;

import android.content.*;
import android.os.*;

import com.google.gson.*;

import java.io.*;
import java.util.*;

/**
 * Created by Elton Soares on 3/20/2017.
 */

public class JSONDAO<ObjectClass> {

    private Context context;
    private File file;
    private Gson gson;

    public JSONDAO(Context context, String jsonFileName){
        this.context = context;
        this.file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), jsonFileName + ".js");
        this.file.setReadable(true);
        this.file.setWritable(true);
        this.gson = new Gson();
    }

    public void create(ObjectClass object){

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String line = gson.toJson(object);
            bufferedWriter.append(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(List<ObjectClass> list){

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for(ObjectClass object : list){
                String line = gson.toJson(object);
                bufferedWriter.append(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<ObjectClass> retrieveAll(Class<ObjectClass> objectClass) throws IOException{

        List<ObjectClass> list = new ArrayList<>();

        if(file.exists()) {

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();

            while (line != null) {
                list.add(gson.fromJson(line, objectClass));
                line = reader.readLine();
            }

            reader.close();

        }

        return list;
    }

    public void deleteAll(Class<ObjectClass> objectClass){
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
