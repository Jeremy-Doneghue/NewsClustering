import java.util.*;

public class RejectBucket implements IBucket {

    private Document[] rejects;
    private int insertIndex = 0;

    public RejectBucket(final int capacity) {
        rejects = new Document[capacity];
    }

    @Override
    public void addDocument(final Document d) {

        if (insertIndex < rejects.length) {
            rejects[insertIndex++] = d;
        }
    }

    @Override
    public int size() {
        return this.insertIndex;
    }

    @Override
    public Document[] getDocuments() {
        return Arrays.copyOf(rejects, insertIndex);
    }

    public boolean isFull() {
        return insertIndex >= rejects.length;
    }
}
