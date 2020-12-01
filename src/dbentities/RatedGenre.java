package dbentities;

public final class RatedGenre {
    private final String name;
    private final int rating;

    public RatedGenre(final String name, final int rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }
}
