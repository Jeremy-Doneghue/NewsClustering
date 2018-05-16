import java.util.*;

public class RejectBucket implements IBucket {

    private final List<Document> rejects;
    private final int capacity;

    public RejectBucket(final int capacity) {
        rejects = new ArrayList<>();
        this.capacity = capacity;
    }

    @Override
    public void addDocument(final Document d) {
        if (rejects.size() < capacity) {
            rejects.add(d);
        }
    }

    @Override
    public int size() {
        return rejects.size();
    }

    @Override
    public List<Document> getDocuments() {
        return rejects;
    }

    public boolean isFull() {
        return rejects.size() >= capacity;
    }
}
