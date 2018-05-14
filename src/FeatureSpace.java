import java.util.Hashtable;

public class FeatureSpace {

    public final Hashtable<String, IndexIDF> table;

    public class IndexIDF {

        public final int index;
        public final double idf;

        public IndexIDF(final int index, final double idf) {
            this.index = index;
            this.idf = idf;
        }
    }

    public FeatureSpace() {
        table = new Hashtable<>();
    }

    public void addFeature(final String word, final int index, final double idf) {
        table.put(word, new IndexIDF(index, idf));
    }
}
