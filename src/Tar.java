import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjcambilr on 23/05/16.
 */
public class Tar {
    //Variables de clase
    private String filename = "";
    private final List<InsideFiles> lista = new ArrayList<>();
    public boolean ArchivoEncontrado = false;

    // Constructor
    public Tar(String filename) {
        this.filename = filename;
        File f = new File(this.filename);
        if (f.exists()) {
            ArchivoEncontrado = true;
        }
    }

    // Metodo list
    public String[] list() {
        String[] listaArchivos = new String[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            //Introducimos los nombres de los archivos dentro de la array
            listaArchivos[i] = lista.get(i).getNom();
        }
        return listaArchivos;
    }

    //Metodo GetByte
    public byte[] getBytes(String name) {
        for (InsideFiles archivoLista : lista) {
            //Obtenemos el contenido del archivo cuyo nombre se introduce
            if (name.equals(archivoLista.getNom())) {
                return archivoLista.getContenido();
            }
        }
        return null;
    }


    // Metodo expand
    public void expand() {
        try {
            InputStream is = new FileInputStream(filename);
            String nom = "";
            int c;
            int size;
            String octal = "";
            //Control del final
            while ((c = is.read()) != -1) {
                //Obtenemos nombre de archivo
                for (int i = 0; i < 100; i++) {
                    if (c > 0) {
                        nom += (char) c;
                    }
                    c = is.read();
                }
                //Comprobamos si esta vacio
                if (nom.equals("")) break;
                //Nos saltamos esta informacion
                is.skip(24);
                //Obtenemos contenido en base Octal
                for (int i = 0; i < 11; i++) {
                    if (c != 0) {
                        octal += (char) c;
                    }
                    c = is.read();
                }
                //Conversor octal a decimal
                size = Integer.parseInt(octal, 8);
                //Completamos el bloque de 512B
                is.skip(376);
                ByteArrayOutputStream contenido = new ByteArrayOutputStream();
                for (int i = 0; i < size; i++) {
                    //Guardamos los bytes del contenido
                    contenido.write(is.read());
                }
                byte[] con = contenido.toByteArray();
                //Obtenemos los bytes necesarios para completar bloque de 512B
                int resto = 512 - (size % 512);
                is.skip(resto);
                //Añadimos Inside file a la lista.
                lista.add(new InsideFiles(nom, size, con));
                nom = "";
                octal = "";
            }
        //Captura de cualquier eror
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


class InsideFiles {
    private String nom = "";
    private int size = 0;
    private final byte[] contenido;

    //Metodo Get del contenido
    public byte[] getContenido() {
        return contenido;
    }

    //Metodo Get del Nombre
    public String getNom() {

        return nom;
    }

    //Metodo toString
    public String toString() {
        return "nombre: " + nom + "\n" +
                "Tamaño: " + size + "\n" +
                "Contenido: " + Arrays.toString(contenido);
    }

    //Constructor InsideFiles
    public InsideFiles(String nom, int size, byte[] contenido) {
        this.contenido = contenido;
        this.nom = nom;
        this.size = size;
    }
}

//clas Program
class Programa {

    private Tar tar;

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner s = new Scanner(System.in);
        boolean fin = false;
        //Iniciamos porgrama
        Programa p = new Programa();
        while (!fin) {
            System.out.println("Introduzca los comandos(introduce help para mas ayuda): ");
            //Obtenemos entrada
            String cadena = s.nextLine();
            ListaComandos listado = new ListaComandos(cadena);
            //Dividimos cadena de texto
            String[] comandos = listado.ClasificarTexto();
            if (comandos.length == 0) {
                System.out.println("No has introducido nigun comando\n");
                continue;
            }
            switch (comandos[0]) {
                //Obtenemos InsideFiles
                case "load":
                    if (comandos.length != 2)
                        System.out.println("El comando load solo necesita el parametro 'ruta_archivo_tar'\n");
                    else {
                        p.load(comandos[1]);
                    }
                    break;
                //Lista de archivos
                case "list":
                    if (comandos.length != 1) System.out.println("El comando list no tiene parametro\n");
                    else p.list();
                    break;
                //Extraccion de archivos
                case "extract":
                    if (comandos.length != 3)
                        System.out.println("El comando extract necesita dos parametros 'Nombre_del_archivo_extraer' 'ruta_destino/nombre_archivo'\n");
                    else p.extract(comandos[1], comandos[2]);
                    break;
                //Salida del programa
                case "exit":
                    fin = true;
                    System.out.println("Cerrando el programa...");
                    break;
                //Ayuda
                case "help":
                    System.out.println("Guia de comandos:\n" +
                            "--------------------------------\n" +
                            "load: Cargar archivo tar en memoria\n" +
                            "      uso: load  'ruta_archivo.tar \n" +
                            "list: listar archivos dentro de tar   \n" +
                            "      uso: list  \n" +
                            "extract: Extraer archivo del tar     \n" +
                            "      uso: extract 'Nombre_del_archivo_extraer' 'ruta_destino/nombre_del_archivo'  Extraer un archivo del tar\n" +
                            "      uso: extract -all 'ruta_destino'  Extraer todos los archivos del tar\n" +
                            "--------------------------------\n");
                    break;
                default:
                    System.out.println("El " + comandos[0] + " no existe\n");
            }

        }
    }

    //Metodo load
    private void load(String ruta) {
        tar = new Tar(ruta);
        System.out.println("Buscando archivo...");
        if (tar.ArchivoEncontrado) {
            System.out.println("Cargando en memoria...");
            //LLamada al metodo expand de la clase tar
            tar.expand();
            System.out.println("Listo para ser utilizado\n");
        } else {
            System.out.println("El archivo no existe\n");
        }
    }

    //Metodo list
    private void list() throws IOException {
        if (tar != null) {
            //LLamada al metodo list de la clase tar
            String[] lista = tar.list();
            for (int i = 0; i < lista.length; i++) {
                System.out.println((i + 1) + ". Archivo: " + lista[i]);
            }
        } else {
            System.out.println("Todavia no se a cargado ningun archivo tar, utilize el comando 'load'\n");
        }
    }

    //Metodo extract
    private void extract(String nombre, String destino) throws IOException {
        if (tar != null) {
           try {
               //Extraccion de todos loa archivos
               if (nombre.equals("-all")) {
                   String sSistemaOperativo = System.getProperty("os.name");
                   String[] archivos = tar.list();
                   //Caso en que la ruta le falte el '\' o '/' final
                   if (destino.charAt(destino.length() - 1) != 92 && destino.charAt(destino.length() - 1) != 47) {
                       //Comprobacion de sistema operativo
                       Pattern p = Pattern.compile("Windows*");
                       Matcher m = p.matcher(sSistemaOperativo);
                       if (m.matches()) destino += (char) 92;
                       else destino += "/";
                   }
                   System.out.println("Extrayendo los datos..");
                   System.out.println("Creando el archivo..");
                   //Extraccion de archivos con el nombre de archivo.
                   for (String archivo : archivos) {
                       String ruta = destino + archivo;
                       FileOutputStream allArchivos = new FileOutputStream(ruta);
                       allArchivos.write(tar.getBytes(archivo));
                       allArchivos.close();
                   }
                   System.out.println("Archivos creados en " + destino + "\n");
                   //Extraccion de un solo archivo, puede elegir el nombre final del archivo
               } else {
                   FileOutputStream nuevoArchivo = new FileOutputStream(destino);
                   nuevoArchivo.write(tar.getBytes(nombre));
                   System.out.println("Extrayendo los datos..");
                   System.out.println("Creando el archivo..");
                   nuevoArchivo.close();
                   System.out.println("Archivo creado en " + destino + "\n");
               }
               //Archivo no encontrado
           }catch (NullPointerException nl){
               System.out.println("El archivo no existe");
           }catch (FileNotFoundException fl){
               //Directorio no encontrado
               System.out.println("la ruta no existe o no es posible acceder");
           }
            //Comando no existente
        } else {
            System.out.println("Todavia no se a cargado ningun archivo tar, utilize el comando 'load'\n");
        }
    }
}

//Clase ListaComandos
class ListaComandos {

    private final String texto;
    //Constructor
    public ListaComandos(String texto) {
        this.texto = texto;
    }
    //Metodo clasificar Texto
    public String[] ClasificarTexto() {
        //Divisinos del texto por espacios.
        return texto.split(" +");
    }
}
