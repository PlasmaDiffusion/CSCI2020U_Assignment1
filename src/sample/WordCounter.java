package sample;

import java.io.*;
import java.util.*;

//This class is from demo 4. It now counts the number of files and also can create TestFile classes upon reading every file.

public class WordCounter {

    private Map<String,Integer> wordCounts;

    private int fileCount;



    private List<TestFile> testFiles;

    public WordCounter() {
        wordCounts = new TreeMap<>();
        testFiles = new ArrayList<>();
        fileCount = 0;
    }


    public Map<String, Integer> getWordCountsMap() {
        return wordCounts;
    }

    public int getFileCount()
    {return fileCount;}

    public List<TestFile> getTestFiles() {
        return testFiles;
    }

    public void processFile(File file) throws IOException {
        System.out.println("Processing " + file.getAbsolutePath() + "...");

        if (file.isDirectory()) {
            // process all the files in that directory
            File[] contents = file.listFiles();
            for (File current: contents) {
                processFile(current);
            }
        } else if (file.exists()) {
            // count the words in this file
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("\\s");//"[\s\.;:\?\!,]");//" \t\n.;,!?-/\\");
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (isWord(word)) {
                    countWord(word);
                }
            }

            //Increment file counter for some formulas.
           fileCount++;
            //Create test file objects (for when testing)
            testFiles.add(new TestFile(file.getName(),0, file.getAbsoluteFile().getParent(), file.getPath()));
        }
    }

    private boolean isWord(String word) {
        String pattern = "^[a-zA-Z]+$";
        if (word.matches(pattern)) {
            return true;
        } else {
            return false;
        }

        // also fine:
        //return word.matches(pattern);
    }

    private void countWord(String word) {
        if (wordCounts.containsKey(word)) {
            int oldCount = wordCounts.get(word);
            wordCounts.put(word, oldCount+1);
        } else {
            wordCounts.put(word, 1);
        }
    }

    //Output number of words.
    public void outputWordCounts(int minCount, File outFile)
            throws IOException {
        System.out.println("Saving word counts to " + outFile.getAbsolutePath());
        System.out.println("# of words: " + wordCounts.keySet().size());
        if (!outFile.exists()) {
            outFile.createNewFile();
            if (outFile.canWrite()) {
                PrintWriter fileOut = new PrintWriter(outFile);

                Set<String> keys = wordCounts.keySet();
                Iterator<String> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    int count = wordCounts.get(key);

                    if (count >= minCount) {
                        fileOut.println(key + ": " + count);
                    }
                }

                fileOut.close();


            } else {
                System.err.println("Error:  Cannot write to file: " + outFile.getAbsolutePath());
            }
        } else {
            System.err.println("Error:  File already exists: " + outFile.getAbsolutePath());
            System.out.println("outFile.exists(): " + outFile.exists());
            System.out.println("outFile.canWrite(): " + outFile.canWrite());
        }

    }


}