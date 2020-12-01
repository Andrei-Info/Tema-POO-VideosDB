package dbentities;

public class ViewedShow {
    private final String name;
    private final int viewCount;

    public ViewedShow(String name, int viewCount) {
        this.name = name;
        this.viewCount = viewCount;
    }

    public String getName() {
        return name;
    }

    public int getViewCount() {
        return viewCount;
    }
}
