package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main extends Application {

    //Maps for frequency of word
    private Map<String, Integer> trainHamFrequency;
    private Map<String, Integer> trainSpamFrequency;

    //Maps for probability
    private Map<String, Float> appearanceInSpamProbability;
    private Map<String, Float> appearanceInHamProbability;
    private Map<String, Float> spamProbability;

    //File count for probability
    private int hamFileCount;
    private int spamFileCount;


    //Table stuff
    //private TableView<StudentRecord> words;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Assignment 1");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        /*
        //Table stuff
        TableColumn<Integer, String> gCol = new TableColumn<>("Grade");
        gCol.setPrefWidth(100);
        gCol.setCellValueFactory(new PropertyValueFactory<>("grade"));





        students = new TableView<>();
        students.getColumns().add(idCol);
        */

        trainingPhase();

        //String testSpam[] = {"S", "s"};
        //String testHam[] = {"S", "s"};

        //Testing phase
    }

    public void trainingPhase()
    {
        trainHamFrequency = new TreeMap<String, Integer>();
        trainSpamFrequency = new TreeMap<String, Integer>();

        appearanceInSpamProbability = new TreeMap<String, Float>();
        appearanceInHamProbability= new TreeMap<String, Float>();
        spamProbability= new TreeMap<String, Float>();

        //Make directories
        String trainingHam[] = {"src/sample/data/train/ham", "src/sample/outputFiles/train/ham.txt"};
        String trainingHam2[] = {"src/sample/data/train/ham2", "src/sample/outputFiles/train/ham2.txt"};
        String trainingSpam[] = {"src/sample/data/train/spam", "src/sample/outputFiles/train/spam.txt"};


        //Start training by counting the number of words ------------------------------------------------
        trainHamFrequency = countWords(trainingHam, 0);
        trainHamFrequency.putAll(
                trainHamFrequency = countWords(trainingHam2, 0));

        trainSpamFrequency = countWords(trainingSpam, 1);

        System.out.println(trainHamFrequency.get("Save"));


        //Calculate the probability of every word
        appearanceInHamProbability = calculateAppearanceProbability(trainHamFrequency, hamFileCount);
        appearanceInSpamProbability = calculateAppearanceProbability(trainSpamFrequency, spamFileCount);

        //Calculate actual spam probability
        spamProbability = calculateSpamProbability();

        System.out.println("Save probability");
        System.out.println(spamProbability.get("Save"));

    }

    public Map<String, Float> calculateAppearanceProbability(Map<String, Integer> map, int fileCount)
    {
        Map<String, Float> newProbabilityMap;

        newProbabilityMap = new TreeMap<>();

        Set<String> keys = map.keySet();
        Iterator<String> keyIterator = keys.iterator();

        //Iterate through every word and calculate its probability.
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();


            int appearances = map.get(key);
            float probability = (float)appearances / (float)fileCount;

            //Add new probability
            newProbabilityMap.put(key, probability);
        }

        return newProbabilityMap;
    }

    public Map<String, Float> calculateSpamProbability()
    {
        Map<String, Float> newProbabilityMap;

        newProbabilityMap = new TreeMap<>();

        Set<String> keys = appearanceInSpamProbability.keySet();
        Iterator<String> keyIterator = keys.iterator();

        //Iterate through every word and calculate its probability.
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            float probability = 1.0f;

            //Check if ham map also has the key, if so calculate probability.
            if (appearanceInHamProbability.containsKey(key)) {

                probability = appearanceInSpamProbability.get(key) / (appearanceInHamProbability.get(key) + appearanceInSpamProbability.get(key));
            }

            //Add new probability
            newProbabilityMap.put(key, probability);
        }

        return newProbabilityMap;
    }

    public static void main(String[] args) {



        launch(args);

    }

    public Map<String, Integer> countWords(String[] args, int category)
    {

    Map<String, Integer> frequencyMap = new TreeMap<>();

        //Main for Word Counter
        if (args.length < 2) {
            System.err.println("Usage: java WordCounter <dir> <outfile>");
            System.exit(0);
        }

        WordCounter wordCounter = new WordCounter();
        File dataDir = new File(args[0]);
        File outFile = new File(args[1]);

        //Try to read all the files of the given directory.
        try {
            wordCounter.processFile(dataDir);

            //Also count the number of files.
            System.out.println("Number of files: ");
            System.out.println(wordCounter.getFileCount());

            if(category == 0) hamFileCount += wordCounter.getFileCount();
            else if (category == 1) spamFileCount += wordCounter.getFileCount();


            //Save the map
            frequencyMap = wordCounter.outputWordCounts(2, outFile);

        } catch (FileNotFoundException e) {
            System.err.println("Invalid input dir: " + dataDir.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    return frequencyMap;
    }
}
