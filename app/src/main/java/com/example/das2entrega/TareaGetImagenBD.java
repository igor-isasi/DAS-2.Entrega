package com.example.das2entrega;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class TareaGetImagenBD extends Worker {
    Context myContext = null;
    public TareaGetImagenBD(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        myContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/iisasi021/WEB/getImagenBD.php";
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

            // recogemos el usuario y lo preparamos para mandar al php
            String usuario = getInputData().getString("usuario");

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("usuario", usuario);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
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

                byte[] imagenBytes = Base64.getDecoder().decode(result);
                Bitmap imagenBitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);

                // guardar imagen en memoria interna
                try (FileOutputStream s = myContext.openFileOutput(usuario + ".jpg", MODE_PRIVATE)) {
                    imagenBitmap.compress(Bitmap.CompressFormat.PNG, 100, s); // bmp is your Bitmap instance
                } catch (IOException e) {
                    e.printStackTrace();
                }

                resultados.putString("nombreFichero", usuario + ".jpg");

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
