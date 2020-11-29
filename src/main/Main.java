package main;

import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import fileio.ActionInputData;
import fileio.Input;
import fileio.InputLoader;
import fileio.Writer;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * Handles favorite commands
     * */
    private static void favorite(ActionInputData action, Input input) {
        ;
    }

    /**
     * Handles command actions
     * */
    private static void command(ActionInputData action, Input input) {
        switch (action.getType()) {
            case "favorite":
                favorite(action, input);
                break;
            default:
                break;
        }
    }

    /**
     * Handles query actions
     * */
    private static void query(ActionInputData action, Input input) {
        ;
    }

    /**
     * Handles recommendation actions
     * */
    public static void recommendation(ActionInputData action, Input input) {
        ;
    }

    /**
     * Call the main checker and the coding style checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(Constants.TESTS_PATH);
        Path path = Paths.get(Constants.RESULT_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        File outputDirectory = new File(Constants.RESULT_PATH);

        Checker checker = new Checker();
        checker.deleteFiles(outputDirectory.listFiles());

        for (File file : Objects.requireNonNull(directory.listFiles())) {

            String filepath = Constants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getAbsolutePath(), filepath);
            }
        }

        checker.iterateFiles(Constants.RESULT_PATH, Constants.REF_PATH,
                Constants.TESTS_PATH);
        Checkstyle test = new Checkstyle();
        test.testCheckstyle();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        InputLoader inputLoader = new InputLoader(filePath1);
        Input input = inputLoader.readData();

        Writer fileWriter = new Writer(filePath2);
        JSONArray arrayResult = new JSONArray();

        //TODO add here the entry point to your implementation
        for (ActionInputData action : input.getCommands()) {
            switch (action.getActionType()) {
                case "command":
                    command(action, input);
                    break;
                case "query":
                    query(action, input);
                    break;
                case "recommendation":
                    recommendation(action, input);
                    break;
                default:
                    break;
            }
        }

        fileWriter.closeJSON(arrayResult);
    }
}
