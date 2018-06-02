import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

import java.util.ArrayList;
import java.util.List;

public class Bucket implements IBucket {

    private final SparseVector clusterVector;
    private final List<Document> documents;
    private DocumentClusterer subClusterer;

    public Bucket(final SparseVector clusterVector) {
        this.clusterVector = clusterVector;
        documents = new ArrayList<>();
    }

    public void initialiseSecondLevel(ConfigurationContainer config, final int clusterLevel) {

        Document[] docs = documents.toArray(new Document[0]);
        if (docs.length >= config.numberOf2ndLevelBuckets)
            subClusterer = new DocumentClusterer(docs, config.secondLevelSimilarityThreshold, config.secondLevelReclusterThreshold, config.numberOf2ndLevelBuckets, clusterLevel);
    }

    public double getSimilarityFor(final Document d) {

        SparseVector dv = d.getFeatureVector();
        return clusterVector.dot(d.getFeatureVector()) / (clusterVector.norm(Vector.Norm.Two) * dv.norm(Vector.Norm.Two));
    }

    @Override
    public void addDocument(final Document d) {
        documents.add(d);

        if (subClusterer != null)
            subClusterer.addDocument(d);
    }

    @Override
    public List<Document> getDocuments() {
        return documents;
    }

    @Override
    public int size() {
        return documents.size();
    }
}
