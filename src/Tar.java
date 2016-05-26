import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjcambilr on 23/05/16.
 */
public class Tar {
    private String filename = "";
    public List<InsideFiles> lista = new ArrayList<>();
    public boolean ArchivoEncontrado = false;

    // Constructor
    public Tar(String filename) {
        this.filename = filename;
        File f = new File(this.filename);
        if (f.exists()) {
            ArchivoEncontrado = true;
        }
    }

    // Torna un array amb la llista de fitxers que hi ha dins el TAR
    public String[] list() throws IOException {
        String[] listaArchivos = new String[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            listaArchivos[i] = lista.get(i).getNom();
        }
        return listaArchivos;
    }

    // Torna un array de bytes amb el contingut del fitxer que té per nom
// igual a l'String «name» que passem per paràmetre
    public byte[] getBytes(String name) {
        for (int i = 0; i < lista.size(); i++) {
            if (name.equals(lista.get(i).getNom())) {
                return lista.get(i).getContenido();
            }
        }
        return null;
    }


    // Expandeix el fitxer TAR dins la memòria
    public void expand() {
        try {
            InputStream is = new FileInputStream(filename);
            String nom = "";
            int c;
            int size;
            String octal = "";
            while ((c = is.read()) != -1) {
                for (int i = 0; i < 100; i++) {
                    if (c > 0) {
                        nom += (char) c;
                    }
                    c = is.read();
                }
                if (nom.equals("")) break;
                is.skip(24);
                for (int i = 0; i < 11; i++) {
                    if (c != 0) {
                        octal += (char) c;
                    }
                    c = is.read();
                }
                size = Integer.parseInt(octal, 8);
                is.skip(376);
                ByteArrayOutputStream contenido = new ByteArrayOutputStream();
                for (int i = 0; i < size; i++) {
                    contenido.write(is.read());
                }
                byte[] con = contenido.toByteArray();
                int resto = 512 - (size % 512);
                is.skip(resto);
                lista.add(new InsideFiles(nom, size, con));
                nom = "";
                octal = "";
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class InsideFiles {
    private String nom = "";
    private int size = 0;

    public byte[] getContenido() {
        return contenido;
    }

    private byte[] contenido;

    public String getNom() {

        return nom;
    }

    public String toString() {
        return "nombre: " + nom + "\n" +
                "Tamaño: " + size + "\n" +
                "Contenido: " + contenido;
    }

    public InsideFiles(String nom, int size, byte[] contenido) {
        this.contenido = contenido;
        this.nom = nom;
        this.size = size;
    }
}

class Programa {

    public Tar tar;

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner s = new Scanner(System.in);
        boolean fin = false;
        Programa p = new Programa();
        while (!fin) {
            System.out.println("Introduzca los comandos: ");
            String cadena = s.nextLine();
            ListaComandos listado = new ListaComandos(cadena);
            String[] comandos = listado.ClasificarTexto();
            if (comandos.length == 0) {
                System.out.println("No has introducido nigun comando\n");
                continue;
            }
            switch (comandos[0]) {
                case "load":
                    if (comandos.length != 2)
                        System.out.println("El comando load solo necesita el parametro 'ruta_archivo_tar'\n");
                    else {
                        p.load(comandos[1]);
                    }
                    break;
                case "list":
                    if (comandos.length != 1) System.out.println("El comando list no tiene parametro\n");
                    else p.list();
                    break;
                case "extract":
                    if (comandos.length != 3)
                        System.out.println("El comando extract necesita dos parametros 'Nombre_del_archivo_extraer' 'ruta_destino/nombre_archivo'\n");
                    else p.extract(comandos[1], comandos[2]);
                    break;
                case "exit":
                    fin = true;
                    System.out.println("Cerrando el programa...");
                    break;
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

    public void load(String ruta) throws InterruptedException {
        tar = new Tar(ruta);
        System.out.println("Buscando archivo...");
        if (tar.ArchivoEncontrado) {
            System.out.println("Cargando en memoria...");
            tar.expand();
            System.out.println("Listo para ser utilizado\n");
        } else {
            System.out.println("El archivo no existe\n");
        }
    }

    public void list() throws IOException, InterruptedException {
        if (tar != null) {
            String[] lista = tar.list();
            for (int i = 0; i < lista.length; i++) {
                System.out.println((i + 1) + ". Archivo: " + lista[i]);
            }
        } else {
            System.out.println("Todavia no se a cargado ningun archivo tar, utilize el comando 'load'\n");
        }
    }

    public void extract(String nombre, String destino) throws IOException, InterruptedException {
        if (tar != null) {
           try {
               if (nombre.equals("-all")) {
                   String sSistemaOperativo = System.getProperty("os.name");
                   String[] archivos = tar.list();
                   if (destino.charAt(destino.length() - 1) != 92 && destino.charAt(destino.length() - 1) != 47) {
                       Pattern p = Pattern.compile("Windows*");
                       Matcher m = p.matcher(sSistemaOperativo);
                       boolean b = m.matches();
                       if (b) destino += (char) 92;
                       else destino += "/";
                   }
                   System.out.println("Extrayendo los datos..");
                   System.out.println("Creando el archivo..");
                   for (int i = 0; i < archivos.length; i++) {
                       String ruta = destino + archivos[i];
                       FileOutputStream allArchivos = new FileOutputStream(ruta);
                       allArchivos.write(tar.getBytes(archivos[i]));
                       allArchivos.close();
                   }
                   System.out.println("Archivo creado en " + destino + "\n");
               } else {
                   FileOutputStream nuevoArchivo = new FileOutputStream(destino);
                   nuevoArchivo.write(tar.getBytes(nombre));
                   System.out.println("Extrayendo los datos..");
                   System.out.println("Creando el archivo..");
                   nuevoArchivo.close();
                   System.out.println("Archivo creado en " + destino + "\n");
               }
           }catch (NullPointerException nl){
               System.out.println("El archivo no existe");
           }catch (FileNotFoundException fl){
               System.out.println("El directorio no existe");
           }

        } else {
            System.out.println("Todavia no se a cargado ningun archivo tar, utilize el comando 'load'\n");
        }
    }
}

class ListaComandos {

    private String texto;

    public ListaComandos(String texto) {
        this.texto = texto;
    }

    public String[] ClasificarTexto() {
        String[] comandos = texto.split(" +");

        return comandos;
    }
}
