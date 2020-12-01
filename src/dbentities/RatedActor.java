package dbentities;

public final class RatedActor {
    private final String actor;
    private final double rating;

    public RatedActor(final String actor, final double rating) {
        this.actor = actor;
        this.rating = rating;
    }

    public String getActor() {
        return actor;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "RatedActor{"
                + "actor='" + actor + '\''
                + ", rating=" + rating
                + '}';
    }
}
