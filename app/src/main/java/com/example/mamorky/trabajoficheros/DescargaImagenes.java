package com.example.mamorky.trabajoficheros;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mamorky on 25/11/17.
 */

public class DescargaImagenes extends AppCompatActivity implements View.OnClickListener {

    public static Drawable imgError;

    private Button btnDescarga;
    private EditText edtFichImagenes, edtFichFrases;
    private ImageView imgIMagen;
    private TextView txvFrase;

    private static ArrayList<String> imagesText;
    private static ArrayList<Bitmap> images;
    private static ArrayList<String> frases;

    private static int flagImg = 0;
    private static int flagFrase = 0;
    private static int intervalo = 0;

    private static final String URLERROR = "alumno.mobi/~alumno/superior/bujalance/errores.txt";

    private Handler handler;
    private Runnable runnable;
    private static boolean correHandler = false;

    public static String mensajeExcepcionHilos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga_imagenes);

        //Obtenemos la imagen de error de los recursos y la guardamoa en una variable publica y
        //estática para poder acceder desde otras clases
        imgError = getResources().getDrawable(R.drawable.error);

        //Obtenemos todos los views del layout
        edtFichImagenes = (EditText) findViewById(R.id.edtFichImagenes);
        edtFichFrases = (EditText) findViewById(R.id.edtFichFrases);
        imgIMagen = (ImageView) findViewById(R.id.imgImagen);
        txvFrase = (TextView) findViewById(R.id.txvFrase);
        btnDescarga = (Button) findViewById(R.id.btnDescargar);

        //Apuntamos el botón de descarga al evento OnClickListener de esta clase
        btnDescarga.setOnClickListener(this);

        //Inicializamos los ArrayList
        imagesText = new ArrayList<String>();
        images = new ArrayList<Bitmap>();
        frases = new ArrayList<String>();

        //Creamos un InputStream con el archivo intervalo que es el que tendrá el intervalo de tiempo entre imagen e imagen
        InputStream streamIntervalo = getApplicationContext().getResources().openRawResource(R.raw.intervalo);
    }

    public boolean descargaEnlacesFrases() {
        btnDescarga.setText("Descarga de enlaces y frases ...");
        HiloLeerArchivos hiloLee = new HiloLeerArchivos(edtFichImagenes.getText().toString(), edtFichFrases.getText().toString(), imagesText, frases);
        hiloLee.Esperame();

        if (mensajeExcepcionHilos != null) {
            Toast.makeText(this, mensajeExcepcionHilos, Toast.LENGTH_LONG).show();
            mensajeExcepcionHilos = null;
            cerrarManejador();
            return false;
        }

        return true;
    }

    public boolean descargarImagenes() {
        btnDescarga.setText("Descarga de imagenes ...");
        HiloDescargaImagenes hiloDescargaImagenes = new HiloDescargaImagenes(imagesText, images);

        hiloDescargaImagenes.Esperame();

        if (mensajeExcepcionHilos != null) {
            Toast.makeText(this, mensajeExcepcionHilos, Toast.LENGTH_LONG).show();
            mensajeExcepcionHilos = null;
            cerrarManejador();
            return false;
        }

        return true;
    }

    private void obtenerIntervalo() {
        //Creamos un InputStream con el archivo intervalo que es el que tendrá el intervalo de tiempo entre imagen e imagen
        InputStream streamIntervalo = getApplicationContext().getResources().openRawResource(R.raw.intervalo);

        //Se encargará de leer el fichero donde tenemos el intervalo
        BufferedReader br = new BufferedReader(new InputStreamReader(streamIntervalo));

        try {
            //Como lo hemos guaradado en la primera linea solo leemos esta
            intervalo = Integer.valueOf(br.readLine());
        } catch (IOException e) {
            //En caso de haber excepción establecemos 1 segundo como el valor del intervalo
            intervalo = 1;
            DescargaImagenes.guardarErrores(e.getMessage());
            e.printStackTrace();
            Toast.makeText(DescargaImagenes.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Avanza en las imagenes de forma círcular
    public void avanzarImagen() {
        int nextImage = flagImg++ % imagesText.size();
        imgIMagen.setImageBitmap(images.get(nextImage));
    }

    //Avanza en las frases de forma círcular
    public void avanzarFrases() {
        String nextFrase = frases.get(flagFrase++ % frases.size());
        txvFrase.setText(nextFrase);
    }

    private void cerrarManejador() {
        if (handler != null)
            handler.removeCallbacks(runnable);
        correHandler = false;

        //Cuando paramos se borran todas las imagenes y frases
        images.clear();
        imagesText.clear();
        imagesText.clear();

        btnDescarga.setText("DESCARGAR");
    }

    private void recorrerImagenes() {
        if (images.size() > 0) {
            Toast.makeText(this, "Descargado", Toast.LENGTH_LONG).show();

            obtenerIntervalo();

            try {
                handler = new Handler();

                runnable = new Runnable() {
                    public void run() {
                        avanzarImagen();
                        avanzarFrases();
                        handler.postDelayed(this, intervalo * 1000);
                    }
                };

                handler.postDelayed(runnable, intervalo * 1000);
                correHandler = true;
                btnDescarga.setText("PARAR");
            } catch (Exception e) {
                Toast.makeText(DescargaImagenes.this, e.getMessage(), Toast.LENGTH_LONG).show();
                cerrarManejador();
            }
        } else
            cerrarManejador();
    }

    /**
     * Sin soculción
     *
     * @param error
     */
    public static void guardarErrores(String error) {
        try {
            byte[] bytes = error.getBytes();

            URL url = new URL("http://" + URLERROR);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnDescarga && !correHandler) {

            //Se llama a los métodos de descarga y si alguno falla se deja de ejecutar el onClick()
            if (!descargaEnlacesFrases() || !descargarImagenes())
                return;

            recorrerImagenes();
        } else if (v == btnDescarga && correHandler) {
            cerrarManejador();
        }
    }
}

class HiloLeerArchivos implements Runnable {

    String rutaImagenes;
    String rutaFrases;
    ArrayList<String> images;
    ArrayList<String> frases;
    Thread hilo;

    public HiloLeerArchivos(String rutaImagenes, String rutaFrases, ArrayList<String> images, ArrayList<String> frases) {
        this.rutaImagenes = rutaImagenes;
        this.rutaFrases = rutaFrases;
        this.images = images;
        this.frases = frases;
        hilo = new Thread(this);
        hilo.start();
    }

    @Override
    public void run() {
        leerFicheroUrl(rutaImagenes, images);
        leerFicheroUrl(rutaFrases, frases);
    }

    public void Esperame() {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            DescargaImagenes.guardarErrores(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método que recibe la ruta donde se encuentra un fichero lo lee y lo guarda en la lista de string pasada
     */
    public void leerFicheroUrl(String ruta, ArrayList<String> guardado) {
        String linea = "";
        try {
            //Recibimos la URL
            URL url = new URL(ruta);

            //Creamos un bufferReader con lo que leeremos el fichero de la web
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((linea = in.readLine()) != null) {
                guardado.add(linea);
            }
            in.close();
        } catch (Exception e) {
            DescargaImagenes.guardarErrores(e.getMessage());
            DescargaImagenes.mensajeExcepcionHilos = e.getMessage();
            e.printStackTrace();
        }
    }
}

class HiloDescargaImagenes implements Runnable {
    ArrayList<String> imagesText;
    ArrayList<Bitmap> images;
    Thread hilo;

    public HiloDescargaImagenes(ArrayList<String> imagesText, ArrayList<Bitmap> images) {
        this.imagesText = imagesText;
        this.images = images;
        hilo = new Thread(this);
        hilo.start();
    }

    @Override
    public void run() {
        descargaImagenes();
    }

    public void Esperame() {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            DescargaImagenes.guardarErrores(e.getMessage());
            e.printStackTrace();
        }
    }

    private void descargaImagenes() {
        for (int i = 0; i < imagesText.size(); i++) {
            Bitmap bitmap = null;
            try {
                //Descargamos el contenido del archivo de la imagen como un stream
                InputStream input = new URL(imagesText.get(i)).openStream();

                //Lo decodificamos en un formato imagen y lo guardamos como bitmap
                bitmap = BitmapFactory.decodeStream(input);

                //Añadimos la imagen al array de imagenes a mostrar
                images.add(bitmap);
            } catch (FileNotFoundException e) {
                DescargaImagenes.guardarErrores(e.getMessage());
                DescargaImagenes.mensajeExcepcionHilos = e.getMessage();
            } catch (Exception e) {
                DescargaImagenes.guardarErrores(e.getMessage());
                //En caso de haber una excepción en alguna de las imagenes se pondrá una imagen de error
                Bitmap bitmapError = ((BitmapDrawable) DescargaImagenes.imgError).getBitmap();
                images.add(bitmapError);
                e.printStackTrace();
            }
        }
    }
}


