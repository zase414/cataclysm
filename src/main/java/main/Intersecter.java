package main;

import org.joml.Vector2f;
import util.Line;

import static util.Line.tMax;
import static util.Line.tMin;

public class Intersecter {

    // SHOWS T ON THE FIRST GIVEN LINE
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
}
