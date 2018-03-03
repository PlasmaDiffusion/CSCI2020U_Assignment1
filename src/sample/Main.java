package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
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
    private Map<String, Double> appearanceInSpamProbability;
    private Map<String, Double> appearanceInHamProbability;
    private Map<String, Double> spamProbability;

    //File count for probability
    private int hamFileCount;
    private int spamFileCount;

    //Stats after the test
    private int correctGuesses = 0;
    private int truePositives = 0;
    private int falsePositives = 0;
    private int trueNegatives = 0;
    private float accuracy;
    private float precision;


    private File mainDirectory;
    //Test file objects
    private ObservableList<TestFile> testFiles;

    //Table stuff
    private TableView<TestFile> testTable;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Assignment 1");



        testFiles = FXCollections.observableArrayList();

        //First choose the directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        mainDirectory = directoryChooser.showDialog(primaryStage);


        //Train and test
        trainingPhase();

        testingPhase();



        //Now make a table

        //Filename
        TableColumn<TestFile, String> fileCol = new TableColumn<>("File");
        fileCol.setPrefWidth(400);
        fileCol.setCellValueFactory(new PropertyValueFactory<>("filename"));

        //Probability
        TableColumn<TestFile, String> probCol = new TableColumn<>("Spam Probability");
        probCol.setPrefWidth(200);
        probCol.setCellValueFactory(new PropertyValueFactory<>("probabilityString"));

        //Actual class
        TableColumn<TestFile, String> classCol = new TableColumn<>("Actual Class");
        classCol.setPrefWidth(100);
        classCol.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

        testTable = new TableView<>();
        testTable.getColumns().add(fileCol);
        testTable.getColumns().add(classCol);
        testTable.getColumns().add(probCol);

        //Gridpane for stats
        GridPane gridPane = new GridPane();

        gridPane.setPadding(new Insets(10, 10, 10, 10));


        gridPane.setHgap(5);
        gridPane.setVgap(5);

        //Accuracy label
        Label nameText = new Label("Accuracy: ");
        Label accuracyText = new Label(Float.toString(accuracy));


        gridPane.add(testTable, 1, 0);
        gridPane.add(nameText, 0, 1);
        gridPane.add(accuracyText, 1, 1);


        //Precision label
        Label nameText2 = new Label("Precision: ");
        Label precisionText = new Label(Float.toString(precision));

        gridPane.add(nameText2, 0, 2);
        gridPane.add(precisionText, 1, 2);

        primaryStage.setScene(new Scene(gridPane, 850, 675));
        primaryStage.show();



        testTable.setItems(testFiles);

    }

    public void trainingPhase()
    {
        trainHamFrequency = new TreeMap<String, Integer>();
        trainSpamFrequency = new TreeMap<String, Integer>();

        appearanceInSpamProbability = new TreeMap<String, Double>();
        appearanceInHamProbability= new TreeMap<String, Double>();
        spamProbability= new TreeMap<String, Double>();

        //Make directories
        String trainingHam = mainDirectory+"/train/ham";
        String trainingHam2 = mainDirectory+"/train/ham2";
        String trainingSpam = mainDirectory+"/train/spam";


        //Start training by counting the number of words
        trainHamFrequency = countWords(trainingHam, 0);
        trainHamFrequency = countWords(trainingHam2, -1); //<--- The previous map is added onto this new one

        trainSpamFrequency = countWords(trainingSpam, 1);



        //Calculate the probability of every word
        appearanceInHamProbability = calculateAppearanceProbability(trainHamFrequency, hamFileCount);
        appearanceInSpamProbability = calculateAppearanceProbability(trainSpamFrequency, spamFileCount);

        //Calculate actual spam probability
        spamProbability = calculateSpamProbability();


    }

    public Map<String, Double> calculateAppearanceProbability(Map<String, Integer> map, int fileCount)
    {
        Map<String, Double> newProbabilityMap;

        newProbabilityMap = new TreeMap<>();

        Set<String> keys = map.keySet();
        Iterator<String> keyIterator = keys.iterator();

        //Iterate through every word and calculate its probability.
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();


            int appearances = map.get(key);


            double probability = (double)appearances / (double)fileCount;

            //Add new probability
            newProbabilityMap.put(key, probability);
        }

        return newProbabilityMap;
    }

    public Map<String, Double> calculateSpamProbability()
    {
        Map<String, Double> newProbabilityMap;

        newProbabilityMap = new TreeMap<>();

        Set<String> keys = appearanceInSpamProbability.keySet();
        Iterator<String> keyIterator = keys.iterator();

        //Iterate through every word and calculate its probability.
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            double probability = 0.999;

            //Check if ham map also has the key, if so calculate probability. If not probability is ~1. (An exact number of 1 would cause a log error later.)
            if (appearanceInHamProbability.containsKey(key)) {

                probability = appearanceInSpamProbability.get(key) / (appearanceInHamProbability.get(key) + appearanceInSpamProbability.get(key));




            }

            if (probability == 1.0) probability = 0.999;

            //Extra formula that makes low frequency words from the training phase have less impact.
            probability = ((3 * 0.5) + (trainSpamFrequency.get(key) * probability)) / (3 + trainSpamFrequency.get(key));


            //Add new probability
            newProbabilityMap.put(key, probability);
        }


        return newProbabilityMap;
    }

    public void testingPhase()
    {
        //Make directories
        String testingHam = mainDirectory+"/test/ham";
        String testingSpam = mainDirectory+"/test/spam";

        countWords(testingHam, 2);
        countWords(testingSpam, 3);

        //Determine the probability of every file. Record how correct the program is as well.
        for(int i = 0; i < testFiles.size(); i++)
        {
            TestFile t = testFiles.get(i);

            if(t.determineSpamProbability(spamProbability)) //Estimated correctly
            {
                correctGuesses++;

                if (t.getActualClass() == "Ham")trueNegatives++;

                if (t.getActualClass() == "Spam") truePositives++;

            }
            else //Estimated incorrectly. See if it was a false positive.
            {
                if (t.getActualClass() == "Ham")falsePositives++;
            }
        }

        //Calculate accuracy and precision
        accuracy = (float)correctGuesses/(float)testFiles.size();
        precision = (float)truePositives/(float)(truePositives+falsePositives);
    }


    public static void main(String[] args) {

        launch(args);

    }

    //Reads all files in a directory.
    public Map<String, Integer> countWords(String path, int category)
    {

    Map<String, Integer> frequencyMap = new TreeMap<>();




        WordCounter wordCounter = new WordCounter();
        File dataDir = new File(path);

        //Special case for second ham folder where the current ham frequency is sent to the counter, so both folders word frequencies get added together.
        if (category == -1)
        {
            wordCounter.setWordCounts(trainHamFrequency);
            category = 0;
        }

        //Try to read all the files of the given directory.
        try {
            wordCounter.processFile(dataDir);

            //Also count the number of files.
            System.out.println("Number of files: ");
            System.out.println(wordCounter.getFileCount());

            //Category 0-1 gets the file count for training. Category 2-3 gets a list of test file classes
            if(category == 0) hamFileCount += wordCounter.getFileCount();
            else if (category == 1) spamFileCount += wordCounter.getFileCount();
            else if (category >= 2) {

                List<TestFile> newTestFiles = FXCollections.observableArrayList();
                newTestFiles = wordCounter.getTestFiles();

                //Add on each test file to the programs main list. Also set which ones are Ham and Spam.
                for (int i = 0; i < newTestFiles.size(); i++)
                {
                    testFiles.add(newTestFiles.get(i));


                    if (category == 2)
                        testFiles.get(testFiles.size() -1).setActualClass("Ham");
                    else if (category == 3)
                        testFiles.get(testFiles.size() -1).setActualClass("Spam");
                }

            }

            //Save the map if the program is training
            if (category < 2) {
                frequencyMap = wordCounter.getWordCountsMap();
            }

        } catch (FileNotFoundException e) {
            System.err.println("Invalid input dir: " + dataDir.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    return frequencyMap;
    }
}
