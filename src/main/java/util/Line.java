package util;

import org.joml.Vector2f;

public abstract class Line {
    public float x1, y1, x2, y2, dx, dy;
    public static float tMin = 0;
    public static float tMax = 1;

    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.dx = x2 - x1;
        this.dy = y2 - y1;
    }

    public Vector2f coordinatesFromT(float t) {
        return new Vector2f(x1+t*dx,y1+t*dy);
    }
}
