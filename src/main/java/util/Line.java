package util;

import org.joml.Vector2f;

public abstract class Line {
    public Coordinates start;
    public Coordinates end;
    public Vector2f vector;
    public static float tMin = 0;
    public static float tMax = 1;

    public Line(float x1, float y1, float x2, float y2) {
        this.start = new Coordinates(x1, y1);
        this.end = new Coordinates(x2, y2);
        this.vector = new Vector2f(x2-x1,y2-y1);
    }

    public Vector2f coordinatesFromT(float t) {
        return new Vector2f(start.x + t * vector.x, start.y + t * vector.y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Line l)) {
            return false;
        }
        return this.start.equals(l.start) && this.end.equals(l.end);
    }
}
