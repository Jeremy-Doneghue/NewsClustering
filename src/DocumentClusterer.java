import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.SparseVector;
import weka.core.*;

import java.util.*;


public class DocumentClusterer {

    Bucket[] buckets;
    int numBuckets = 4;
    RejectBin bin;
    double similarityThresh;
    int reclusterThreshold;

    FeatureSpace featureSpace;

    SlidingWindow docWindow;

    public DocumentClusterer(Document[] documents, double similarityThresh, int reclusterThreshold) {

        this.reclusterThreshold = reclusterThreshold;
        this.similarityThresh = similarityThresh;
        docWindow = new SlidingWindow(documents);

        cluster();
    }

    private void putDocInBucket(Document d) {

        IBucket bestMatch = bin;
        double bestValue = 0.0;
        for (Bucket b : buckets)
            // Compare document.vector using bucket.getsimilarity
            // if > bestValue AND > simthresh
                // BestMatch = bucket
                // Bestvalue = that

        bestMatch.addDocument(d);

        if (bin.isFull()) {
            //cluster();
        }
    }

    public void addDocument(Document d) {

        d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        docWindow.addDocument(d);
        putDocInBucket(d);
    }

    private void cluster() {

        // Append window and bin document arrays
        Document[] docs = new Document[docWindow.getSize() + bin.getSize()];
        System.arraycopy(docWindow.getDocuments(), 0, docs, 0, docWindow.getSize());
        System.arraycopy(bin.getDocuments(), 0, docs, docWindow.getSize(), bin.getSize());
        featureSpace = generateFeatureSpace(docs);

        for (Document d : docs) {
            d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        }
        // TODO: do kmeans

        // create buckets
        buckets = new Bucket[numBuckets];
    }

    private SparseVector generateFeatureVectorFor(FeatureSpace s, Document d) {

        SparseVector out = new SparseVector(s.table.size());
        for (String w : d.getWords()) {
            if (s.table.containsKey(w)) {
                FeatureSpace.IndexIDF inst = s.table.get(w);
                out.add(inst.index, inst.idf);
            }
        }

        return out;
    }

    private FeatureSpace generateFeatureSpace(Document[] docs) {

        FeatureSpace out = new FeatureSpace();

        int counter = 0;
        for (Document d : docs) {
            for (String word : d.getWords()) {
                if (!out.table.containsKey(word)) {
                    out.addInstance(word, counter, idfOfWord(word, docs));
                    counter++;
                }
            }
        }
        return out;
    }

    public double idfOfWord(String word, Document[] docs) {

        double occurrenceCounter = (double)Arrays.stream(docs).filter(n -> n.containsWord(word)).count() + 1.0;
        double N = (double) docs.length;

        return Math.log(N / occurrenceCounter);
    }
}
