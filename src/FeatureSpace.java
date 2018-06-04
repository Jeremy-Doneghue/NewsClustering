import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class FeatureSpace {

    private final Map<String, IndexIDF> table;

    public class IndexIDF {

        public final int index;
        public final double idf;

        public IndexIDF(final int index, final double idf) {
            this.index = index;
            this.idf = idf;
        }
    }

    public Map<String, IndexIDF> getTable() { return table; }

    public FeatureSpace() {
        table = new Hashtable<>();
    }

    private void addFeature(final String word, final int index, final double idf) {
        table.put(word, new IndexIDF(index, idf));
    }

    public void addFeatures(final List<Document> docs) {
        int counter = 0;
        for (final Document d : docs) {
            for (final String word : d.getWords()) {
                if (!containsWord(word)) {
                    addFeature(word, counter, idfOfWord(word, docs));
                    counter++;
                }
            }
        }
    }

    public void recreate(final List<Document> docs) {
        clear();
        addFeatures(docs);
    }

    private double idfOfWord(final String word, final List<Document> docs) {

        double occurrenceCounter = (double) docs.stream().filter(n -> n.containsWord(word)).count() + 1.0;
        double N = (double) docs.size();

        return Math.log(N / occurrenceCounter);
    }

    public void clear() {
        table.clear();
    }

    public int size() {
        return table.size();
    }

    public boolean containsWord(String word) {
        return table.containsKey(word);
    }

    public IndexIDF getForWord(String word) {
        return table.get(word);
    }
}
