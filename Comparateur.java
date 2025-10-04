
/**
 * Comparateur.java 
 * 
 * Comparateur de n-uplets (tableaux de chaînes) selon une liste d'indices de colonnes.
 * 
 * @author Erkin Tunc BOYA
 * @version 1.5
 * @since   2025-10-01
 */

import java.util.Comparator;

/**
 * Le comparateur définit un ordre sur des n-uplets en fonction d’une
 * sous-liste de leurs colonnes.
 *
 * Règles :
 * - Si les deux valeurs sont numériques (suite de chiffres), comparaison
 * numérique (long).
 * - Sinon, comparaison lexicographique (String.compareTo).
 * - Valeur vide/null considérée plus petite.
 */
class Comparateur implements Comparator<String[]> {

    /** Type de comparaison pour chaque colonne. */
    public enum Type {
        NUM, TXT, AUTO
    }

    /** indices des colonnes à comparer, dans l’ordre de priorité */
    public final int[] indices;

    /** type à utiliser pour chaque colonne (même longueur que indices) */
    public final Type[] types;

    /**
     * Constructeur AUTO : déduit NUM/TXT à la volée pour chaque comparaison.
     * (Garde une compatibilité avec l’ancienne version.)
     */
    public Comparateur(int[] indices) {
        this(indices, remplir(typesAuto(indices.length)));
    }

    /**
     * Constructeur typé : force NUM ou TXT (ou AUTO) par colonne.
     * 
     * @param indices colonnes (ordre de priorité)
     * @param types   types correspondants (même longueur que indices)
     */
    public Comparateur(int[] indices, Type[] types) {
        if (indices == null || types == null || indices.length != types.length) {
            throw new IllegalArgumentException("indices et types doivent avoir la même longueur");
        }
        this.indices = indices;
        this.types = types.clone();
    }

    @Override
    public int compare(String[] t1, String[] t2) {
        for (int k = 0; k < indices.length; k++) {
            int index = indices[k];
            Type type = types[k];

            String a = (t1[index] == null) ? "" : t1[index].trim();
            String b = (t2[index] == null) ? "" : t2[index].trim();

            // Gestion des vides/null : le vide est considéré comme plus petit
            if (a.isEmpty() && b.isEmpty())
                continue;
            if (a.isEmpty())
                return -1;
            if (b.isEmpty())
                return 1;

            int c;
            switch (type) {
                case NUM:
                    c = compareNumeriqueOuLex(a, b);
                    break;
                case TXT:
                    c = a.compareTo(b);
                    break;
                default: // AUTO
                    if (estNumerique(a) && estNumerique(b))
                        c = compareNumerique(a, b);
                    else
                        c = a.compareTo(b);
                    break;
            }
            if (c != 0)
                return c; // différence trouvée -> on renvoie
        }
        return 0; // toutes les colonnes testées sont égales
    }

    // --- outils ---

    /** Compare numériquement (long). Suppose a et b numériques. */
    private static int compareNumerique(String a, String b) {
        long la = Long.parseLong(a);
        long lb = Long.parseLong(b);
        return Long.compare(la, lb);
    }

    /** Si les deux valeurs sont numériques => numérique, sinon lexicographique. */
    private static int compareNumeriqueOuLex(String a, String b) {
        if (estNumerique(a) && estNumerique(b))
            return compareNumerique(a, b);
        return a.compareTo(b); // fallback
    }

    /**
     * Retourne vrai si la chaîne ne contient que des chiffres 0-9 (pas de signe,
     * pas d’espace).
     */
    private static boolean estNumerique(String s) {
        if (s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < '0' || ch > '9')
                return false;
        }
        return true;
    }

    // --- helpers pour le constructeur AUTO ---
    private static Type[] typesAuto(int n) {
        Type[] arr = new Type[n];
        for (int i = 0; i < n; i++)
            arr[i] = Type.AUTO;
        return arr;
    }

    // Petit helper pour le constructeur AUTO
    private static Type[] remplir(Type[] t) {
        return t;
    }
}
