package sample;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TestFile {
    private String filename;
    private double spamProbability;
    private String actualClass;
    private String filePath;
    private String probabilityString;

    public TestFile(String filename,
                    double spamProbability,
                    String actualClass, String filePath) {
        this.filename = filename;
        this.spamProbability = spamProbability;
        this.actualClass = actualClass;
        this.filePath = filePath;
    }

    public String getFilename() { return this.filename; }

    public double getSpamProbability() { return this.spamProbability; }

    public String getSpamProbRounded() {
        DecimalFormat df = new DecimalFormat("0.00000");
        return df.format(this.spamProbability);
    }

    public String getActualClass() { return this.actualClass; }
    public void setFilename(String value) { this.filename = value; }
    public void setSpamProbability(double val) { this.spamProbability = val; }
    public void setActualClass(String value) { this.actualClass = value; }
    public String getProbabilityString() { return probabilityString; }


    public boolean determineSpamProbability(Map<String, Double> spamWordMap)
    {
        double n = 1.0;

        Map<String, Integer> wordCountMap;

        //Read in file and write all words the file has.
        WordCounter wordCounter = new WordCounter();
        File dataDir = new File(filePath);

        try {
            wordCounter.processFile(dataDir);
            wordCountMap = wordCounter.getWordCountsMap();
        } catch (IOException e) {
            e.printStackTrace();
            spamProbability = -1.0;
            //Failed to read file. Probability won't be calculated.
            return false;
        }

        //Check the amount of times every word shows up in the file
        Set<String> keys = wordCountMap.keySet();
        Iterator<String> keyIterator = keys.iterator();

        //wordCounter.outputWordCounts(2, new File("src/sample/data/outputFiles/test"));
        //Iterate through every word in the file and increase spam likeliness based on spam word probability from the training phase.
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            //Check if the word exists in the spam map. If it does, n will change.
            if (spamWordMap.containsKey(key)) {

                double wordProbability = spamWordMap.get(key);

                //Avoid log of 0
                if(wordProbability != 0 && wordProbability != 1)
                n+= (Math.log(1 - wordProbability) - Math.log(wordProbability));

                System.out.println("Probability: " + wordProbability);

                System.out.println("Adding: ");

                System.out.println(Math.log(1 - spamWordMap.get(key)) - Math.log(spamWordMap.get(key)));

                System.out.println("New n :" + n);
            }
        }

        //Set file spam probability
        spamProbability = (double)1 / (1+ Math.pow(Math.E, n));


        if (Double.isNaN(spamProbability)) spamProbability = 0.0f;


       probabilityString = getSpamProbRounded();

        //Guess if it's spam or not
        boolean spam;

        if (spamProbability > 0.5) spam = true;
        else spam = false;

        //Return value will be whether or not the program guessed correctly
        if (actualClass == "Spam" && spam) return true;
        else if (actualClass == "Ham" && !spam) return true;
        else return false;
    }

}