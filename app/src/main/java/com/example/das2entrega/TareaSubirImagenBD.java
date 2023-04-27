package com.example.das2entrega;

import android.content.Context;
import android.content.res.Resources;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class TareaSubirImagenBD extends Worker {
    Context myContext = null;
    public TareaSubirImagenBD(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        myContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/iisasi021/WEB/subirImagenBD.php";
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

            // recogemos el usuario e imagen (desde memoria) y los preparamos para mandar al php
            String usuario = getInputData().getString("usuario");
            String imagenBase64 = null;

            FileInputStream ficheroImagen = null;
            try {
                ficheroImagen = myContext.openFileInput(usuario + ".jpg");
                if(ficheroImagen != null && ficheroImagen.available() > 0) {
                    Bitmap imagenBitmap = BitmapFactory.decodeStream(ficheroImagen);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imagenBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imagenBytes = stream.toByteArray();
                    imagenBase64 = Base64.getEncoder().encodeToString(imagenBytes);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("usuario", usuario);
            parametrosJSON.put("imagen", imagenBase64);
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
