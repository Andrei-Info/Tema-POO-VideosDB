package dbentities;

public class FavoritedShow {
    private final String name;
    private final int count;

    public FavoritedShow(String name, int count) {
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
