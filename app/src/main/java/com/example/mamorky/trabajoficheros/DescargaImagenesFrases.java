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

public class DescargaImagenesFrases extends AppCompatActivity implements View.OnClickListener {

    //Imagen que se muestra cuando se produce un error
    public static Drawable imgError;

    //Views del layout
    private Button btnDescarga;
    private EditText edtFichImagenes, edtFichFrases;
    private ImageView imgIMagen;
    private TextView txvFrase;

    //ArrayList donde se guardará frases,enlaces e imagenes
    private static ArrayList<String> imagesText;
    private static ArrayList<Bitmap> images;
    private static ArrayList<String> frases;

    //Banderitas que usaremos para recorrer los ArrayList
    private static int flagImg = 0;
    private static int flagFrase = 0;
    private static int intervalo = 0;

    //Ruta donde se encuentra el archivo de Errores
    private static final String URLERROR = "http://alumno.mobi/~alumno/superior/bujalance/errores.txt";

    //Manejador,runable y booleano que indica si el handler esta corriendo
    private Handler handler;
    private Runnable runnable;
    private static boolean correHandler = false;

    //Cuando se produce una excepción en un hilo el mensaje se guarda aquí y este se muestra en un Toast
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

    /**
     * Método que se encargara de llamar al hilo que descarga todas los enlaces de las imagenes y las frases
     * @return true si consigue descargar con existo, si no false
     * */
    public boolean descargaEnlacesFrases() {

        //Un hilo no me permite crear un ProgressDialog con lo que mi alternativa es cambiar el texto del botón
        btnDescarga.setText("Descarga de enlaces y frases ...");

        //Se crea el hilo
        HiloLeerArchivos hiloLee = new HiloLeerArchivos(edtFichImagenes.getText().toString(), edtFichFrases.getText().toString(), imagesText, frases);

        //Hacemos que el hilo principal esperé a que este haya descargado todos los enlaces para continuar
        hiloLee.Esperame();

        if (mensajeExcepcionHilos != null) {
            Toast.makeText(this, mensajeExcepcionHilos, Toast.LENGTH_LONG).show();
            mensajeExcepcionHilos = null;
            cerrarManejador();
            return false;
        }

        return true;
    }

    /**
     * Método que llama al hilo de descarga de imagenes
     * @return true si descarga con existo
     * */
    public boolean descargarImagenes() {

        //Es la alternativa usada al ProgressDialog
        btnDescarga.setText("Descarga de imagenes ...");

        //Se crea el hilo de descarga de imagenes
        HiloDescargaImagenes hiloDescargaImagenes = new HiloDescargaImagenes(imagesText, images);

        //Se le dice al hilo principal que espera a la descarga de imagenes
        hiloDescargaImagenes.Esperame();

        if (mensajeExcepcionHilos != null) {
            Toast.makeText(this, mensajeExcepcionHilos, Toast.LENGTH_LONG).show();
            mensajeExcepcionHilos = null;
            cerrarManejador();
            return false;
        }

        return true;
    }

    //**
    // Método que obtiene el intervalo del archivo intervalos.txt
    // */
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
            DescargaImagenesFrases.guardarErrores(e.getMessage());
            e.printStackTrace();
            Toast.makeText(DescargaImagenesFrases.this, e.getMessage(), Toast.LENGTH_LONG).show();
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

    /**
     * El manejador a de ser cerrado pues este es quien se encarga de cerrarlo, es decir, elimina el proceso del hanlder
     * Este método también se encargará eliminar el contenido de las listas, y de volver a inicializar las banderitas
     * */
    private void cerrarManejador() {
        if (handler != null)
            handler.removeCallbacks(runnable);
        correHandler = false;

        //Cuando paramos se borran todas las imagenes y frases
        images.clear();
        imagesText.clear();
        frases.clear();

        //Tambien ponemos la banderita a cero
        flagFrase = 0;
        flagImg = 0;

        btnDescarga.setText("DESCARGAR");
    }

    /**
     * Recorre las imagenes crea un manejador y un runnable que se le asignará a este con un retardo
     * que es el intervalo obtenido del texto
     * */
    private void recorrerImagenes() {
            //Nos muestra un mensaje que si llegamos a este punto es por que la descarga se realizo con exito
            Toast.makeText(this, "Descargado", Toast.LENGTH_LONG).show();

            //Llamamos al método que obtiene el intervalo
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

                //Asiganmos al manejador el runnable
                handler.post(runnable);

                //Decimos que el handler esta corriendo
                correHandler = true;

                //Y cambiamos el texto del botón por parar para parar el proceso cuando nosotros pulsemos sobre este.
                btnDescarga.setText("PARAR");
            } catch (Exception e) {
                Toast.makeText(DescargaImagenesFrases.this, e.getMessage(), Toast.LENGTH_LONG).show();

                //Si se produce una excepción cerramos el manejador para que este no siga consumiendo
                cerrarManejador();
            }
    }

    /**SIN SOLUCIÓN
     * ------------
     * Este es el método que debería guardar los errores en el pero no he conseguido hayar la solución
     * @param error que se produce como string
     */
    public static void guardarErrores(String error) {
        try {
            //Pasa a bytes el error recibido
            byte[] bytes = error.getBytes();

            //Crea la URL
            URL url = new URL(URLERROR);

            //Crea la concexión con la clase HTTPURLConection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //La decimos que se puedan poner contenido en esta dirección
             urlConnection.setDoOutput(true);

            //Creamos un DataOutputStream que recibirá el stream de la conexión y  escribirá en este
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        //Si se pulsa el botón descarga y no esta andando el handler ...
        if (v == btnDescarga && !correHandler) {

            //... Se llama a los métodos de descarga y si alguno falla se deja de ejecutar el onClick()
            if (!descargaEnlacesFrases() || !descargarImagenes())
                return;

            //... Se recorren las imagenes
            recorrerImagenes();

        //Si se pulsa el botón de descarga y el handler esta corriendo, este elimina el proceso
        } else if (v == btnDescarga && correHandler) {
            cerrarManejador();
        }
    }
}

/**
 * Hilo que se encarga de descargar los enlaces de las imagenes y las frases
 * */
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

        //El hilo se debe de iniciar en el constructor debido a que implementamos la interfaz runnable
        hilo.start();
    }

    @Override
    public void run() {
        //Llamamos al método LeerFicheroURL que se encarga de  el fichero del servidor

        //Se descargan los enlaces de las imagenes y se guardan en el ArrayList de imagenes
        leerFicheroUrl(rutaImagenes, images);

        //Se descargan las frases y se guardan en el ArrayList de frases
        leerFicheroUrl(rutaFrases, frases);
    }

    //Método que usará el main para hacer que este lo espere
    public void Esperame() {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            DescargaImagenesFrases.guardarErrores(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método que recibe la ruta donde se encuentra un fichero del servidor, lo lee y lo guarda en la lista de string pasada
     * @param ruta -> Es la ruta donde se encuentra el fichero
     * @param guardado -> Donde se guardarán los string leidos
     */
    public void leerFicheroUrl(String ruta, ArrayList<String> guardado) {
        String linea = "";
        try {
            //Creamos una URL a partir de la ruta pasada
            URL url = new URL(ruta);

            //Creamos un bufferReader con lo que leeremos el fichero de la web
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            //Leemos todas las lineas del fichero
            while ((linea = in.readLine()) != null) {
                guardado.add(linea);
            }

            //Cerramos el fichero
            in.close();
        } catch (Exception e) {
            //Llama al método que guarda los errores
            DescargaImagenesFrases.guardarErrores(e.getMessage());


            DescargaImagenesFrases.mensajeExcepcionHilos = e.getMessage();
            e.printStackTrace();
        }
    }
}

/**
 * Hilo que se encargará de descargar las imagenes
 * */
class HiloDescargaImagenes implements Runnable {
    ArrayList<String> imagesText;
    ArrayList<Bitmap> images;
    Thread hilo;

    //El constructor del hilo recibe un ArrayList de string con la ruta donde estas se encuentran y
    // un ArrayList de Bitmap donde estas se guaradarán
    public HiloDescargaImagenes(ArrayList<String> imagesText, ArrayList<Bitmap> images) {
        this.imagesText = imagesText;
        this.images = images;
        hilo = new Thread(this);

        //Iniciamos el hilo dentro del constructor
        hilo.start();
    }

    @Override
    public void run() {
        //El run llama al método de descarga de imagenes
        descargaImagenes();
    }

    //Al implementar la interfaz runnable es necesario crear un método  para decirle al hilo que se espere
    public void Esperame() {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            DescargaImagenesFrases.guardarErrores(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Es el método encargado de la descarga de imagenes
     * */
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
                DescargaImagenesFrases.guardarErrores(e.getMessage());
                DescargaImagenesFrases.mensajeExcepcionHilos = e.getMessage();
            } catch (Exception e) {
                DescargaImagenesFrases.guardarErrores(e.getMessage());
                //En caso de haber una excepción en alguna de las imagenes se pondrá una imagen de error
                Bitmap bitmapError = ((BitmapDrawable) DescargaImagenesFrases.imgError).getBitmap();
                images.add(bitmapError);
                e.printStackTrace();
            }
        }
    }
}


