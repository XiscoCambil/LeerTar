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
    public boolean existe = false;

    // Constructor
    public Tar(String filename) {
        this.filename = filename;
        File f = new File(this.filename);
        if(f.exists()){
            existe = true;
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
            System.out.println(lista);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {
        Programa p = new Programa();
        p.menu();
        p.ControlEleccion();
    }
}

class InsideFiles {
    private String nom = "";
    private int size = 0;

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    private byte[] contenido;

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getNom() {

        return nom;
    }

    public int getSize() {
        return size;
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
    private int option = 0;
    Tar t;
    Scanner s = new Scanner(System.in);

    public Programa() {

    }

    public void menu() {
        while (option < 1 || option > 3) {
            System.out.println(
                    "Introduce la accion a realizar \n" +
                            "1. Cargar archivo \n" +
                            "2. Listar contenido de archivo \n" +
                            "3. Extraer archivo \n" +
                            "Selecciona la opcion que desea: "
            );
            option = s.nextInt();

        }

    }

    public void ControlEleccion() {
        switch (option) {
            case 1:
//                load();
        }
    }

    public void load() throws FileNotFoundException{
        boolean ruta = false;
        while (!ruta){
            System.out.println("Introduce la ruta del archivo: ");
            String rutaArchivoEncontrar = s.next();
            ruta = t.existe;
        }
        System.out.println("El archivo no se ha podido encontrar!");

    }
}