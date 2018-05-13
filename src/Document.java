import sun.jvm.hotspot.oops.Instance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Document {

    private HashMap<String, Integer> bagOfWords;
    private double highestFrequency;

    // Feature vector (MTJ Sparse Vector)

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

    public void setFeatureVector() { // Takes sparsevector

    }

    //public SparseVector getVector() {
        //return this.vector
    //}

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