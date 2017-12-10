

**Trabajo ACDA 1º Trimestre**
============================================

>**<h3><b>Descarga de imagenes  y frases</b><h3>**


En este ejercio se descargan las imagenes y las frases desde el servidor alumnos.mobi. En un fichero se encontrarán las imagenes , mientras que en el otro fichero estarán las frases. Esto se mostrarán de forma automática.

<p>
>**<i>Estructura de Métodos Usados</i>**

<p>
>Clase Principal

>![enter image description here](https://lh3.googleusercontent.com/-tNIJ3Ll1qA8/Wi0wQ8uS7PI/AAAAAAAAJbY/cl0_WQVDiccJOKA66FBJNWZkqYHPeDRzwCLcBGAs/s2000/m%25C3%25A9todos.png "métodos.png")

<p>
>Hilo de descarga de enlaces y frases

>![enter image description here](https://lh3.googleusercontent.com/-r_DW9vtVEdU/Wi0xCkg6oRI/AAAAAAAAJbk/DQ5Qfy5QHIIXdfVeftNrUBdUbIoI4sLWACLcBGAs/s2000/metodosHilo.png "metodosHilo.png")

<p>
>Hilo de descarga de imágenes

>![enter image description here](https://lh3.googleusercontent.com/32FIm0bHOyhfb6_VBC-u1_G_KKsidGpL-QWSynD26xqOmwkTtOBR2RJGSGZgaYhVWXcZQG7sYnT8=s2000 "HiloDescargaImagenes.png")

<p>
>**Observaciones**

No he utilizado métodos asíncronos puesto que si usaba estos tenia un problema y es que el hilo principal seguía ejecutandose con lo cual lo que he hecho a sido crear dos hilos uno que descarga los enlaces y las frases y el otro que se encarga de descargar las imagenes, y para que el hilo principal no continue este llama al método esperar de los hilos("Este método llama al método join para que el hilo principal espere a que este hilo termine").

Tampoco se han usado librerías externas todo esta realizado con las librerías internas.

\#Las Imagenes se descargan no se leen desde internet. Estas se guardan como Bitmap en el ArrayList de images.

>**Problemas Encontrados**

Estos son alguno de los problemas que me he encontrado:

 - En un hilo que no sea el hilo principal no se pueden crear elementos View con lo cual un hilo que no sea el principal no puede crear ni un **ProgressDialog** ni un **Toast**.
 
 - **Conexión con el servidor** No he conseguido guardar el contenido de los errores en el fichero errores del servidor 

>**Soluciones a los Problemas**

Para el problema de los views en encontrado las siguientes soluciones:

- En vez de mostrar un ProgressDialog lo que hago es **cambiar el texto del botón de descarga** para que este muestre que se esta descargando

- En el caso de los Toast lo que hago es crear una variable de String llamada **mensajeExcepcionHilos** que alamacenará el error que se a producido en el hilo en caso de producirse. Cuando el hilo externo termina el hilo principal preguntara si este es null o no si no es null es por que se produjo un excepción y se mostrará el toast. La variable **mensajeExcepcionHilos** se inicializará a null si este fuera distinto después de mostrar el Toast.

- Para el problema de guardar los errores aun no he encontrado una solución 

>**Mejoras en la App**

- Se ha añadido la posibilidad de que si volvemos a pulsar el botón esta pare el proceso de pasar las imagenes y reinicialice los ArrayList y las banderas por si queremos volver a descargar. Esto se hace en el método **cerrarManejador**.

- Imagenes almacenadas en el dispositivo, si perdemos la conexión a internet estas deberían seguir pasandose sin problema a pesar de perder la conexión.

>**Imagen de muestra de la APP**

![enter image description here](https://lh3.googleusercontent.com/X2mVqY_u5KF3G42iOLiqpYYzvty3J9JmNmB61kxPK3R_85rgVaOGWs6yoW8PwkmcLbQyrJ4NKLbk=s800 "TrabajoFichero.png")

