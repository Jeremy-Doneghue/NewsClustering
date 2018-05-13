import java.util.Arrays;

public class SlidingWindow {

    private Document[] window;
    private int insertPoint = 0;

    public SlidingWindow(int size) {
        window = new Document[size];
    }

    public void addDocument(Document d) {
        window[insertPoint] = d;
        insertPoint = (insertPoint + 1) % window.length;
    }

    public Document[] getDocuments() {
        return this.window;
    }

    public int getSize() {
        return window.length;
    }
}
