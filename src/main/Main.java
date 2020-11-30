package main;

import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import dbentities.Rating;
import entertainment.Season;
import fileio.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
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
    @SuppressWarnings("unchecked")
    private static void favorite(ActionInputData action, Input input,
            JSONObject output) {
        // Find the user that wants to add a show to favorite
        UserInputData user = null;
        for (UserInputData u : input.getUsers()) {
            if (u.getUsername().equals(action.getUsername())) {
                user = u;
                break;
            }
        }

        // Check if the user has seen the show and doesn't have it already in
        // his favorite list
        if (user != null && user.getHistory().containsKey(action.getTitle())) {
            boolean favorite = false;

            for (String show : user.getFavoriteShows()) {
                if (show.equals(action.getTitle())) {
                    favorite = true;
                    break;
                }
            }
            // Add show to user's favorite shows if conditions are met
            if (!favorite) {
                user.addFavoriteShow(action.getTitle());
                output.put(Constants.ID_STRING, action.getActionId());
                output.put(Constants.MESSAGE, "success -> " + action.getTitle()
                        + " was added as favourite");
            } else {
                // Output error because the user already has the show in his
                // favorites list
                output.put(Constants.ID_STRING, action.getActionId());
                output.put(Constants.MESSAGE, "error -> " + action.getTitle()
                        + " is already in favourite list");
            }
        } else {
            // The user hasn't seen the video they are trying to add to
            // favorite, output an error
            output.put(Constants.ID_STRING, action.getActionId());
            output.put(Constants.MESSAGE, "error -> " + action.getTitle()
                    + " is not seen");
        }
    }

    /**
     * Handles view commands
     * */
    @SuppressWarnings("unchecked")
    private static void view(ActionInputData action, Input input, JSONObject
            output) {
        // Find the user that wants to view a show
        UserInputData user = null;
        for (UserInputData u : input.getUsers()) {
            if (u.getUsername().equals(action.getUsername())) {
                user = u;
                break;
            }
        }

        if (user != null) {
            Map<String, Integer> history = user.getHistory();
            // Add show to viewed if it's not already
            history.putIfAbsent(action.getTitle(), 0);
            // Increment view count
            history.put(action.getTitle(), history.get(action.getTitle()) + 1);

            // Output success message
            output.put(Constants.ID_STRING, action.getActionId());
            output.put(Constants.MESSAGE, "success -> " + action.getTitle()
                    + " was viewed with total views of "
                    + history.get(action.getTitle()));
        }
    }

    /**
     * Handles rating actions
     * */
    @SuppressWarnings("unchecked")
    private static void rating(ActionInputData action, Input input,
            JSONObject output) {
        // Find the user that wants to rate a show
        UserInputData user = null;
        for (UserInputData u : input.getUsers()) {
            if (u.getUsername().equals(action.getUsername())) {
                user = u;
                break;
            }
        }

        if (user != null && user.getHistory().containsKey(action.getTitle())) {
            Rating rating = new Rating(action.getGrade(), action.getTitle(),
                    action.getSeasonNumber());

            // Add the rating to the user's profile
            user.addRating(rating);

            // Search for the show being rated
            if (action.getSeasonNumber() == 0) {
                // Searching for a movie
                for (MovieInputData movie : input.getMovies()) {
                    if (movie.getTitle().equals(action.getTitle())) {
                        // Rate the movie
                        movie.addRating(action.getGrade());
                        // Output message
                        output.put(Constants.ID_STRING, action.getActionId());
                        output.put(Constants.MESSAGE, "success -> "
                                + action.getTitle() + " was rated with "
                                + action.getGrade() + " by "
                                + action.getUsername());
                        break;
                    }
                }
            } else {
                // Searching for a serial
                for (SerialInputData serial : input.getSerials()) {
                    if (serial.getTitle().equals(action.getTitle())) {
                        ArrayList<Season> seasons = serial.getSeasons();
                        // Rate the season
                        seasons.get(action.getSeasonNumber() - 1)
                                .addRating(action.getGrade());
                        // Output message
                        output.put(Constants.ID_STRING, action.getActionId());
                        output.put(Constants.MESSAGE, "success -> "
                                + action.getTitle() + " was rated with "
                                + action.getGrade() + " by "
                                + action.getUsername());
                        break;
                    }
                }
            }
        } else {
            // The user hasn't seen the video they are trying to rate, output
            // an error
            output.put(Constants.ID_STRING, action.getActionId());
            output.put(Constants.MESSAGE, "error -> " + action.getTitle()
                    + " is not seen");
        }
    }

    /**
     * Handles command actions
     * */
    private static void command(ActionInputData action, Input input, JSONObject
            output) {
        switch (action.getType()) {
            case Constants.FAVORITE:
                favorite(action, input, output);
                break;
            case Constants.VIEW:
                view(action, input, output);
                break;
            case Constants.RATING:
                rating(action, input, output);
            default:
                break;
        }
    }

    /**
     * Handles query actions
     * */
    private static void query(ActionInputData action, Input input, JSONObject
            output) {
        ;
    }

    /**
     * Handles recommendation actions
     * */
    public static void recommendation(ActionInputData action, Input input,
            JSONObject output) {
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
    @SuppressWarnings("unchecked")
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        InputLoader inputLoader = new InputLoader(filePath1);
        Input input = inputLoader.readData();

        Writer fileWriter = new Writer(filePath2);
        JSONArray arrayResult = new JSONArray();

        //TODO add here the entry point to your implementation
        for (ActionInputData action : input.getCommands()) {
            JSONObject output = new JSONObject();

            switch (action.getActionType()) {
                case Constants.COMMAND:
                    command(action, input, output);
                    break;
                case Constants.QUERY:
                    query(action, input, output);
                    break;
                case Constants.RECOMMENDATION:
                    recommendation(action, input, output);
                    break;
                default:
                    break;
            }

            arrayResult.add(output);
        }

        fileWriter.closeJSON(arrayResult);
    }
}
