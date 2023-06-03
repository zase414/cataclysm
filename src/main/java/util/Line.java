package util;

import org.joml.Vector2f;

public abstract class Line {
    public float x1, y1, x2, y2, dx, dy;
    static float tMin = 0, tMax = 1;

    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.dx = x2 - x1;
        this.dy = y2 - y1;
    }

    public Line() {}

    // SHOWS T ON THE FIRST GIVEN LINE !!!!!
    public static float getIntersectionT(Line line1, Line line2) {
        return ((line1.y1 * line2.dx) - (line2.y1 * line2.dx) - (line1.x1 * line2.dy) + (line2.x1 * line2.dy)) / ((line1.dx * line2.dy) - (line1.dy * line2.dx));
    }

    public static Vector2f getIntersection(Line line1, Line line2) {
        // t is the scale of the line1 vector that ends with the intersection
        Vector2f intersection = new Vector2f();
        float t = getIntersectionT(line1, line2);

        intersection.x = line1.x1 + (line1.dx * t);
        intersection.y = line1.y1 + (line1.dy * t);

        return intersection;
    }

    public static boolean areIntersecting(Line line1, Line line2) {

        // t, t2 == vector scale multipliers
        float t = getIntersectionT(line1, line2);
        float t2 = getIntersectionT(line2, line1);

        // the t is not "out of bounds"
        return t >= tMin && t <= tMax && t2 >= tMin && t2 <= tMax;
    }

    public Vector2f coordinatesFromT(float t) {
        return new Vector2f(x1+t*dx,y1+t*dy);
    }
}
