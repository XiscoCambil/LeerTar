import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        boolean activado = false;
        boolean fin = false;
        Programa p = new Programa();
        while (!fin) {
            System.out.println("Introduzca los comandos: ");
            String cadena = s.nextLine();
            ListaComandos listado = new ListaComandos(cadena);
            Comando[] comandos = listado.ClasificarTexto();
            System.out.println(comandos.length);
            System.out.println(comandos[0].getTexto());
            switch (comandos[0].getTexto()) {
                case "load":
                    p.load(comandos[1].getTexto());
                    break;
                case "list":
                    p.list();
                    break;
                case "extract":
                    p.extract(comandos[1].getTexto(), comandos[2].getTexto());
                    break;
                case "exit":
                   fin = true;
                    System.out.println("adios");
                    break;
                default:
                    System.out.println("El comando no es correcto");
            }

        }
    }

    public void load(String ruta) {
        tar = new Tar(ruta);
        if (tar.ArchivoEncontrado) {
            System.out.println("Archivo encontrado");
            System.out.println("Cargando en memoria");
            tar.expand();
        } else {
            System.out.println("El archivo no existe");

        }
    }

    public void list() throws IOException {
        if (tar != null) {
            String[] lista = tar.list();
            for (int i = 0; i < lista.length; i++) {
                System.out.println((i + 1) + ". Archivo: " + lista[i]);
            }
        } else {
            System.out.println("Todavia no se a cargado ningun archivo");

        }
    }

    public void extract(String nombre, String destino) throws IOException {
        if (tar != null) {
            FileOutputStream nuevoArchivo = new FileOutputStream(destino);
            nuevoArchivo.write(tar.getBytes(nombre));
            nuevoArchivo.close();
        } else {
            System.out.println("Todavia no se a cargado ningun archivo");

        }
    }


}


class Comando {

    public String getTexto() {
        return texto;
    }

    private String texto;

    public Comando(String texto) {
        this.texto = texto;
    }

}

class ListaComandos {

    private String texto;

    public ListaComandos(String texto) {
        this.texto = texto;
    }

    public Comando[] ClasificarTexto() {
        String chunk = "";
        List<Comando> cadena = new ArrayList<>();
        for (int i = 0; i < texto.length(); i++) {
            if (i == texto.length() - 1) {
                chunk += texto.charAt(i);
                cadena.add(new Comando(chunk));
                break;
            }
            if (texto.charAt(i) != 32) {
                chunk += texto.charAt(i);
            } else {
                cadena.add(new Comando(chunk));
                chunk = "";
            }
        }
        Comando[] comandos = new Comando[cadena.size()];
        return cadena.toArray(comandos);

    }

}