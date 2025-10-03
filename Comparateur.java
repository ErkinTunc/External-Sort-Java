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

    public final int[] indices; // en-tete | indices des colonnes à comparer, dans l’ordre de priorité

    /**
     * Constructeur
     * 
     * @param indices ordre de priorité des colonnes sur lesquelles comparer
     */
    public Comparateur(int[] indices) {
        this.indices = indices;
    }

    /**
     * Compare deux n-uplets selon les colonnes spécifiées.
     * 
     * @return négatif si t1 < t2, 0 si égalité, positif si t1 > t2.
     */
    @Override
    public int compare(String[] t1, String[] t2) {
        for (int index : indices) {
            String a = (t1[index] == null) ? "" : t1[index].trim();
            String b = (t2[index] == null) ? "" : t2[index].trim();

            // Gestion des vides/null : le vide est considéré comme plus petit
            if (a.isEmpty() && b.isEmpty())
                continue;
            if (a.isEmpty())
                return -1;
            if (b.isEmpty())
                return 1;

            // Si les deux valeurs sont numériques, on compare en numérique
            if (estNumerique(a) && estNumerique(b)) {
                long la = Long.parseLong(a);
                long lb = Long.parseLong(b);
                int c = Long.compare(la, lb);
                if (c != 0)
                    return c; // différence trouvée
                continue; // égalité -> on passe à l’indice suivant
            }

            // Sinon, comparaison lexicographique standard
            int c = a.compareTo(b);
            if (c != 0)
                return c; // différence trouvée
            // égalité -> on passe à l’indice suivant
        }
        return 0; // toutes les colonnes testées sont égales
    }

    /**
     * Retourne vrai si la chaîne ne contient que des chiffres 0-9 (pas de signe,
     * séparateur, etc.)
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
}
