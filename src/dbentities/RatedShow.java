package dbentities;

public final class RatedShow {
    private final String name;
    private final double rating;

    public RatedShow(final String name, final double rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }
}
