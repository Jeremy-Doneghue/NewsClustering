import moa.cluster.Clustering;
import moa.clusterers.AbstractClusterer;
import moa.core.Measurement;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException;
import sun.plugin2.message.BestJREAvailableMessage;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class DocumentClusterer extends AbstractClusterer {

    private final Bucket[] buckets;
    private final FeatureSpace featureSpace;
    private final SlidingWindow<Document> documentSlidingWindow;
    private final RejectBucket bin;


    private final double similarityThreshold;
    private final int reclusterThreshold;
    private final int numberOfBuckets;
    private final int clusterLevel;

    private  ConfigurationContainer config;
    private boolean documentsAddedSinceLastCluster = false;

    public DocumentClusterer(final Document[] documents, ConfigurationContainer config, final int clusterLevel) {
        this.reclusterThreshold = config.reclusterThreshold;
        this.similarityThreshold = config.similarityThreshold;
        this.numberOfBuckets = config.numberOfBuckets;
        this.clusterLevel = clusterLevel;

        this.config = config;

        documentSlidingWindow = new SlidingWindow<Document>(documents);
        buckets = new Bucket[numberOfBuckets];
        bin = new RejectBucket(reclusterThreshold);
        featureSpace = new FeatureSpace();


        if (documents.length > numberOfBuckets) {
            cluster();
        }
    }

    public DocumentClusterer(final Document[] documents, final double similarityThreshold, final int reclusterThreshold, final int numberOfBuckets, final int clusterLevel) {

        this.reclusterThreshold = reclusterThreshold;
        this.similarityThreshold = similarityThreshold;
        this.numberOfBuckets = numberOfBuckets;
        this.clusterLevel = clusterLevel;

        documentSlidingWindow = new SlidingWindow<Document>(documents);
        buckets = new Bucket[numberOfBuckets];
        bin = new RejectBucket(reclusterThreshold);
        featureSpace = new FeatureSpace();

        if (documents.length > numberOfBuckets) {
            cluster();
        }
    }

    private int putDocInBucket(final Document d) {

        IBucket bestMatch = bin;
        int index = -1;
        double bestValue = 0.0;
        for (int i = 0; i < numberOfBuckets; i++) {
            Bucket b = buckets[i];
            if (b == null) {
                System.out.println("Null bucket on cluster-level " + clusterLevel);
                continue;
            }
            double similarity = b.getSimilarityFor(d);
            if (similarity > bestValue && similarity > similarityThreshold) {
                bestMatch = b;
                bestValue = similarity;
                index = i;
            }
        }

        bestMatch.addDocument(d);

        if (bin.isFull() && documentsAddedSinceLastCluster) {
            System.out.println("Re-cluster triggered at level " + this.clusterLevel);
            cluster();
        }

        return index;
    }

    public void addDocument(final Document d) {

        documentsAddedSinceLastCluster = true;
        d.setFeatureVector(generateFeatureVectorFor(d, featureSpace));
        documentSlidingWindow.add(d);
        putDocInBucket(d);
    }

    private void cluster() {

        documentsAddedSinceLastCluster = false;

        // Append window and bin document arrays
        Set<Document> docset = new HashSet<>(documentSlidingWindow);
        docset.addAll(bin.getDocuments());

        if (docset.size() < numberOfBuckets)
            throw new RuntimeException("docset.size() < numberOfBuckets");

        ArrayList<Document> docs = new ArrayList<>(docset);
        featureSpace.recreate(docs);

        for (Document d : docs) {
            d.setFeatureVector(generateFeatureVectorFor(d, featureSpace));
        }

        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setDebug(true);

        ArrayList<Attribute> atts = new ArrayList<>(featureSpace.size());
        for (int i = 0; i < featureSpace.size(); i++)
            atts.add(new Attribute("a" + i));

        Instances instances = new Instances("instance", atts, documentSlidingWindow.size() + bin.size());
        for (Document d : docs)
            instances.add(d.getInstance());

        try {
            clusterer.setNumClusters(numberOfBuckets);
            clusterer.buildClusterer(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create buckets
        Instances centroids = clusterer.getClusterCentroids();
        for (int i = 0; i < centroids.numInstances(); i++) {
            SparseVector v = new SparseVector(featureSpace.size());

            Instance inst = centroids.get(i);
            for (int j = 0; j < inst.numAttributes(); j++) {
                double value = inst.value(j);
                if (value != 0.0)
                    v.set(j, value);
            }
            buckets[i] = new Bucket(v, featureSpace);
        }


        int[] documentDistribution = new int[numberOfBuckets + 1];
        for (Document d : docs) {
            int dest = putDocInBucket(d);
            documentDistribution[dest + 1]++;
        }
        System.out.println(this.clusterLevel + ": " + Arrays.toString(documentDistribution));

        if (clusterLevel > 0) {
            for (Bucket b : buckets)
                b.initialiseSecondLevel(config, clusterLevel - 1);
        }
    }

    private SparseVector generateFeatureVectorFor(final Document d, final FeatureSpace s) {

        SparseVector featureVector = new SparseVector(s.size());

        for (String w : d.getWords()) {
            if (s.containsWord(w)) {
                FeatureSpace.IndexIDF inst = s.getForWord(w);
                featureVector.add(inst.index, d.getTFFor(w) * inst.idf);
            }
        }

        return featureVector;
    }

    @Override
    public void resetLearningImpl() {

    }

    @Override
    public void trainOnInstanceImpl(com.yahoo.labs.samoa.instances.Instance instance) {

    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[0];
    }

    @Override
    public void getModelDescription(StringBuilder stringBuilder, int i) {

    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    @Override
    public double[] getVotesForInstance(com.yahoo.labs.samoa.instances.Instance instance) {
        return new double[0];
    }

    @Override
    public Clustering getClusteringResult() {
        return null;
    }
}
