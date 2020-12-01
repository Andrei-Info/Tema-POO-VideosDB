package dbentities;

public class DurationShow {
    private final String name;
    private final int duration;

    public DurationShow(String name, int duration) {
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
