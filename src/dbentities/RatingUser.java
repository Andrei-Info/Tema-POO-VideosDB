package dbentities;

public final class RatingUser {
    private final String name;
    private final int ratingCount;

    public RatingUser(final String user, final int ratingCount) {
        this.name = user;
        this.ratingCount = ratingCount;
    }

    public String getName() {
        return name;
    }

    public int getRatingCount() {
        return ratingCount;
    }
}
