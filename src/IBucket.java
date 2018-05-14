public interface IBucket {

    void addDocument(final Document d);
    Document[] getDocuments();
    int size();
}
