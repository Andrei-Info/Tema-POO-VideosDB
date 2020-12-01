package dbentities;

public class AwardedActor {
    private final String name;
    private final int count;

    public AwardedActor(String name, int count) {
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
