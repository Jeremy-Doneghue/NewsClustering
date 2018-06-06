import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;
import sun.security.krb5.Config;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Bucket implements IBucket {

    private final SparseVector clusterVector;
    private final List<Document> documents;
    private final FeatureSpace associatedFeatureSpace;
    private final ConfigurationContainer config;
    private final DocumentClusterer subClusterer;

    public Bucket(final SparseVector clusterVector, final FeatureSpace associatedFeatureSpace) {
        this.clusterVector = clusterVector;
        this.associatedFeatureSpace = associatedFeatureSpace;
        documents = new ArrayList<>();
        subClusterer = null;
        config = null;
    }

    public Bucket(final SparseVector clusterVector, final FeatureSpace associatedFeatureSpace, final ConfigurationContainer config, final int clusterLevel) {
        this.clusterVector = clusterVector;
        this.associatedFeatureSpace = associatedFeatureSpace;
        this.config = config;
        documents = new ArrayList<>();
        subClusterer = new DocumentClusterer(config.secondLevelSimilarityThreshold, config.secondLevelReclusterThreshold, config.numberOf2ndLevelBuckets, config.slidingWindowCapacity, clusterLevel);
    }

    public double getSimilarityFor(final Document d) {

        SparseVector dv = d.getFeatureVector();
        return clusterVector.dot(d.getFeatureVector()) / (clusterVector.norm(Vector.Norm.Two) * dv.norm(Vector.Norm.Two));
    }

    @Override
    public void addDocument(final Document d) {
        documents.add(d);
        if (subClusterer != null) {
            if (!subClusterer.getHasMadeFirstClustering() && documents.size() > config.numberOf2ndLevelBuckets) {
                subClusterer.setupClustering(documents);
            }
            else if (subClusterer.getHasMadeFirstClustering()) {
                subClusterer.addDocument(d);
            }
        }

    }

    @Override
    public List<Document> getDocuments() {
        return documents;
    }

    @Override
    public int size() {
        return documents.size();
    }

    @Override
    public String toString() {
        return getMostImportantWords(5).toString();
    }

    private List<String> getMostImportantWords(final int numWords) {
        if (associatedFeatureSpace == null)
            return new ArrayList<>();

        final Set<Map.Entry<String, FeatureSpace.IndexIDF>> featureEntrySet = associatedFeatureSpace.getTable().entrySet();
        final int numToCollect = Math.min(featureEntrySet.size(), numWords);

        //sorts the words in the feature space by looking up the importance of that word in the bucket's clusterVector
        //then reduces the stream to the desired number of words, or the size of the feature space if the desired number is too big
        //then maps the stream to just contain strings (the most important ones).
        return featureEntrySet.stream().sorted(new Comparator<Map.Entry<String, FeatureSpace.IndexIDF>>() {
            @Override
            public int compare(Map.Entry<String, FeatureSpace.IndexIDF> o1, Map.Entry<String, FeatureSpace.IndexIDF> o2) {
                int index1 = o1.getValue().index;
                Double clusterVal1 = clusterVector.get(index1);
                int index2 = o2.getValue().index;
                Double clusterVal2 = clusterVector.get(index2);

                //we negative so that we get biggest values first
                return -clusterVal1.compareTo(clusterVal2);
            }
        }).limit(numToCollect).map(new Function<Map.Entry<String,FeatureSpace.IndexIDF>, String>() {
            @Override
            public String apply(Map.Entry<String, FeatureSpace.IndexIDF> stringIndexIDFEntry) {
                return stringIndexIDFEntry.getKey();
            }
        }).collect(Collectors.toList());
    }
}
