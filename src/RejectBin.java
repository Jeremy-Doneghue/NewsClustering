import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RejectBin implements IBucket {

    private Document[] rejectList;
    private int capacity;
    private int newItemPointer = 0;


    public RejectBin(int capacity) {

        this.capacity = capacity;
        rejectList = new Document[capacity];
    }

    public void addDocument(Document d) {

        if (newItemPointer < capacity) {
            rejectList[newItemPointer++] = d;
        }
    }

    public int getSize() {
        return this.newItemPointer;
    }

    public Document[] getDocuments() {
        return Arrays.copyOf(rejectList, newItemPointer);
    }

    public boolean isFull() {
        return newItemPointer >= capacity;
    }
}
