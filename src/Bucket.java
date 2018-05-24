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

    public void initialiseSecondLevel(final double similarityThreshold, final int reclusterThreshold, final int numberOfBuckets, final int clusterLevel) {

        subClusterer = new DocumentClusterer((Document[]) documents.toArray(), similarityThreshold, reclusterThreshold, numberOfBuckets, clusterLevel);
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
