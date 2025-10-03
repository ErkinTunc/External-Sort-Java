
/**
 * Q1: BufferedReader fichierInit : permet de lire le fichier CSV initial de façon efficace.
 *     C’est efficace car il lit les données avec un buffer(un block), donc on n’a pas besoin de charger
 *     tout le fichier en mémoire et on évite de faire une lecture pour chaque caractère.
 *
 * Q2: int[] indices : ce tableau contient, pour chaque colonne demandée pour le tri,
 *     sa position (indice) dans l'entête du fichier CSV.
 *     Exemple: si on trie par ["Nom","Age"] et que dans le CSV l'entête est
 *     ["Id","Nom","Ville","Age"], alors indices = [1,3].
 *     Cela permet au comparateur de savoir sur quelles colonnes et dans quel ordre
 *     comparer les lignes.               
 * 
 * Q5: Quand on compare les n-uplets (9) et (10) sur la colonne COM,
 *     on obtient "Andert-et-Condon" < "Anglefort".
 *     La raison: String.compareTo fait une comparaison lexicographique
 *     caractère par caractère. Ici, "Andert..." vient avant "Anglefort"
 *     dans l’ordre alphabétique.
 * 
 *     Si on compare avec deux colonnes (REG, COM),
 *     les deux REG sont identiques ("Auvergne-Rhône-Alpes"),
 *     donc la comparaison continue sur COM et donne le même résultat.
 **/

import java.io.*;
import java.util.*;

public class TriExterne {
    // size of the cache in number of n-uplet
    // M >= 3
    public static int M = 10;
    public final String[][] cache; // cache en mémoire pour stocker temporairement les lignes
    public final String path; // chemin du fichier CSV à trier
    public final String[] entete; // liste des noms de colonnes du fichier CSV
    public final Comparateur comparateur; // comparateur pour trier les lignes selon les colonnes demandées
    public final BufferedReader fichierInit; // Reads text from a character-input stream

    /**
     * Constructeur de TriExterne : ouvre le fichier CSV et prépare le tri.
     * - Lit l'entête du fichier pour récupérer les noms de colonnes.
     * - Vérifie que les colonnes demandées pour le tri existent.
     * - Crée un comparateur basé sur ces colonnes.
     * - Initialise un cache en mémoire pour stocker temporairement les lignes.
     *
     * @param path     le chemin du fichier CSV à trier
     * @param colonnes le nom des colonnes d'une table (comme dans une clause ORDER
     *                 BY en SQL)
     * @throws FileNotFoundException si le fichier n'existe pas
     * @throws IOException           si le fichier n'existe pas ou si une erreur de
     *                               lecture
     */
    public TriExterne(String path, String[] colonnes) throws FileNotFoundException, IOException {
        this.path = path;
        this.fichierInit = new BufferedReader(new FileReader(path));
        // liste des noms de colonnes du fichier CSV
        this.entete = fichierInit.readLine().split(";");

        int[] indices = new int[colonnes.length]; // indices des colonnes pour le tri
        for (int i = 0; i < colonnes.length; i++) {
            indices[i] = -1;
            for (int indice = 0; indice < entete.length; indice++)
                if (entete[indice].equals(colonnes[i]))
                    indices[i] = indice;

            if (indices[i] == -1)
                throw new RuntimeException(
                        String.format("La colonne \"%s\" n'existe pas dans l'entête du fichier CSV", colonnes[i]));
        }

        this.comparateur = new Comparateur(indices);
        this.cache = new String[M][entete.length];
    }

    /**
     * Tri le fichier CSV en utilisant un tri externe
     * 
     * @throws FileNotFoundException si le fichier n'existe pas
     * @throws IOException           si le fichier n'existe pas ou si une erreur de
     *                               lecture
     */
    public void trier() throws FileNotFoundException, IOException {
    }

    /**
     * tri le cache entre les indices [0,(taille-1)]
     * <p>
     * - Arrays.sort(array,0,taille ,comparateur) permet de trier une partie d'un
     * tableau
     * </p>
     * 
     * @param taille le nombre d'éléments à trier (<= M)
     */
    private void trierCache(int taille) {
        Arrays.sort(cache, 0, taille, comparateur);
    }

    /**
     * écrit une ligne dans un fichier CSV pour les stocker les valeurs
     * 
     * @param fw      le FileWriter du fichier CSV
     * @param valeurs le nuplet (tableau de String)
     * @throws IOException si une erreur d'écriture dans le fichier se produit
     */
    private static void ecrireLigneCSV(FileWriter fw, String[] valeurs) throws IOException {
        String mot = "";
        for (int i = 0; i < valeurs.length; i++) {
            mot += valeurs[i];
            if (i != valeurs.length - 1) {
                mot += ";";
            }
        }
        mot += "\n";
        fw.append(mot);
    }

    // écrit dans un fichier CSV le contenu du cache de l'indice 0 à taille - 1
    // (avec taille <= M)
    private void sauvegardeCache(String path, String[] entete, int taille) throws IOException {
    }

    // lit le fichier CSV inital et le diviser en fichiers triée de taille M à
    // l'exception du dernier
    // retourne le nombre de nuplets lus
    private int creerFragmentsInitiaux() throws IOException {
        return 0;
    }

    /**
     * Fusionne les fichiers fragment_niveau_numero.csv
     * 
     * @param niveau le niveau de fusion
     * @param nombre le nombre de fichiers à fusionner
     * @return le nombre de fichiers créés
     * @throws FileNotFoundException si le fichier n'existe pas
     * @throws IOException           si le fichier n'existe pas ou si une erreur de
     *                               lecture
     */
    private int fusion(int niveau, int nombre) throws FileNotFoundException, IOException {
        return 0;
    }

    public String toString() {
        return Arrays.toString(this.cache);
    }

    /**
     * Nom du fichier fragment pour un niveau et un numéro donné
     * 
     * @param niveau le niveau de fusion
     * @param numero le numéro du fragment
     * @return le nom du fichier fragment
     */
    private static String nomDeFragment(int niveau, int numero) {
        return String.format("fragment_%s_%s.csv", niveau, numero);
    }

    /**
     * Convertit une ligne CSV en un nuplet (tableau de String)
     * 
     * @param ligne la ligne CSV
     * @return le nuplet (tableau de String)
     */
    public static String[] nupletDepuis(String ligne) {
        return ligne.split(";");
    }

    public static void main(String[] args) throws Exception {

        // test du comparateur
        testComparateur();

        // exécution du tri externe
        if (args.length < 2) {
            System.out.println("Le premier argument est un fichier CSV");
            System.out.println("Le deuxième argument est les colonnes pour le tri");
            return;
        }

        long t1 = System.currentTimeMillis();

        TriExterne algo = new TriExterne(args[0], args[1].split(";"));

        algo.trier();

        System.out.println("Temps d'éxécution : " + (System.currentTimeMillis() - t1) + "ms");

    }

    /**
     * Q4: Teste le comparateur avec une colonne (COM) puis avec deux colonnes (REG,
     * COM).
     */
    public static void testComparateur() {
        String[] entete = "CODREG;REG;CODDEP;CODARR;CODCAN;CODCOM;COM;PMUN;PCAP;PTOT".split(";");

        // deux lignes de données
        String[] t1 = "84;Auvergne-Rhône-Alpes;01;1;04;009;Andert-et-Condon;326;9;335".split(";");
        String[] t2 = "84;Auvergne-Rhône-Alpes;01;1;10;010;Anglefort;1105;17;1122".split(";");

        // --- Test avec une colonne (COM) ---
        int colCom = -1;
        for (int i = 0; i < entete.length; i++) {
            if (entete[i].equals("COM")) {
                colCom = i;
                break;
            }
        }
        Comparateur comp1 = new Comparateur(new int[] { colCom });
        int res1 = comp1.compare(t1, t2);
        System.out.println(
                "Comparaison avec COM : " + t1[colCom] + (res1 < 0 ? " < " : res1 > 0 ? " > " : " == ") + t2[colCom]);

        // --- Test avec deux colonnes (REG puis COM) ---
        int colReg = -1;
        for (int i = 0; i < entete.length; i++) {
            if (entete[i].equals("REG")) {
                colReg = i;
                break;
            }
        }
        Comparateur comp2 = new Comparateur(new int[] { colReg, colCom });
        int res2 = comp2.compare(t1, t2);
        System.out.println("Comparaison avec REG, COM : "
                + t1[colReg] + " / " + t1[colCom]
                + (res2 < 0 ? " < " : res2 > 0 ? " > " : " == ")
                + t2[colReg] + " / " + t2[colCom]);
    }

}
