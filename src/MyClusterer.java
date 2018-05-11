import com.github.javacliparser.Options;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.gui.AWTRenderer;
import moa.tasks.TaskMonitor;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import moa.core.*;

public class MyClusterer implements moa.clusterers.Clusterer {

    @Override
    public void setModelContext(InstancesHeader instancesHeader) {

    }

    @Override
    public InstancesHeader getModelContext() {
        return null;
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    @Override
    public void setRandomSeed(int i) {

    }

    @Override
    public boolean trainingHasStarted() {
        return false;
    }

    @Override
    public double trainingWeightSeenByModel() {
        return 0;
    }

    @Override
    public void resetLearning() {

    }

    @Override
    public void trainOnInstance(Instance instance) {

    }

    @Override
    public double[] getVotesForInstance(Instance instance) {
        return new double[0];
    }

    @Override
    public Measurement[] getModelMeasurements() {
        return new Measurement[0];
    }

    @Override
    public moa.clusterers.Clusterer[] getSubClusterers() {
        return new moa.clusterers.Clusterer[0];
    }

    @Override
    public int measureByteSize() {
        return 0;
    }

    @Override
    public String getPurposeString() {
        return null;
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public void prepareForUse() {

    }

    @Override
    public void prepareForUse(TaskMonitor taskMonitor, ObjectRepository objectRepository) {

    }

    @Override
    public moa.clusterers.Clusterer copy() {
        return null;
    }

    @Override
    public String getCLICreationString(Class<?> aClass) {
        return null;
    }

    @Override
    public void getDescription(StringBuilder stringBuilder, int i) {

    }

    @Override
    public Clustering getClusteringResult() {
        return null;
    }

    @Override
    public boolean implementsMicroClusterer() {
        return false;
    }

    @Override
    public Clustering getMicroClusteringResult() {
        return null;
    }

    @Override
    public boolean keepClassLabel() {
        return false;
    }

    @Override
    public AWTRenderer getAWTRenderer() {
        return null;
    }
}
