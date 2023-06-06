package util;

public class Wall extends Line {
    public Color color = new Color();
    public int id;
    public float height;
    public Wall(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }
    public float minVisibleT = 2.0f, maxVisibleT = -2.0f;
}
