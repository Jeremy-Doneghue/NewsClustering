import java.util.Hashtable;

public class FeatureSpace {

    public final Hashtable<String, IndexIDF> table;

    public class IndexIDF {

        public int index;
        public double idf;

        public IndexIDF(int index, double idf) {
            this.index = index;
            this.idf = idf;
        }
    }

    public FeatureSpace() {
        table = new Hashtable<>();
    }

    public void addInstance(String word, int index, double idf) {
        table.put(word, new IndexIDF(index, idf));
    }
}
