package fileio;

import java.util.ArrayList;

/**
 * Information about a movie, retrieved from parsing the input test files
 * <p>
 * DO NOT MODIFY
 * Modification: Movies needed a rating
 */
public final class MovieInputData extends ShowInput {
    /**
     * Duration in minutes of a season
     */
    private final int duration;
    /**
     * Rating of the movie
     */
    private double rating = 0;
    /**
     * Number of people who rated the movie
     */
    private int numberOfRaters = 0;

    public MovieInputData(final String title, final ArrayList<String> cast,
                          final ArrayList<String> genres, final int year,
                          final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public double getRating() {
        return rating;
    }

    /**
     * Adds a rating to the movie.
     */
    public void addRating(final double newRating) {
        this.rating = (this.rating * numberOfRaters + newRating)
                / (numberOfRaters + 1);
        numberOfRaters++;
    }

    @Override
    public String toString() {
        return "MovieInputData{" + "title= "
                + super.getTitle() + "year= "
                + super.getYear() + "duration= "
                + duration + "cast {"
                + super.getCast() + " }\n"
                + "genres {" + super.getGenres() + " }\n ";
    }
}
