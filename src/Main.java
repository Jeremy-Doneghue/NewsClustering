import moa.clusterers.Clusterer;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Main {

    private static int fileDateComparitor(File f1, File f2) {

        if (f1.lastModified() > f2.lastModified())
            return 1;

        if (f1.lastModified() < f2.lastModified())
            return -1;

        return 0;
    }

    public static void main(String[] args) {

        // TODO: Validate args
        String dir = args[0];
        File dataDir = new File(dir);
        BufferedReader reader;

        LinkedList<Document> fullDataSet = new LinkedList<>();

        File[] files = dataDir.listFiles();
        files = Arrays.stream(files).sorted(Main::fileDateComparitor).toArray(File[]::new);

        for (File f : files) {

            try {
                Map<String, Integer> bow = new HashMap<>();
                reader = Files.newBufferedReader(f.toPath());

                // TODO: check f is a .txt file and not a dir or other type
                if (!f.getName().endsWith(".txt"))
                    continue;

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] words = line.split(" ");
                    for (String word : words) {

                        word = word.toLowerCase();
                        word = word.replaceAll("[^a-z\\-]", "");

                        if (word.equals(""))
                            continue;

                        // If it's already in there, increment
                        if (bow.containsKey(word)) {
                            Integer count = bow.get(word);
                            bow.put(word, count + 1);
                        }
                        // Else add new entry
                        else {
                            bow.put(word, 1);
                        }
                    }
                }

                fullDataSet.add(new Document(bow, f.getName()));

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(f.getName());
            }
        }



        int initialWindowSize = 10;
        Document[] intialDocs = new Document[initialWindowSize];
        //TODO: check initial dataset is big enough
        for (int i = 0; i < initialWindowSize; i++) {
            intialDocs[i] = fullDataSet.get(i);
        }

        ConfigurationContainer config = new ConfigurationContainer(
                0.01,
                0.001,
                500,
                100,
                4,
                2
        );

        //DocumentClusterer c = new DocumentClusterer(intialDocs, 0.01, 300, 10, 4, 1);
        DocumentClusterer c = new DocumentClusterer(intialDocs, config, 1);

        for (int i = initialWindowSize; i < fullDataSet.size(); i++) {
            c.addDocument(fullDataSet.get(i));
        }


    }
}
