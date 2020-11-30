package fileio;

import dbentities.Rating;

import java.util.ArrayList;
import java.util.Map;

/**
 * Information about an user, retrieved from parsing the input test files
 * <p>
 * DO NOT MODIFY
 * Modified: Added ratings given by the user, turned "movies" into "shows"
 */
public final class UserInputData {
    /**
     * User's username
     */
    private final String username;
    /**
     * Subscription Type
     */
    private final String subscriptionType;
    /**
     * The history of the shows seen
     */
    private final Map<String, Integer> history;
    /**
     * Shows added to favorites
     */
    private final ArrayList<String> favoriteShows;
    /**
     * User's given ratings
     */
    private final ArrayList<Rating> ratings = new ArrayList<>();

    public UserInputData(final String username, final String subscriptionType,
                         final Map<String, Integer> history,
                         final ArrayList<String> favoriteShows) {
        this.username = username;
        this.subscriptionType = subscriptionType;
        this.favoriteShows = favoriteShows;
        this.history = history;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, Integer> getHistory() {
        return history;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public ArrayList<String> getFavoriteShows() {
        return favoriteShows;
    }

    public ArrayList<Rating> getRatings() {
        return ratings;
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
    }

    public void addFavoriteShow(String show) {
        favoriteShows.add(show);
    }

    @Override
    public String toString() {
        return "UserInputData{" + "username='"
                + username + '\'' + ", subscriptionType='"
                + subscriptionType + '\'' + ", history="
                + history + ", favoriteMovies="
                + favoriteShows + '}';
    }
}
