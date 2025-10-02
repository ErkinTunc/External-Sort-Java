import java.io.*;
import java.util.*;

// compare deux nuplets par rapport à l'ordre lexicographique d'un sous-ensemble de ces indices
// attention, l'ordre des indices compte et il n'est pas forcément croissant
class Comparateur implements Comparator<String[]> {

    public final int[] indices;
    
    public Comparateur(int[] indices) {
        this.indices = indices;
    }

    public int compare(String[] t1, String[] t2) {
        return 0;
    }
}
