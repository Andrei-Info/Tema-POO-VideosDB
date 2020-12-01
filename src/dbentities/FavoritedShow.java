package dbentities;

public final class FavoritedShow {
    private final String name;
    private final int count;

    public FavoritedShow(final String name, final int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
