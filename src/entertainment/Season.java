package entertainment;

/**
 * Information about a season of a tv show
 * <p>
 * DO NOT MODIFY
 * Modified: Needed to add number of people who rated the season and change the
 * rating into a single value.
 */
public final class Season {
    /**
     * Number of current season
     */
    private final int currentSeason;
    /**
     * Duration in minutes of a season
     */
    private int duration;
    /**
     * Rating of a season
     */
    private double rating = 0;
    /**
     * Number of people who rated the season
     */
    private int numberOfRaters = 0;

    public Season(final int currentSeason, final int duration) {
        this.currentSeason = currentSeason;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public double getRating() {
        return rating;
    }

    /**
     * Adds a rating to the season.
     */
    public void addRating(final double newRating) {
        this.rating = (this.rating * numberOfRaters + newRating)
                / (numberOfRaters + 1);
        numberOfRaters++;
    }

    @Override
    public String toString() {
        return "Episode{"
                + "currentSeason="
                + currentSeason
                + ", duration="
                + duration
                + '}';
    }
}

