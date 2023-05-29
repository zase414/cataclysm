package util;

public class Wall extends Line {
    public float r, g, b, a;
    public int id;
    public Wall(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }
    public Wall() {}
}
