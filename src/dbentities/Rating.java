package dbentities;

public class Rating {
    /**
     * Rating of video, between 0 and 10
     */
    private final double score;
    /**
     * Name of movie or serial rated
     */
    private final String title;
    /**
     * The number of the rated season of a serial (0 in case of a movie)
     */
    private final int season;

    public Rating(double score, String title, int season) {
        this.score = score;
        this.title = title;
        this.season = season;
    }

    public double getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public int getSeason() {
        return season;
    }
}
