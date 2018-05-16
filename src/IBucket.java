import java.util.List;

public interface IBucket {

    void addDocument(final Document d);
    List<Document> getDocuments();
    int size();
}
