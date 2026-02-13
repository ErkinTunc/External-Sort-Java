
/**
 * TriExterne.java : implémente un tri externe sur un fichier CSV.
 * 
 * Fonctionnalités :
 * - Lit un fichier CSV avec une entête (noms de colonnes).
 * - Trie les lignes selon une ou plusieurs colonnes (comme ORDER BY en SQL).
 * - Utilise un tri externe avec un cache de taille M (nombre de lignes).
 * - Génère des fragments triés, puis les fusionne par groupes de (M-1).
 * - Produit un fichier trié final.
 * - Nettoie les fragments temporaires après usage.
 * - Permet de gérer des fichiers plus grands que la mémoire disponible.
 * 
 * Usage: java TriExterne <fichier.csv> <colonne1;colonne2;...> [<type1;type2;...>(optionnel)]
 * Exemple: java TriExterne data.csv "Nom;Age"
 *          java TriExterne data.csv "Nom;Age" "TXT;NUM"
 * 
 * Limite: M doit être au moins 3 pour permettre la fusion.
 * 
 * @author Erkin Tunc BOYA
 * @version 1.14
 * @since   2025-10-01
 * 
 * @see     Comparateur
 * @throws  RuntimeException si une colonne demandée n’existe pas dans l’entête
 */

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
 *
 * Q12: Trop de fragments pour Q12 (3499 > 9). Il faut avoir au plus M-1 fragments
 *      pour pouvoir les fusionner en une seule passe.
 *      Avec M=10, on peut fusionner au plus 9 fragments à la fois.
 * 
 **/

package tri_externe;

import java.io.*;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TriExterne {
    // size of the cache in number of n-uplet
    // M >= 3
    public static int M = 10;
    public final String[][] cache; // cache en mémoire pour stocker temporairement les lignes
    public final String path; // chemin du fichier CSV à trier
    public final String[] entete; // liste des noms de colonnes du fichier CSV
    public final Comparateur comparateur; // comparateur pour trier les lignes selon les colonnes demandées
    public final BufferedReader fichierInit; // Reads text from a character-input stream

    private final Path runDir; // tmp/fragments/run_YYYYMMDD_HHMMSS
    private final Path outputDir; // output/

    /**
     * Constructeur rétro-compatible : types AUTO (déduction à la volée).
     */
    public TriExterne(String path, String[] colonnes) throws IOException {
        this(path, colonnes, null);// null => AUTO
    }

    /**
     * Constructeur de TriExterne : ouvre le fichier CSV et prépare le tri.
     * - Lit l'entête du fichier pour récupérer les noms de colonnes.
     * - Vérifie que les colonnes demandées pour le tri existent.
     * - Crée un comparateur basé sur ces colonnes.
     * - Initialise un cache en mémoire pour stocker temporairement les lignes.
     *
     * <p>
     * Constructeur typé : force NUM/TXT par colonne (même longueur que colonnes).
     * </p>
     * 
     * @param path     le chemin du fichier CSV à trier
     * @param colonnes le nom des colonnes d'une table (comme dans une clause ORDER
     *                 BY en SQL)
     * @throws FileNotFoundException si le fichier n'existe pas
     * @throws IOException           si le fichier n'existe pas ou si une erreur de
     *                               lecture
     */
    public TriExterne(String path, String[] colonnes, String typesCsvOuNull) throws IOException {
        this.path = path;
        this.fichierInit = new BufferedReader(new FileReader(path));
        this.entete = fichierInit.readLine().split(";");

        this.runDir = createRunDir();
        this.outputDir = Paths.get("output");

        // Résoudre indices
        int[] indices = new int[colonnes.length];
        for (int i = 0; i < colonnes.length; i++) {
            indices[i] = -1;
            for (int j = 0; j < entete.length; j++) {
                if (entete[j].equals(colonnes[i])) {
                    indices[i] = j;
                    break;
                }
            }
            if (indices[i] == -1)
                throw new RuntimeException("Colonne inconnue dans l'entête: " + colonnes[i]);
        }

        // Types: si null/vides => AUTO, sinon parse "NUM;TXT;..."
        Comparateur.Type[] types;
        if (typesCsvOuNull == null || typesCsvOuNull.trim().isEmpty()) {
            types = new Comparateur.Type[colonnes.length];
            Arrays.fill(types, Comparateur.Type.AUTO);
        } else {
            String[] ts = typesCsvOuNull.split(";");
            if (ts.length != colonnes.length)
                throw new IllegalArgumentException("Le nombre de types doit égaler le nombre de colonnes.");
            types = new Comparateur.Type[ts.length];
            for (int i = 0; i < ts.length; i++) {
                String t = ts[i].trim().toUpperCase();
                if (t.equals("NUM"))
                    types[i] = Comparateur.Type.NUM;
                else if (t.equals("TXT"))
                    types[i] = Comparateur.Type.TXT;
                else if (t.equals("AUTO") || t.isEmpty())
                    types[i] = Comparateur.Type.AUTO;
                else
                    throw new IllegalArgumentException("Type inconnu: " + ts[i] + " (attendu NUM/TXT/AUTO)");
            }
        }

        this.comparateur = new Comparateur(indices, types);
        this.cache = new String[M][entete.length];
    }

    /**
     * Tri le fichier CSV en utilisant un tri externe (toutes les passes).
     *
     * Étapes :
     * - Génère les fragments initiaux (niveau 0).
     * - Puis fusionne par groupes de (M-1) fragments, niveau par niveau,
     * jusqu'à obtenir un seul fragment (globalement trié).
     */
    public void trier() throws IOException {
        int total = creerFragmentsInitiaux(); // crée fragment_0_0.csv, fragment_0_1.csv, ...
        int nFragments = (total + M - 1) / M; // ceil(total / M)

        System.out.println("[Tri externe] Niveau 0 généré.");
        System.out.println(" - N-uplets lus   : " + total);
        System.out.println(" - Taille M       : " + M);
        System.out.println(" - #fragments     : " + nFragments);

        if (nFragments == 0)
            return; // rien à faire

        int niveau = 0;
        int courant = nFragments;

        // Passes successives de fusion jusqu'à 1 fragment
        while (courant > 1) {
            int produits = 0;

            // Fusion par groupes de (M-1) fragments
            for (int debut = 0; debut < courant; debut += (M - 1)) {
                int nb = Math.min(M - 1, courant - debut);
                // fragment_{niveau}_{debut .. debut+nb-1} -> fragment_{niveau+1}_{produits}
                fusionGroupe(niveau, debut, nb, produits);
                produits++;
            }

            System.out.println("[Fusion] Niveau " + (niveau + 1) + " : " + produits + " fragment(s) créé(s).");

            // (Optionnel) Nettoyage : supprimer les fragments du niveau courant
            supprimerFragmentsDuNiveau(niveau, courant);

            // Passer au niveau suivant
            niveau++;
            courant = produits;

        }

        System.out.println("[Terminé] Fichier trié : " + nomDeFragment(niveau, 0));
        ensureOutputDir();
        Path finalFragment = Paths.get(nomDeFragment(niveau, 0));
        Path finalOutput = outputDir.resolve("sorted.csv");
        Files.copy(finalFragment, finalOutput, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[Sortie] Fichier final : " + finalOutput.toString());
    }

    /**
     * Supprime les fragments d'un niveau avec journalisation et quelques
     * tentatives.
     * 
     * @param niveau le niveau des fragments à supprimer
     * @param count  le nombre de fragments à supprimer
     */
    private void supprimerFragmentsDuNiveau(int niveau, int count) {
        for (int i = 0; i < count; i++) {
            File temporaryFiles = new File(nomDeFragment(niveau, i));
            boolean deleted = false;
            for (int attempt = 0; attempt < 3 && temporaryFiles.exists(); attempt++) {
                deleted = temporaryFiles.delete();
                if (deleted)
                    break;
                // légère pause + GC pour éviter un verrouillage transitoire sous Windows
                System.gc();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ignore) {
                }
            }
            if (!deleted && temporaryFiles.exists()) {
                System.out.println("[WARN] Non supprimé: " + temporaryFiles.getAbsolutePath()
                        + " (exists=" + temporaryFiles.exists()
                        + ", canWrite=" + temporaryFiles.canWrite() + ")");
            }
        }
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
        String mot = ""; // TODO: StringBuilder serait plus efficace
        for (int i = 0; i < valeurs.length; i++) {
            mot += valeurs[i];
            if (i != valeurs.length - 1) {
                mot += ";";
            }
        }
        mot += "\n";
        fw.append(mot);
    }

    /**
     * sauvegarde le contenu du cache[0,taille-1] dans un fichier CSV
     * (avec taille <= M)
     * 
     * Etapes:
     * -
     * 
     * @param path   le chemin du fichier CSV
     * @param entete la liste des noms de colonnes du fichier CSV
     * @param taille le nombre d'éléments à sauvegarder (<= M)
     * @throws IOException              si une erreur d'écriture dans le fichier se
     *                                  produit
     * @throws IllegalArgumentException si taille n'est pas dans l'intervalle [0,M]
     */
    private void sauvegardeCache(String path, String[] entete, int taille) throws IOException {

        if (taille < 0 || taille > M) {
            throw new IllegalArgumentException("taille doit etre dans l'intervalle [0, M]");
        }
        try (FileWriter fw = new FileWriter(path)) {
            // Ecrire Entete
            ecrireLigneCSV(fw, entete);

            // Ecrire premeir n(taille) ligne
            for (int i = 0; i < taille; i++) {
                if (cache[i] != null) {
                    ecrireLigneCSV(fw, cache[i]);
                }
            }
        }
    }

    /**
     * Crée les fragments initiaux triés à partir du fichier CSV initial.
     * Chaque fragment est un fichier CSV contenant au plus M lignes triées.
     * 
     * @return le nombre TOTAL de n-uplets lus (hors entête)
     * @throws IOException si une erreur de lecture ou d'écriture se produit
     */
    private int creerFragmentsInitiaux() throws IOException {
        String ligne;
        int totalTuples = 0; // demandé par l'énoncé Q9
        int fill = 0; // nb d'éléments actuellement dans le cache
        int numFragment = 0; // fragment_0_{numFragment}.csv

        try {
            while ((ligne = this.fichierInit.readLine()) != null) {
                cache[fill] = nupletDepuis(ligne);
                fill++;
                totalTuples++;

                if (fill == M) {
                    trierCache(fill);
                    String fragName = nomDeFragment(0, numFragment++);
                    sauvegardeCache(fragName, entete, fill);
                    fill = 0; // on "vide" le cache logiquement
                }
            }

            // Dernier fragment partiel
            if (fill > 0) {
                trierCache(fill);
                String fragName = nomDeFragment(0, numFragment++);
                sauvegardeCache(fragName, entete, fill);
            }
        } finally {
            this.fichierInit.close(); // on a fini de lire le fichier initial
        }

        return totalTuples;

    }

    /**
     * Fusionne un groupe de fragments:
     * fragment_{niveau}_{debut} .. fragment_{niveau}_{debut+nombre-1}
     * en un seul fragment:
     * fragment_{niveau+1}_{outIndex}
     *
     * Hypothèse locale : nombre <= M-1 (cache[M-1] sert de tampon).
     *
     * @param niveau   le niveau des fragments d'entrée
     * @param debut    l'indice du premier fragment d'entrée à fusionner
     * @param nombre   le nombre de fragments à fusionner (<= M-1)
     * @param outIndex l'indice du fragment de sortie au niveau (niveau+1)
     * @return 1 si au moins un n-uplet a été écrit, 0 sinon
     * @throws IOException en cas d'erreur d'E/S
     */
    private int fusionGroupe(int niveau, int debut, int nombre, int outIndex) throws IOException {
        if (nombre <= 0)
            return 0;
        if (nombre > M - 1)
            throw new IllegalArgumentException("nombre doit être ≤ M-1");

        // 1) Ouvrir les fragments d'entrée
        BufferedReader[] brs = new BufferedReader[nombre];
        try {
            for (int j = 0; j < nombre; j++) {
                String inName = nomDeFragment(niveau, debut + j);
                brs[j] = new BufferedReader(new FileReader(inName));
            }

            // 2) Préparer la sortie
            String outName = nomDeFragment(niveau + 1, outIndex);
            try (FileWriter fw = new FileWriter(outName)) {

                // Écrire l’entête en sortie
                ecrireLigneCSV(fw, entete);

                // 3) Charger le premier n-uplet de chaque entrée dans cache[0..nombre-1]
                for (int j = 0; j < nombre; j++) {
                    brs[j].readLine(); // sauter l’entête du fragment d’entrée
                    String ligne = brs[j].readLine();
                    cache[j] = (ligne == null) ? null : nupletDepuis(ligne);
                }

                // 4) Boucle de fusion
                boolean aEcrit = false;
                while (true) {
                    int iMin = -1;
                    for (int j = 0; j < nombre; j++) {
                        if (cache[j] == null)
                            continue;
                        if (iMin == -1 || comparateur.compare(cache[j], cache[iMin]) < 0) {
                            iMin = j;
                        }
                    }
                    if (iMin == -1)
                        break; // toutes les entrées sont épuisées

                    // Utiliser la dernière case du cache (index M-1) comme tampon
                    cache[M - 1] = cache[iMin];

                    // Écrire le n-uplet minimal
                    ecrireLigneCSV(fw, cache[M - 1]);
                    aEcrit = true;

                    // Avancer dans la source du minimum
                    String ligne = brs[iMin].readLine();
                    cache[iMin] = (ligne == null) ? null : nupletDepuis(ligne);
                }

                return aEcrit ? 1 : 0;
            }
        } finally {
            // 5) Fermer les entrées et nettoyer le cache utilisé
            for (int j = 0; j < nombre; j++) {
                if (brs[j] != null) {
                    try {
                        brs[j].close();
                    } catch (IOException ignore) {
                    }
                }
            }
            for (int j = 0; j < Math.min(nombre + 1, M); j++)
                cache[j] = null;
        }
    }

    private static Path createRunDir() {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path dir = Paths.get("tmp", "fragments", "run_" + stamp);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier run: " + dir, e);
        }
        return dir;
    }

    private static void ensureOutputDir() {
        try {
            Files.createDirectories(Paths.get("output"));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier output/", e);
        }
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
    private String nomDeFragment(int niveau, int numero) {
        return runDir.resolve(String.format("fragment_%s_%s.csv", niveau, numero)).toString();
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

        // === test du comparateur ===
        // testComparateur();

        // exécution du tri externe
        if (args.length < 2) {
            System.out.println("Le premier argument est un fichier CSV");
            System.out.println("Le deuxième argument est les colonnes pour le tri");
            System.out.println(
                    "Le troisième argument (optionnel) est les types (NUM;TXT;...) par colonne Sinon automatique");
            return;
        }

        // mesurer le temps d'exécution
        long t1 = System.currentTimeMillis();

        // lancer le tri externe
        String fichier = args[0];
        String[] cols = args[1].split(";");
        String typesCsv = (args.length >= 3) ? args[2] : null; // null => AUTO

        TriExterne algo = new TriExterne(fichier, cols, typesCsv);

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
