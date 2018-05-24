import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;
import weka.core.Instance;
import weka.core.SparseInstance;

import javax.print.Doc;
import java.util.*;

public class Document {

    private final Map<String, Integer> bagOfWords;
    private final double highestWordCount;

    private SparseVector featureVector;

    public Document(final Map<String, Integer> bow) {

        this.bagOfWords = bow;

        Iterator<Map.Entry<String,Integer>> it = bagOfWords.entrySet().iterator();
        int max = 0;
        while (it.hasNext()) {
            int curr = it.next().getValue();
            if (curr > max)
                max = curr;
        }
        highestWordCount = (double)max;
    }

    public void setFeatureVector(final SparseVector v) { // Takes sparsevector
        this.featureVector = v;
    }

    public SparseVector getFeatureVector() {
        return this.featureVector;
    }

    // Return the feature vector as a weka instance for use in simplekmeans
    public Instance getInstance() {

        SparseInstance si = new SparseInstance(featureVector.size());
        for (VectorEntry e : featureVector) {
            si.setValueSparse(e.index(), e.get());
        }

        return si;
    }

    public Set<String> getWords() {
        return bagOfWords.keySet();
    }

    public double getTFFor(final String word) {
        int wordFreq = bagOfWords.getOrDefault(word, 0);
        return 0.5 + (0.5 * (wordFreq / highestWordCount));
    }

    public boolean containsWord(final String word) { return bagOfWords.containsKey(word); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Document)) return false;
        Document otherDocument = (Document)obj;
        return this.bagOfWords.equals(otherDocument.bagOfWords);
    }

    @Override
    public int hashCode() {
        return bagOfWords.hashCode();
    }
}