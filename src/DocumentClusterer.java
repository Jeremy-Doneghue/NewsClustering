import com.github.javacliparser.IntOption;
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

    private boolean hasMadeFirstClustering = false;
    private  ConfigurationContainer config;

    public DocumentClusterer(ConfigurationContainer config, final int clusterLevel) {
        //TODO: separate initialisation of the data structure from the first cluster()
//TODO: check capacity is more than the # of buckets
        this.reclusterThreshold = config.reclusterThreshold;
        this.similarityThreshold = config.similarityThreshold;
        this.numberOfBuckets = config.numberOfBuckets;
        this.clusterLevel = clusterLevel;

        this.config = config;

        documentSlidingWindow = new SlidingWindow<Document>(config.slidingWindowCapacity);
        buckets = new Bucket[numberOfBuckets];
        bin = new RejectBucket(reclusterThreshold);
        featureSpace = new FeatureSpace();
    }

    public DocumentClusterer(final double similarityThreshold, final int reclusterThreshold, final int numberOfBuckets, final int slidingWindowCapacity, final int clusterLevel) {

        //TODO: check capacity is more than the # of buckets
        //TODO: add a document sliding window capacity parameter and re-do using moa.Options
        this.reclusterThreshold = reclusterThreshold;
        this.similarityThreshold = similarityThreshold;
        this.numberOfBuckets = numberOfBuckets;
        this.clusterLevel = clusterLevel;

        documentSlidingWindow = new SlidingWindow<Document>(slidingWindowCapacity);
        buckets = new Bucket[numberOfBuckets];
        bin = new RejectBucket(reclusterThreshold);
        featureSpace = new FeatureSpace();
    }

    private int putDocInBucket(final Document d) {

        IBucket bestMatch = bin;
        int index = -1;
        double bestValue = 0.0;
        for (int i = 0; i < numberOfBuckets; i++) {
            Bucket b = buckets[i];
            if (b == null) {
                System.out.println("Null Bucket with index " + i + " at level " + clusterLevel);
                continue;
            }
            double similarity = b.getSimilarityFor(d);
            if (similarity > bestValue && similarity > similarityThreshold) {
                bestMatch = b;
                bestValue = similarity;
                index = i;
            }
        }

        return index;
    }

    public void setupClustering(List<Document> initialDocs) {

        if (!hasMadeFirstClustering) {
            documentSlidingWindow.addAll(initialDocs);
            cluster();
            hasMadeFirstClustering = true;
        }
    }

    public boolean getHasMadeFirstClustering() {
        return hasMadeFirstClustering;
    }

    public void addDocument(final Document d) {

        d.generateFeatureVector(featureSpace);
        documentSlidingWindow.add(d);
        int matchIndex = putDocInBucket(d);

        if (matchIndex == -1) {
            bin.addDocument(d);
        }
        else if (buckets[matchIndex] != null) {
            buckets[matchIndex].addDocument(d);
        }
        else {
            System.err.println("the bucket match was null");
        }

        if (bin.isFull()) {
            System.out.println("Re-cluster triggered at level " + this.clusterLevel);
            cluster();
        }
    }

    public void cluster() {

        // Append window and bin document arrays
        Set<Document> docset = new HashSet<>(documentSlidingWindow);
        docset.addAll(bin.getDocuments());

        if (docset.size() < numberOfBuckets)
            throw new RuntimeException("docset.size() < numberOfBuckets");

        ArrayList<Document> docs = new ArrayList<>(docset);
        featureSpace.recreate(docs);
        bin.clear();

        for (Document d : docs) {
            d.generateFeatureVector(featureSpace);
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
            if (clusterLevel > 0) {
                buckets[i] = new Bucket(v, featureSpace, config, clusterLevel - 1);
            }
            else {
                buckets[i] = new Bucket(v, featureSpace);
            }

        }

        int[] documentDistribution = new int[numberOfBuckets + 1];
        for (Document d : docs) {
            int dest = putDocInBucket(d);
            documentDistribution[dest + 1]++;
        }
        System.out.println(this.clusterLevel + ": " + Arrays.toString(documentDistribution));
        for (Bucket b : buckets) {
            System.out.println(b.toString());
        }
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
