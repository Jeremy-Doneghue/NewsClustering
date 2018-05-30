import no.uib.cipr.matrix.sparse.SparseVector;
import sun.plugin2.message.BestJREAvailableMessage;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import javax.print.Doc;
import java.util.*;


public class DocumentClusterer {

    protected Bucket[] buckets;
    protected FeatureSpace featureSpace;
    protected RejectBucket bin;

    private final double similarityThreshold;
    private final int reclusterThreshold;
    private final int numberOfBuckets;
    private final int clusterLevel;

    private final SlidingWindow<Document> documentSlidingWindow;

    public DocumentClusterer(final Document[] documents, final double similarityThreshold, final int reclusterThreshold, final int numberOfBuckets, final int clusterLevel) {

        this.reclusterThreshold = reclusterThreshold;
        this.similarityThreshold = similarityThreshold;
        this.numberOfBuckets = numberOfBuckets;
        this.clusterLevel = clusterLevel;

        documentSlidingWindow = new SlidingWindow<Document>(documents);

        cluster();
    }

    protected int putDocInBucket(final Document d) {

        IBucket bestMatch = bin;
        int index = -1;
        double bestValue = 0.0;
        for (int i = 0; i < numberOfBuckets; i++) {
            Bucket b = buckets[i];
            if (b == null)
                System.out.println("Null bucket on cluster-level" + this.clusterLevel);
            double similarity = b.getSimilarityFor(d);
            if (similarity > bestValue && similarity > similarityThreshold) {
                bestMatch = b;
                bestValue = similarity;
                index = i;
            }
        }

        bestMatch.addDocument(d);

        if (bin.isFull()) {
            System.out.println("Re-cluster triggered at level " + this.clusterLevel);
            cluster();
        }

        return index;
    }

    public void addDocument(final Document d) {

        d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        documentSlidingWindow.add(d);
        putDocInBucket(d);
    }

    protected void cluster() {

        // Append window and bin document arrays
        Set<Document> docset = new HashSet<>(documentSlidingWindow);
        int binSize = 0;
        // On the first clustering we have no previous reject bin, so need to handle especially
        if (bin != null) {
            docset.addAll(bin.getDocuments());
            binSize = bin.size();
        }

        ArrayList<Document> docs = new ArrayList<>(docset);
        featureSpace = generateFeatureSpace(docs);

        for (Document d : docs) {
            d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        }

        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setDebug(true);

        ArrayList<Attribute> atts = new ArrayList<>(featureSpace.size());
        for (int i = 0; i < featureSpace.size(); i++)
            atts.add(new Attribute("a" + i));

        Instances instances = new Instances("instance", atts, documentSlidingWindow.size() + binSize);
        for (Document d : docs)
            instances.add(d.getInstance());

        try {
            clusterer.setNumClusters(numberOfBuckets);
            clusterer.buildClusterer(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create buckets
        buckets = new Bucket[numberOfBuckets];
        Instances centroids = clusterer.getClusterCentroids();
        for (int i = 0; i < centroids.numInstances(); i++) {
            SparseVector v = new SparseVector(featureSpace.size());

            Instance inst = centroids.get(i);
            for (int j = 0; j < inst.numAttributes(); j++) {
                double value = inst.value(j);
                if (value != 0.0)
                    v.add(i, value);
            }
            buckets[i] = new Bucket(v);
        }

        bin = new RejectBucket(reclusterThreshold);

        int[] documentDistribution = new int[numberOfBuckets + 1];
        for (Document d : docs) {
            int dest = putDocInBucket(d);
            documentDistribution[dest + 1]++;
        }
        System.out.println("Distribution of documents into bins. Bin 0 is reject bin.");
        System.out.println(Arrays.toString(documentDistribution));

        if (clusterLevel > 0) {
            for (Bucket b : buckets)
                b.initialiseSecondLevel(similarityThreshold, reclusterThreshold, numberOfBuckets, clusterLevel - 1);
        }
    }

    private SparseVector generateFeatureVectorFor(final FeatureSpace s, final Document d) {

        SparseVector featureVector = new SparseVector(s.size());

        for (String w : d.getWords()) {
            if (s.containsWord(w)) {
                FeatureSpace.IndexIDF inst = s.getForWord(w);
                featureVector.add(inst.index, d.getTFFor(w) * inst.idf);
            }
        }

        return featureVector;
    }

    private FeatureSpace generateFeatureSpace(List<Document> docs) {

        FeatureSpace featureSpace = new FeatureSpace();

        int counter = 0;
        for (Document d : docs) {
            for (String word : d.getWords()) {
                if (!featureSpace.containsWord(word)) {
                    featureSpace.addFeature(word, counter, idfOfWord(word, docs));
                    counter++;
                }
            }
        }
        return featureSpace;
    }

    public double idfOfWord(final String word, final List<Document> docs) {

        double occurrenceCounter = (double)docs.stream().filter(n -> n.containsWord(word)).count() + 1.0;
        double N = (double) docs.size();

        return Math.log(N / occurrenceCounter);
    }
}
