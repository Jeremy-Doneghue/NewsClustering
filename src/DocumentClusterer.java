import no.uib.cipr.matrix.sparse.SparseVector;
import sun.plugin2.message.BestJREAvailableMessage;

import java.util.*;


public class DocumentClusterer {

    private Bucket[] buckets;
    private FeatureSpace featureSpace;
    private RejectBucket bin;

    private final double similarityThreshold;
    private final int reclusterThreshold;
    private final int numberOfBuckets;

    private final SlidingWindow<Document> documentSlidingWindow;

    public DocumentClusterer(final Document[] documents, final double similarityThreshold, final int reclusterThreshold, final int numberOfBuckets) {

        this.reclusterThreshold = reclusterThreshold;
        this.similarityThreshold = similarityThreshold;
        this.numberOfBuckets = numberOfBuckets;

        documentSlidingWindow = new SlidingWindow<>(documents);
        cluster();
    }

    private void putDocInBucket(final Document d) {

        IBucket bestMatch = bin;
        double bestValue = 0.0;
        for (Bucket b : buckets) {
            double similarity = b.getSimilarityFor(d);
            if (similarity > bestValue && similarity > similarityThreshold) {
                bestMatch = b;
                bestValue = similarity;
            }
        }

        bestMatch.addDocument(d);

        if (bin.isFull()) {
            cluster();
        }
    }

    public void addDocument(final Document d) {

        d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        documentSlidingWindow.add(d);
        putDocInBucket(d);
    }

    private void cluster() {

        // Append window and bin document arrays
        Document[] docs = new Document[documentSlidingWindow.size() + bin.size()];
        System.arraycopy(documentSlidingWindow.getDocuments(), 0, docs, 0, documentSlidingWindow.size());
        System.arraycopy(bin.getDocuments(), 0, docs, documentSlidingWindow.size(), bin.size());
        featureSpace = generateFeatureSpace(docs);

        for (Document d : docs) {
            d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        }
        // TODO: do kmeans

        // create buckets
        buckets = new Bucket[numberOfBuckets];
        bin = new RejectBucket(reclusterThreshold);
    }

    private SparseVector generateFeatureVectorFor(final FeatureSpace s, final Document d) {

        SparseVector featureVector = new SparseVector(s.table.size());
        for (String w : d.getWords()) {
            if (s.table.containsKey(w)) {
                FeatureSpace.IndexIDF inst = s.table.get(w);
                featureVector.add(inst.index, d.getTFFor(w) * inst.idf);
            }
        }

        return featureVector;
    }

    private FeatureSpace generateFeatureSpace(final Document[] docs) {

        FeatureSpace featureSpace = new FeatureSpace();

        int counter = 0;
        for (Document d : docs) {
            for (String word : d.getWords()) {
                if (!featureSpace.table.containsKey(word)) {
                    featureSpace.addFeature(word, counter, idfOfWord(word, docs));
                    counter++;
                }
            }
        }
        return featureSpace;
    }

    public double idfOfWord(final String word, final Document[] docs) {

        double occurrenceCounter = (double)Arrays.stream(docs).filter(n -> n.containsWord(word)).count() + 1.0;
        double N = (double) docs.length;

        return Math.log(N / occurrenceCounter);
    }
}
