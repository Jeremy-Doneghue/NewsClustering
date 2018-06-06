public class ConfigurationContainer {

    public final double similarityThreshold;
    public final double secondLevelSimilarityThreshold;
    public final int reclusterThreshold;
    public final int secondLevelReclusterThreshold;
    public final int numberOfBuckets;
    public final int numberOf2ndLevelBuckets;
    public final int slidingWindowCapacity;

    public ConfigurationContainer(final double similarityThreshold,
                                  final double secondLevelSimilarityThreshold,
                                  final int reclusterThreshold,
                                  final int secondLevelReclusterThreshold,
                                  final int numberOfBuckets,
                                  final int numberOf2ndLevelBuckets,
                                  final int slidingWindowCapacity)
    {
        this.numberOf2ndLevelBuckets = numberOf2ndLevelBuckets;
        this.numberOfBuckets = numberOfBuckets;
        this.reclusterThreshold = reclusterThreshold;
        this.secondLevelReclusterThreshold = secondLevelReclusterThreshold;
        this.secondLevelSimilarityThreshold = secondLevelSimilarityThreshold;
        this.similarityThreshold = similarityThreshold;
        this.slidingWindowCapacity = slidingWindowCapacity;
    }

    //TODO: Decide on default values
//    public static ConfigurationContainer getDefaultConfiguration() {
//        return new ConfigurationContainer()
//    }
}
