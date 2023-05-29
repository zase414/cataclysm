package util;

import org.joml.Vector2f;

public class Ray extends Line {
    public float r, g, b, a;
    public float distanceToWall;
    public Vector2f firstIntersection = new Vector2f();
    public float intersectionT;
    public boolean intersectedAnything = false;
    public Wall intersectedWall = new Wall();
    public int id;
    public Ray(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }
}
