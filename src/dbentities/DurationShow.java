package dbentities;

public final class DurationShow {
    private final String name;
    private final int duration;

    public DurationShow(final String name, final int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }
}
