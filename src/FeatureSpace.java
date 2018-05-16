import java.util.Hashtable;
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

    public void addFeature(final String word, final int index, final double idf) {
        table.put(word, new IndexIDF(index, idf));
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
