package main;

import actor.ActorsAwards;
import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import dbentities.AwardedActor;
import dbentities.RatedActor;
import dbentities.RatedShow;
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
import java.util.*;

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
     * Handles "favorite" commands
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
     * Handles "view" commands
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
     * Handles "rating" commands
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

            // See if user already rated the show
            boolean rated = false;
            for (Rating r : user.getRatings()) {
                if (r.getTitle().equals(action.getTitle()) && r.getSeason()
                        == action.getSeasonNumber()) {
                    rated = true;
                    break;
                }
            }

            if (!rated) {
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
                            output.put(Constants.ID_STRING,
                                    action.getActionId());
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
                            output.put(Constants.ID_STRING,
                                    action.getActionId());
                            output.put(Constants.MESSAGE, "success -> "
                                    + action.getTitle() + " was rated with "
                                    + action.getGrade() + " by "
                                    + action.getUsername());
                            break;
                        }
                    }
                }
            } else {
                // The user has already rated the show, output error
                output.put(Constants.ID_STRING, action.getActionId());
                output.put(Constants.MESSAGE, "error -> " + action.getTitle()
                        + " has been already rated");
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
     * Handles "command" actions
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
     * Handles "average" queries
     * */
    @SuppressWarnings("unchecked")
    private static void average(ActionInputData action, Input input, JSONObject
            output) {
        // Add all actors to maps keeping track of their total rating and the
        // number of shows they play in
        HashMap<String, Double> rating = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();

        // Do so for each movie
        for (MovieInputData movie : input.getMovies()) {
            if (movie.getRating() == 0) {
                continue;
            }
            for (String actor : movie.getCast()) {
                rating.putIfAbsent(actor, (double)0);
                count.putIfAbsent(actor, 0);

                rating.put(actor, rating.get(actor) + movie.getRating());
                count.put(actor, count.get(actor) + 1);
            }
        }
        // Do so for each serial
        for (SerialInputData serial : input.getSerials()) {
            if (serial.getRating() == 0) {
                continue;
            }
            for (String actor : serial.getCast()) {
                rating.putIfAbsent(actor, (double)0);
                count.putIfAbsent(actor, 0);

                rating.put(actor, rating.get(actor) + serial.getRating());
                count.put(actor, count.get(actor) + 1);
            }
        }

        // Create an array list of all the actors and their overall rating
        ArrayList<RatedActor> actors = new ArrayList<>();
        for (String actor : rating.keySet()) {
            if (rating.get(actor) != 0) {
                actors.add(new RatedActor(actor, rating.get(actor)
                        / count.get(actor)));
            }
        }

        // Sort the array based on the input sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            actors.sort((actor1, actor2) -> {
                if (actor1.getRating() == actor2.getRating()) {
                    return actor1.getActor().compareTo(actor2.getActor());
                } else {
                    return Double.compare(actor1.getRating(),
                            actor2.getRating());
                }
            });
        } else {
            // Sort descending
            actors.sort((actor1, actor2) -> {
                if (actor1.getRating() == actor2.getRating()) {
                    return actor1.getActor().compareTo(actor2.getActor());
                } else {
                    return -Double.compare(actor1.getRating(),
                            actor2.getRating());
                }
            });
        }

        // Add first N actors to output
        StringBuilder result = new StringBuilder("Query result: [");
        String prefix = "";
        int cnt = 0;
        for (RatedActor actor : actors) {
            result.append(prefix);
            result.append(actor.getActor());
            prefix = ", ";

            cnt++;
            if (cnt == action.getNumber()) {
                break;
            }
        }
        result.append("]");

        output.put(Constants.ID_STRING, action.getActionId());
        output.put(Constants.MESSAGE, result.toString());
    }

    /**
     * Handles "awards" queries
     * */
    @SuppressWarnings("unchecked")
    private static void awards(ActionInputData action, Input input, JSONObject
            output) {
        // Add all actors who have all awards required in a map, together with
        // the total ammount of said awards.
        HashMap<String, Integer> count = new HashMap<>();

        // For each actor, check awards
        boolean hasAwards;
        int awardCount;
        for (ActorInputData actor : input.getActors()) {
            hasAwards = true;
            awardCount = 0;
            // Check for each award in the filter
            for(String award : action.getFilters().get(Constants.AWARDS_INDEX)) {
                if (!actor.getAwards().containsKey(ActorsAwards.valueOf(award))) {
                    hasAwards = false;
                } else {
                    // Count the awards the actor has relevant to the filter
                    awardCount += actor.getAwards().get(ActorsAwards.valueOf(award));
                }
            }

            // Add actor to map if they have all awards
            if (hasAwards) {
                count.put(actor.getName(), awardCount);
            }
        }

        // Create an array list of each actor and the number of awards they have
        ArrayList<AwardedActor> actors = new ArrayList<>();
        for (String actor : count.keySet()) {
            actors.add(new AwardedActor(actor, count.get(actor)));
        }

        // Sort the array based on the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            actors.sort((actor1, actor2) -> {
                if (actor1.getCount() == actor2.getCount()) {
                    return actor1.getName().compareTo(actor2.getName());
                } else {
                    return Integer.compare(actor1.getCount(), actor2.getCount());
                }
            });
        } else {
            // Sort descending
            actors.sort((actor1, actor2) -> {
                if (actor1.getCount() == actor2.getCount()) {
                    return actor1.getName().compareTo(actor2.getName());
                } else {
                    return -Integer.compare(actor1.getCount(), actor2.getCount());
                }
            });
        }

        // Add all actors from the array to output
        StringBuilder result = new StringBuilder("Query result: [");
        String prefix = "";
        for (AwardedActor actor : actors) {
            result.append(prefix);
            result.append(actor.getName());
            prefix = ", ";
        }
        result.append("]");

        output.put(Constants.ID_STRING, action.getActionId());
        output.put(Constants.MESSAGE, result.toString());
    }

    /**
     * Handles "filter_description" queries
     * */
    @SuppressWarnings("unchecked")
    private static void filterDescription(ActionInputData action, Input input, JSONObject
            output) {
        // Add all actors with all keywords to an array list
        ArrayList<String> actors = new ArrayList<>();
        boolean hasAllKeywords;

        for (ActorInputData actor : input.getActors()) {
            hasAllKeywords = true;
            // For each keyowrd in the filter
            for (String keyword : action.getFilters().get(Constants.WORDS_INDEX)) {
                // Make sure it's part of the actor's description
                if (!actor.getCareerDescription().contains(keyword)) {
                    hasAllKeywords = false;
                    break;
                }
            }

            // Add the actor to the array if they fit the filter
            if (hasAllKeywords) {
                actors.add(actor.getName());
            }
        }

        // Sort actors according to the action sort type
        // Sort ascending
        Collections.sort(actors);
        if (action.getSortType().equals(Constants.DESCENDING)) {
            // Sort descending
            Collections.reverse(actors);
        }

        // Add all actors from the array to output
        StringBuilder result = new StringBuilder("Query result: [");
        String prefix = "";
        for (String actor : actors) {
            result.append(prefix);
            result.append(actor);
            prefix = ", ";
        }
        result.append("]");

        output.put(Constants.ID_STRING, action.getActionId());
        output.put(Constants.MESSAGE, result.toString());
    }

    /**
     * Checks whether a show fits some filters
     * */
    @SuppressWarnings("RedundantIfStatement")
    private static boolean checkFilters(List<List<String>> filters, ShowInput show) {
        // Check the year filter
        if (filters.get(Constants.YEAR_INDEX).get(0) != null
                && filters.get(Constants.YEAR_INDEX).get(0)
                .compareTo(Integer.toString( show.getYear() ) ) != 0) {
            return false;
        }
        // Check the genre filter
        if (filters.get(Constants.GENRE_INDEX).get(0) != null
                && !show.getGenres().contains( filters
                .get(Constants.GENRE_INDEX).get(0) ) ) {
            return false;
        }

        return true;
    }

    /**
     * Handles "rating" queries
     * */
    @SuppressWarnings("unchecked")
    private static void ratingQuery(ActionInputData action, Input input, JSONObject
            output) {
        // Add all rated shows to an array list that have a rating different
        // from 0 and fit all the filters
        boolean filterFit;
        ArrayList<RatedShow> shows = new ArrayList<>();

        // Do so for each serial
        for (SerialInputData serial : input.getSerials()) {
            if (serial.getRating() != 0 && checkFilters(action.getFilters(), serial)) {
                shows.add(new RatedShow(serial.getTitle(), serial.getRating()));
            }
        }

        // Do so for each movie
        for (MovieInputData movie : input.getMovies()) {
            if (movie.getRating() != 0 && checkFilters(action.getFilters(), movie)) {
                shows.add(new RatedShow(movie.getTitle(), movie.getRating()));
            }
        }

        // Sort shows according to the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            shows.sort((show1, show2) -> {
                if (show1.getRating() == show2.getRating()) {
                    return show1.getName().compareTo(show2.getName());
                } else {
                    return Double.compare(show1.getRating(), show2.getRating());
                }
            });
        } else {
            // Sort descending
            shows.sort((show1, show2) -> {
                if (show1.getRating() == show2.getRating()) {
                    return show1.getName().compareTo(show2.getName());
                } else {
                    return -Double.compare(show1.getRating(), show2.getRating());
                }
            });
        }

        // Add the first N shwos from the array to the output
        StringBuilder result = new StringBuilder("Query result: [");
        int count = 0;
        String prefix = "";
        for (RatedShow show : shows) {
            result.append(prefix);
            result.append(show.getName());
            prefix = ", ";

            count++;
            if (count == action.getNumber()) {
                break;
            }
        }
        result.append("]");

        output.put(Constants.ID_STRING, action.getActionId());
        output.put(Constants.MESSAGE, result.toString());
    }

    /**
     * Handles "query" actions
     * */
    private static void query(ActionInputData action, Input input, JSONObject
            output) {
        switch (action.getCriteria()) {
            case Constants.AVERAGE:
                average(action, input, output);
                break;
            case Constants.AWARDS:
                awards(action, input, output);
                break;
            case Constants.FILTER_DESCRIPTIONS:
                filterDescription(action, input, output);
                break;
            case Constants.RATINGS:
                ratingQuery(action, input, output);
                break;
            default:
                break;
        }
    }

    /**
     * Handles "recommendation" actions
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
