import no.uib.cipr.matrix.sparse.SparseVector;
import sun.jvm.hotspot.oops.Instance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Document {

    private HashMap<String, Integer> bagOfWords;
    private double highestFrequency;

    SparseVector fv;

    public Document(HashMap<String, Integer> bow) {

        this.bagOfWords = bow;

        Iterator<Map.Entry<String,Integer>> it = bagOfWords.entrySet().iterator();
        double max = 0;
        while (it.hasNext()) {
            int curr = it.next().getValue();
            if (curr > max)
                max = (double)curr;
        }
        highestFrequency = max;
    }

    public void setFeatureVector(SparseVector v) { // Takes sparsevector
        this.fv = v;
    }

    public SparseVector getFeatureVector() {
        return this.fv;
    }

    // Return the feature vector as a weka instance for use in simplekmeans
    public Instance getInstance() {

        return null;
    }

    public Set<String> getWords() {
        return bagOfWords.keySet();
    }

    public double getTFFor(String word) {
        int wordFreq = bagOfWords.getOrDefault(word, 0);
        return 0.5 + (0.5 * (wordFreq / highestFrequency));
    }

    public boolean containsWord(String word) { return bagOfWords.containsKey(word); }

}