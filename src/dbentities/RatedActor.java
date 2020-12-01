package dbentities;

public class RatedActor {
    private final String actor;
    private final double rating;

    public RatedActor(String actor, double rating) {
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
        return "RatedActor{" +
                "actor='" + actor + '\'' +
                ", rating=" + rating +
                '}';
    }
}
