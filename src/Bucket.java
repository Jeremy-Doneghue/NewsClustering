import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

import java.util.ArrayList;
import java.util.List;

public class Bucket implements IBucket {

    private final SparseVector clusterVector;
    private final List<Document> documents;

    public Bucket(final SparseVector clusterVector) {
        this.clusterVector = clusterVector;
        documents = new ArrayList<>();
    }

    public double getSimilarityFor(final Document d) {

        SparseVector dv = d.getFeatureVector();
        return clusterVector.dot(d.getFeatureVector()) / (clusterVector.norm(Vector.Norm.Two) * dv.norm(Vector.Norm.Two));
    }

    @Override
    public void addDocument(final Document d) {
        documents.add(d);
    }

    @Override
    public Document[] getDocuments() {
        return documents.toArray(new Document[0]);
    }

    @Override
    public int size() {
        return documents.size();
    }
}
