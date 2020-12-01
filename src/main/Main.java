package main;

import actor.ActorsAwards;
import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import dbentities.*;
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
                    return -actor1.getActor().compareTo(actor2.getActor());
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
            // Check for each award in the filter
            for(String award : action.getFilters().get(Constants.AWARDS_INDEX)) {
                if (!actor.getAwards().containsKey(ActorsAwards.valueOf(award))) {
                    hasAwards = false;
                }
            }

            // Add actor to map if they have all awards
            if (hasAwards) {
                // Count the total number of awards the actor has
                awardCount = 0;
                for (ActorsAwards award : actor.getAwards().keySet()) {
                    awardCount += actor.getAwards().get(award);
                }

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
                    return -actor1.getName().compareTo(actor2.getName());
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
                StringTokenizer stringTokenizer = new StringTokenizer(actor.getCareerDescription()
                        .toLowerCase(), Constants.DELIM);
                boolean hasThisKeyword = false;

                while (stringTokenizer.hasMoreTokens()) {
                    if (keyword.equals(stringTokenizer.nextToken())) {
                        hasThisKeyword = true;
                        break;
                    }
                }
                if (!hasThisKeyword) {
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
    private static void ratingQuery(ActionInputData action, Input input, JSONObject output) {
        // Add all rated shows to an array list that have a rating different
        // from 0 and fit all the filters
        ArrayList<RatedShow> shows = new ArrayList<>();

        if (action.getObjectType().equals(Constants.MOVIES)) {
            // Do so for each movie
            for (MovieInputData movie : input.getMovies()) {
                if (movie.getRating() != 0 && checkFilters(action.getFilters(), movie)) {
                    shows.add(new RatedShow(movie.getTitle(), movie.getRating()));
                }
            }
        } else {
            // Do so for each serial
            for (SerialInputData serial : input.getSerials()) {
                if (serial.getRating() != 0 && checkFilters(action.getFilters(), serial)) {
                    shows.add(new RatedShow(serial.getTitle(), serial.getRating()));
                }
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
                    return -show1.getName().compareTo(show2.getName());
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
     * Handles "favorite" queries
     * */
    @SuppressWarnings("unchecked")
    private static void favoriteQuery(ActionInputData action, Input input, JSONObject output) {
        // Add all favouited shows from user's data to a hashmap, counting the number of favorites
        // for each show
        HashMap<String, Integer> showsMap = new HashMap<>();

        for (UserInputData user : input.getUsers()) {
            for (String show : user.getFavoriteShows()) {
                // Add a show to the map if it's missing
                showsMap.putIfAbsent(show, 0);
                // Increment the show's favorite counter
                showsMap.put(show, showsMap.get(show) + 1);
            }
        }

        // Remove all shows from the map if they don't fit the object_type filter
        if (action.getObjectType().equals(Constants.MOVIES)) {
            for (SerialInputData serial : input.getSerials()) {
                showsMap.remove(serial.getTitle());
            }
        } else {
            for (MovieInputData movie : input.getMovies()) {
                showsMap.remove(movie.getTitle());
            }
        }

        // Add all show from the map into an array if they fit the filters
        ArrayList<FavoritedShow> shows = new ArrayList<>();
        ArrayList<ShowInput> allShows = new ArrayList<>(input.getMovies());
        allShows.addAll(input.getSerials());

        for (String show : showsMap.keySet()) {
            // Search for the show in all movies and serials
            for (ShowInput showInput : allShows) {
                if (showInput.getTitle().equals(show) && checkFilters(action.getFilters(),
                        showInput)) {
                    shows.add(new FavoritedShow(show, showsMap.get(show)));
                    break;
                }
            }
        }

        // Sort shows according to the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            shows.sort((show1, show2) -> {
                if (show1.getCount() == show2.getCount()) {
                    return show1.getName().compareTo(show2.getName());
                } else {
                    return Integer.compare(show1.getCount(), show2.getCount());
                }
            });
        } else {
            // Sort descending
            shows.sort((show1, show2) -> {
                if (show1.getCount() == show2.getCount()) {
                    return -show1.getName().compareTo(show2.getName());
                } else {
                    return -Integer.compare(show1.getCount(), show2.getCount());
                }
            });
        }

        // Add the first N shows to the output
        StringBuilder result = new StringBuilder("Query result: [");
        int count = 0;
        String prefix = "";
        for (FavoritedShow show : shows) {
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
     * Handles "longest" queries
     * */
    @SuppressWarnings("unchecked")
    private static void longest(ActionInputData action, Input input, JSONObject output) {
        // Add all shows that fit the filters to an array list
        ArrayList<DurationShow> shows = new ArrayList<>();

        if (action.getObjectType().equals(Constants.MOVIES)) {
            // Do so for each movie
            for (MovieInputData movie : input.getMovies()) {
                if (checkFilters(action.getFilters(), movie)) {
                    shows.add(new DurationShow(movie.getTitle(), movie.getDuration()));
                }
            }
        } else {
            // Do so for each serial
            for (SerialInputData serial : input.getSerials()) {
                if (checkFilters(action.getFilters(), serial)) {
                    shows.add(new DurationShow(serial.getTitle(), serial.getDuration()));
                }
            }
        }

        // Sort shows according to the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            shows.sort((show1, show2) -> {
                if (show1.getDuration() == show2.getDuration()) {
                    return show1.getName().compareTo(show2.getName());
                } else {
                    return Integer.compare(show1.getDuration(), show2.getDuration());
                }
            });
        } else {
            // Sort descending
            shows.sort((show1, show2) -> {
                if (show1.getDuration() == show2.getDuration()) {
                    return -show1.getName().compareTo(show2.getName());
                } else {
                    return -Integer.compare(show1.getDuration(), show2.getDuration());
                }
            });
        }

        // Add the first N shows to the output
        StringBuilder result = new StringBuilder("Query result: [");
        int count = 0;
        String prefix = "";
        for (DurationShow show : shows) {
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
     * Handles "most_viewed" queries
     * */
    @SuppressWarnings("unchecked")
    private static void mostViewed(ActionInputData action, Input input, JSONObject output) {
        // Add all viewed shows into a map
        HashMap<String, Integer> showsMap = new HashMap<>();
        for (UserInputData user : input.getUsers()) {
            for (String show : user.getHistory().keySet()) {
                // Add a show to the map if it's missing
                showsMap.putIfAbsent(show, 0);
                // Increment the show's view count
                showsMap.put(show, showsMap.get(show) + user.getHistory().get(show));
            }
        }

        // Add all shows that fit the filter into an array
        ArrayList<ViewedShow> shows = new ArrayList<>();

        for (String show : showsMap.keySet()) {
            // Search for the show in the appropiate show type
            if (action.getObjectType().equals(Constants.MOVIES)) {
                for (MovieInputData movie : input.getMovies()) {
                    if (movie.getTitle().equals(show) && checkFilters(action.getFilters(), movie))
                    {
                        shows.add(new ViewedShow(show, showsMap.get(show)));
                    }
                }
            } else {
                for (SerialInputData serial : input.getSerials()) {
                    if (serial.getTitle().equals(show) && checkFilters(action.getFilters(),
                            serial)) {
                        shows.add(new ViewedShow(show, showsMap.get(show)));
                    }
                }
            }
        }

        // Sort shows according to the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            shows.sort((show1, show2) -> {
                if (show1.getViewCount() == show2.getViewCount()) {
                    return show1.getName().compareTo(show2.getName());
                } else {
                    return Integer.compare(show1.getViewCount(), show2.getViewCount());
                }
            });
        } else {
            // Sort descending
            shows.sort((show1, show2) -> {
                if (show1.getViewCount() == show2.getViewCount()) {
                    return -show1.getName().compareTo(show2.getName());
                } else {
                    return -Integer.compare(show1.getViewCount(), show2.getViewCount());
                }
            });
        }

        // Add the first N shows to the output
        StringBuilder result = new StringBuilder("Query result: [");
        int count = 0;
        String prefix = "";
        for (ViewedShow show : shows) {
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
     * Handles "num_ratings" queries
     * */
    @SuppressWarnings("unchecked")
    private static void numberOfRatings(ActionInputData action, Input input, JSONObject output) {
        // Add all users and their number of ratings to a list
        ArrayList<RatingUser> users = new ArrayList<>();
        for (UserInputData user : input.getUsers()) {
            if (user.getRatings().size() != 0) {
                users.add(new RatingUser(user.getUsername(), user.getRatings().size()));
            }
        }

        // Sort users according to the action sort type
        if (action.getSortType().equals(Constants.ASCENDING)) {
            // Sort ascending
            users.sort((user1, user2) -> {
                if (user1.getRatingCount() == user2.getRatingCount()) {
                    return user1.getName().compareTo(user2.getName());
                } else {
                    return Integer.compare(user1.getRatingCount(), user2.getRatingCount());
                }
            });
        } else {
            // Sort descending
            users.sort((show1, show2) -> {
                if (show1.getRatingCount() == show2.getRatingCount()) {
                    return -show1.getName().compareTo(show2.getName());
                } else {
                    return -Integer.compare(show1.getRatingCount(), show2.getRatingCount());
                }
            });
        }

        // Add the first N users to the output
        StringBuilder result = new StringBuilder("Query result: [");
        int count = 0;
        String prefix = "";
        for (RatingUser user : users) {
            result.append(prefix);
            result.append(user.getName());
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
            case Constants.FAVORITE:
                favoriteQuery(action, input, output);
                break;
            case Constants.LONGEST:
                longest(action, input, output);
                break;
            case Constants.MOST_VIEWED:
                mostViewed(action, input, output);
                break;
            case Constants.NUM_RATINGS:
                numberOfRatings(action, input, output);
                break;
            default:
                break;
        }
    }

    /**
     * Finds the user based on their username
     * */
    private static UserInputData findUser(ActionInputData action, Input input) {
        for (UserInputData user : input.getUsers()) {
            if (user.getUsername().equals(action.getUsername())) {
                return user;
            }
        }

        return null;
    }

    /**
     * Handles "standard" recommendations
     * */
    @SuppressWarnings("unchecked")
    public static void standard(ActionInputData action, Input input, JSONObject output) {
        // Find the user who's asking for a recommandation
        UserInputData user = findUser(action, input);

        // Search through all shows in the database
        ShowInput show = null;
        // Search all movies
        for (MovieInputData movie : input.getMovies()) {
            if (user != null && !user.getHistory().containsKey(movie.getTitle())) {
                show = movie;
                break;
            }
        }
        // Search all serials
        if (show == null) {
            for (SerialInputData serial : input.getSerials()) {
                if (user != null && !user.getHistory().containsKey(serial.getTitle())) {
                    show = serial;
                    break;
                }
            }
        }

        // Output the result
        output.put(Constants.ID_STRING, action.getActionId());
        if (show != null) {
            output.put(Constants.MESSAGE, "StandardRecommendation result: " + show.getTitle());
        } else {
            output.put(Constants.MESSAGE, "StandardRecommendation cannot be applied!");
        }
    }

    /**
     * Handles "best_unseen" recommendations
     * */
    @SuppressWarnings("unchecked")
    public static void bestUnseen(ActionInputData action, Input input, JSONObject output) {
        // Find the user who's asking for a recommandation
        UserInputData user = findUser(action, input);

        // Add all shows to an array list
        ArrayList<RatedShow> shows = new ArrayList<>();

        // Do so for each movie
        for (MovieInputData movie : input.getMovies()) {
            shows.add(new RatedShow(movie.getTitle(), movie.getRating()));
        }

        // Do so for each serial
        for (SerialInputData serial : input.getSerials()) {
            shows.add(new RatedShow(serial.getTitle(), serial.getRating()));
        }

        // Sort shows descending based on their rating
        shows.sort((show1, show2) -> -Double.compare(show1.getRating(), show2.getRating()));

        // Find first unseen show
        String show = null;
        for (RatedShow s : shows) {
            if (user != null && !user.getHistory().containsKey(s.getName())) {
                show = s.getName();
                break;
            }
        }

        // Output the result
        output.put(Constants.ID_STRING, action.getActionId());
        if (show != null) {
            output.put(Constants.MESSAGE, "BestRatedUnseenRecommendation result: "
                    + show);
        } else {
            output.put(Constants.MESSAGE, "BestRatedUnseenRecommendation cannot be applied!");
        }
    }

    /**
     * Returns a show's input data based on its title
     * */
    private static ShowInput findShow(String title, Input input) {
        // Search through all movies
        for (MovieInputData movie : input.getMovies()) {
            if (movie.getTitle().equals(title)) {
                return movie;
            }
        }

        // Search thorough all serials
        for (SerialInputData serial : input.getSerials()) {
            if (serial.getTitle().equals(title)) {
                return serial;
            }
        }

        return null;
    }

    /**
     * Finds all shows in a given genre
     * */
    private static ArrayList<ShowInput> findAllGenreShows(String genre, Input input) {
        ArrayList<ShowInput> shows = new ArrayList<>();

        // Search through all movies
        for (MovieInputData movie : input.getMovies()) {
            if (movie.getGenres().contains(genre)) {
                shows.add(movie);
            }
        }
        // Search through all serials
        for (SerialInputData serial : input.getSerials()) {
            if (serial.getGenres().contains(genre)) {
                shows.add(serial);
            }
        }

        return shows;
    }

    /**
     * Handles "popular" recommendations
     * */
    @SuppressWarnings("unchecked")
    public static void popular(ActionInputData action, Input input, JSONObject output) {
        // Find the user who's asking for a recommandation
        UserInputData user = findUser(action, input);

        // Check that the user has a premium subscription
        if (user != null && !user.getSubscriptionType().equals(Constants.PREMIUM)) {
            user = null;
        }

        // Create a map of the genre and their popularity
        HashMap<String, Integer> genreMap = new HashMap<>();
        ShowInput showInput;

        // Populate said map
        // Iterate though all users
        for (UserInputData userInputData : input.getUsers()) {
            // Iterate though all shows seen by a user
            for (String title : userInputData.getHistory().keySet()) {
                // Find the show's input data
                showInput = findShow(title, input);
                if (showInput != null) {
                    // Iterate through all the show's genre
                    for (String genre : showInput.getGenres()) {
                        // Add genre to map if it's not already there
                        genreMap.putIfAbsent(genre, 0);
                        // Increase the genre's "popularity"
                        genreMap.put(genre, genreMap.get(genre)
                                + userInputData.getHistory().get(title));
                    }
                }
            }
        }

        // Add all genre into an array
        ArrayList<RatedGenre> genres = new ArrayList<>();
        for (String genre : genreMap.keySet()) {
            genres.add(new RatedGenre(genre, genreMap.get(genre)));
        }

        // Sort genres descending based on their popularity
        genres.sort((genre1, genre2) -> -Double.compare(genre1.getRating(), genre2.getRating()));

        // Find first unseend show
        ShowInput show = null;
        ArrayList<ShowInput> allGenreShows;

        // For all genre
        for (RatedGenre genre : genres) {
            // Look through all shows in the genre
            allGenreShows = findAllGenreShows(genre.getName(), input);
            for (ShowInput showInput1 : allGenreShows) {
                // If the show hasn't been seen by the user, return it
                if (user != null && !user.getHistory().containsKey(showInput1.getTitle())) {
                    show = showInput1;
                    break;
                }
            }

            // Check if a show has been found
            if (show != null) {
                break;
            }
        }

        // Output the result
        output.put(Constants.ID_STRING, action.getActionId());
        if (show != null) {
            output.put(Constants.MESSAGE, "PopularRecommendation result: "
                    + show.getTitle());
        } else {
            output.put(Constants.MESSAGE, "PopularRecommendation cannot be applied!");
        }
    }

    /**
     * Handles "favorite" recommendations
     * */
    @SuppressWarnings("unchecked")
    public static void favoriteRecom(ActionInputData action, Input input, JSONObject output) {
        // Find the user who's asking for a recommandation
        UserInputData user = findUser(action, input);

        // Check that the user has a premium subscription
        if (user != null && !user.getSubscriptionType().equals(Constants.PREMIUM)) {
            user = null;
        }

        // Create a map of all shows added to favorite
        HashMap<String, Integer> showMap = new HashMap<>();

        // Populate the map
        for (UserInputData userInputData : input.getUsers()) {
            for (String show : userInputData.getFavoriteShows()) {
                // Add show to map if it's not there already
                showMap.putIfAbsent(show, 0);
                // Increment the show's favorite counter
                showMap.put(show, showMap.get(show) + 1);
            }
        }

        // Add all shows from the map into an array
        ArrayList<FavoritedShow> shows = new ArrayList<>();
        for (String show : showMap.keySet()) {
            shows.add(new FavoritedShow(show, showMap.get(show)));
        }

        // Sort shows descending based on their favorited count
        shows.sort((show1, show2) -> -Integer.compare(show1.getCount(), show2.getCount()));

        // Search for an unseen show in the shows array
        String unseenShow = null;
        for (FavoritedShow show : shows) {
            if (user != null && !user.getHistory().containsKey(show.getName())) {
                unseenShow = show.getName();
                break;
            }
        }

        // Output the result
        output.put(Constants.ID_STRING, action.getActionId());
        if (unseenShow != null) {
            output.put(Constants.MESSAGE, "FavoriteRecommendation result: "
                    + unseenShow);
        } else {
            output.put(Constants.MESSAGE, "FavoriteRecommendation cannot be applied!");
        }
    }

    /**
     * Handles "search" recommendations
     * */
    @SuppressWarnings("unchecked")
    public static void search(ActionInputData action, Input input, JSONObject output) {
        // Find the user who's asking for a recommandation
        UserInputData user = findUser(action, input);

        // Get all shows in the given genre
        ArrayList<ShowInput> shows = findAllGenreShows(action.getGenre(), input);

        // Remove all shows already seen by the user
        if (user != null) {
            shows.removeIf(show -> user.getHistory().containsKey(show.getTitle()));
        }

        // Sort the shows based on their rating, ascending
        shows.sort((show1, show2) -> {
            if (show1.getRating() == show2.getRating()) {
                return show1.getTitle().compareTo(show2.getTitle());
            } else {
                return Double.compare(show1.getRating(), show2.getRating());
            }
        });

        // Add the shows to the output
        if (shows.size() != 0) {
            StringBuilder result = new StringBuilder("SearchRecommendation result: [");
            int count = 0;
            String prefix = "";
            for (ShowInput show : shows) {
                result.append(prefix);
                result.append(show.getTitle());
                prefix = ", ";
            }
            result.append("]");

            output.put(Constants.ID_STRING, action.getActionId());
            output.put(Constants.MESSAGE, result.toString());
        } else {
            // Output the error if there are no unseen shows in the genre
            output.put(Constants.ID_STRING, action.getActionId());
            output.put(Constants.MESSAGE, "SearchRecommendation cannot be applied!");
        }
    }

    /**
     * Handles "recommendation" actions
     * */
    public static void recommendation(ActionInputData action, Input input, JSONObject output) {
        switch (action.getType()) {
            case Constants.STANDARD:
                standard(action, input, output);
                break;
            case Constants.BEST_UNSEEN:
                bestUnseen(action, input, output);
                break;
            case Constants.POPULAR:
                popular(action, input, output);
                break;
            case Constants.FAVORITE:
                favoriteRecom(action, input, output);
                break;
            case Constants.SEARCH:
                search(action, input, output);
                break;
            default:
                break;
        }
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
