import java.io.*;
import java.util.*;

/**
 * Le comparateur permet de définir un ordre sur des n-uplets en fonction d’une
 * sous-liste de leurs colonnes.
 * 
 * Exemple : si indices = [1, 3], alors on compare d'abord la colonne 1, puis
 * la colonne 3 si nécessaire.
 * 
 * Attention : l'ordre des indices donné par l'utilisateur compte et n'est pas
 * forcément croissant.
 */
class Comparateur implements Comparator<String[]> {

    public final int[] indices;

    /**
     * Constructeur du comparateur
     * 
     * @param indices les indices des colonnes sur lesquelles faire la comparaison
     */
    public Comparateur(int[] indices) {
        this.indices = indices;
    }

    /**
     * Compare deux n-uplets en fonction des colonnes spécifiées dans indices
     * - l’ordre lexicographique = dictionary order (a-z)
     * 
     * @param t1 le premier n-uplet
     * @param t2 le second n-uplet
     * 
     * @return un entier négatif, zéro ou positif si t1 est respectivement
     *         inférieur, égal ou supérieur à t2
     */
    public int compare(String[] t1, String[] t2) {
        for (int index : indices) {
            int compare = t1[index].compareTo(t2[index]);
            if (compare != 0) {
                return compare; // dès qu'on trouve une différence on la retourne
            }
        }
        return 0; // toutes les colonnes testées sont égales
    }
}
