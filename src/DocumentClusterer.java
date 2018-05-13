import com.github.javacliparser.Options;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.gui.AWTRenderer;
import moa.tasks.TaskMonitor;
//import no.uib.cipr.matrix.DenseMatrix;
//import no.uib.cipr.matrix.Matrix;
import moa.core.*;

import java.util.*;


public class DocumentClusterer {

    // List buckets = list of buckets
    RejectBin bin;
    double similarityThresh;
    int reclusterThreshold;

    Hashtable<String, Integer> featureSpace;

    SlidingWindow docWindow;

    public DocumentClusterer(Collection documents, double similarityThresh, int reclusterThreshold, int windowSize) {

        this.reclusterThreshold = reclusterThreshold;
        this.similarityThresh = similarityThresh;
        docWindow = new SlidingWindow(windowSize);
    }

    private void putDocInBucket(Document d) {

        IBucket bestMatch = bin;
        double bestValue = 0.0;
        // For each bucket
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

        docWindow.addDocument(d);
        putDocInBucket(d);
    }

    public void cluster() {

        // sets this.buckets
    }

    private void generateFeatureSpace() {

        featureSpace = new Hashtable<>();

        int counter = 0;
        for (Document d : docWindow.getDocuments()) {
            for (String word : d.getWords()) {
                if (!featureSpace.containsKey(word)) {
                    featureSpace.put(word, counter);
                    counter++;
                }
            }
        }
        for (Document d : bin.getDocuments()) {
            for (String word : d.getWords()) {
                if (!featureSpace.containsKey(word)) {
                    featureSpace.put(word, counter);
                    counter++;
                }
            }
        }
    }

    private double[] generateFeatureVector(Document d) {

        double[] out = new double[featureSpace.size()];

        for (String word : d.getWords()) {

            if (featureSpace.containsKey(word)) {
                int index = featureSpace.get(word);
                out[index] = d.getTFFor(word) * idfOfWord(word);
            }
        }

        return out;
    }

    public double idfOfWord(String word) {

        double occurrenceCounter = (double)Arrays.stream(docWindow.getDocuments()).filter(n -> n.containsWord(word)).count();
        occurrenceCounter += (double)Arrays.stream(bin.getDocuments()).filter(n -> n.containsWord(word)).count() + 1.0;

        double N = (double) docWindow.getSize() + bin.numItems();

        return Math.log(N / occurrenceCounter);
    }
}
