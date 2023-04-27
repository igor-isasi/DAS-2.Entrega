package com.example.das2entrega;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PerfilActivity extends AppCompatActivity {

    String usuario = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
            TextView textUsuario = (TextView) findViewById(R.id.textUser);
            textUsuario.setText(usuario);
        }

        //actualizar la imagen de perfil cogiendola de BD
        actualizarImagen();

        // Pedir permisos para la geolocalizacion
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // instanciar el launcher para recoger la imagen
        ActivityResultLauncher<Intent> takePictureLauncher =
                registerForActivityResult(new
                        ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap imagenBitmap = (Bitmap) bundle.get("data");

                        // Se guarda la imagen en un fichero porque no se puede enviar serializada (demasiado grande)
                        try (FileOutputStream out = openFileOutput(usuario + ".jpg", MODE_PRIVATE)) {
                            imagenBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Se programa la tarea de subida de imagen
                        Data datos = new Data.Builder().putString("usuario", usuario).build();
                        if (usuario != null) {
                            OneTimeWorkRequest otwrImagen = new OneTimeWorkRequest.Builder(TareaSubirImagenBD.class).setInputData(datos).build();
                            WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrImagen.getId())
                                    .observe(this, new Observer<WorkInfo>() {
                                        @Override
                                        public void onChanged(WorkInfo workInfo) {
                                            if (workInfo != null && workInfo.getState().isFinished()) {
                                                String datos = workInfo.getOutputData().getString("datos");
                                                System.out.println(datos);
                                                //actualizar la imagen de perfil cogiendola de BD
                                                actualizarImagen();
                                            }
                                        }
                                    });
                            WorkManager.getInstance(this).enqueue(otwrImagen);
                        }
                    } else {
                        Log.d("TakenPicture", "No photo taken");
                    }
                });

        // boton cambiar imagen de perfil
        Button botonCambiarImagen = (Button) findViewById(R.id.botonCambiarImagen);
        botonCambiarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent elIntentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureLauncher.launch(elIntentFoto);
            }
        });

        // boton marcadores
        Button botonMarcadores = (Button) findViewById(R.id.botonMarcadores);
        botonMarcadores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MarcadoresActivity.class);
                intent.putExtra("usuario", usuario);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });

        // boton ver mapa
        Button botonVerMapa = (Button) findViewById(R.id.botonVerMapa);
        botonVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapaActivity.class);
                intent.putExtra("usuario", usuario);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });
    }

    public void actualizarImagen() {
        // Se actualiza la imagen cogiendola de BD
        Data datos = new Data.Builder().putString("usuario", usuario).build();
        if (!usuario.isEmpty()) {
            OneTimeWorkRequest otwrGetImagen = new OneTimeWorkRequest.Builder(TareaGetImagenBD.class).setInputData(datos).build();
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrGetImagen.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if(workInfo != null && workInfo.getState().isFinished()){
                                String nombreFichero = workInfo.getOutputData().getString("nombreFichero");
                                System.out.println(nombreFichero);

                                FileInputStream ficheroImagen = null;
                                try {
                                    ficheroImagen = openFileInput(nombreFichero);
                                    if(ficheroImagen != null && ficheroImagen.available() > 0) {
                                        Bitmap imagenBitmap = BitmapFactory.decodeStream(ficheroImagen);
                                        ImageView elImageView = findViewById(R.id.imagenPerfil);
                                        elImageView.setImageBitmap(imagenBitmap);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ImageView elImageView = findViewById(R.id.imagenPerfil);
                                    elImageView.setImageResource(R.drawable.ic_launcher_foreground);
                                }
                            }
                        }
                    });
            WorkManager.getInstance(this).enqueue(otwrGetImagen);
        } else {
            Toast aviso = Toast.makeText(getApplicationContext(), "Por favor, rellena el usuario", Toast.LENGTH_SHORT);
            aviso.show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("usuario", usuario);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        usuario = savedInstanceState.getString("usuario");
        actualizarImagen();
    }
}
