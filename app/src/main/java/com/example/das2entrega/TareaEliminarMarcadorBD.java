package com.example.das2entrega;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TareaEliminarMarcadorBD extends Worker {
    public TareaEliminarMarcadorBD(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/iisasi021/WEB/eliminarMarcadorBD.php";
        HttpURLConnection urlConnection = null;
        Data.Builder resultados = new Data.Builder();
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type","application/json");

            // recogemos el usuario y el marcador y los preparamos para mandar al php
            String usuario = getInputData().getString("usuario");
            String marcador = getInputData().getString("marcador");

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("usuario", usuario);
            parametrosJSON.put("marcador", marcador);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            resultados = new Data.Builder().putInt("statusCode", statusCode);
            if (statusCode == 200) {
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();
                resultados.putString("datos", result);
                urlConnection.disconnect();
                return Result.success(resultados.build());
            } else {
                urlConnection.disconnect();
                return Result.failure(resultados.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(resultados.build());
        }
    }
}
