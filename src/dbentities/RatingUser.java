package dbentities;

public class RatingUser {
    private final String name;
    private final int ratingCount;

    public RatingUser(String user, int ratingCount) {
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
