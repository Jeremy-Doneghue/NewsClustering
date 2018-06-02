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

    protected Bucket[] buckets;
    protected FeatureSpace featureSpace;
    protected RejectBucket bin;

    private final double similarityThreshold;
    private final int reclusterThreshold;
    private final int numberOfBuckets;
    private final int clusterLevel;
    private boolean documentsAddedSinceLastCluster = false;

    private ConfigurationContainer config;

    private final SlidingWindow<Document> documentSlidingWindow;

    public DocumentClusterer(final Document[] documents, ConfigurationContainer config, final int clusterLevel) {
        this.reclusterThreshold = config.reclusterThreshold;
        this.similarityThreshold = config.similarityThreshold;
        this.numberOfBuckets = config.numberOfBuckets;
        this.clusterLevel = clusterLevel;

        this.config = config;

        documentSlidingWindow = new SlidingWindow<Document>(documents);

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

        if (documents.length > numberOfBuckets) {
            cluster();
        }

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

        if (bin.isFull() && documentsAddedSinceLastCluster) {
            System.out.println("Re-cluster triggered at level " + this.clusterLevel);
            cluster();
        }

        return index;
    }

    public void addDocument(final Document d) {

        documentsAddedSinceLastCluster = true;
        if (featureSpace == null) {
            documentSlidingWindow.add(d);
            cluster();
        }

        d.setFeatureVector(generateFeatureVectorFor(featureSpace, d));
        documentSlidingWindow.add(d);
        putDocInBucket(d);
    }

    protected void cluster() {

        documentsAddedSinceLastCluster = false;

        // Append window and bin document arrays
        Set<Document> docset = new HashSet<>(documentSlidingWindow);
        int binSize = 0;
        // On the first clustering we have no previous reject bin, so need to handle especially
        if (bin != null) {
            docset.addAll(bin.getDocuments());
            binSize = bin.size();
        }
        bin = new RejectBucket(reclusterThreshold);

        if (docset.size() < numberOfBuckets)
            throw new RuntimeException("docset.size() < numberOfBuckets");

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

        double occurrenceCounter = (double) docs.stream().filter(n -> n.containsWord(word)).count() + 1.0;
        double N = (double) docs.size();

        return Math.log(N / occurrenceCounter);
    }

    public List<String> getMostImportantWordsInBucket(final Bucket b, final int numWords) {
        if (featureSpace == null || b == null)
            return new ArrayList<>();
        //this is the centroid that represents the featureVector of a topic
        final SparseVector clusterVector = b.getClusterVector();

        final Set<Map.Entry<String, FeatureSpace.IndexIDF>> featureEntrySet = featureSpace.getTable().entrySet();
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
