import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

import java.util.LinkedList;

public class Bucket implements IBucket{

    private SparseVector clusterVector;

    private LinkedList<Document> docs;

    public Bucket(SparseVector clusterVector) {
        this.clusterVector = clusterVector;
        docs = new LinkedList<>();
    }

    public double compareVector(Document d) {

        SparseVector dv = d.getFeatureVector();
        return clusterVector.dot(d.getFeatureVector()) / (clusterVector.norm(Vector.Norm.Two) * dv.norm(Vector.Norm.Two));
    }
    
    public void addDocument(Document d) {
        docs.add(d);
    }
}
