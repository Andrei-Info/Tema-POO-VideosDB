package dbentities;

public final class AwardedActor {
    private final String name;
    private final int count;

    public AwardedActor(final String name, final int count) {
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
