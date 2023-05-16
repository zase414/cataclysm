package util;

import org.joml.Vector2f;

public class Line {
    public float x1, y1, x2, y2;
    public float dx = x2 - x1;
    public float dy = y2 - y1;
    static float tMin = 0, tMax = 1;

    public void init() {
        this.dx = this.x2 - this.x1;
        this.dy = this.y2 - this.y1;
    }

    public static float getIntersectionT(Line line1, Line line2) {
        return ((line1.y1 * line2.dx) - (line2.y1 * line2.dx) - (line1.x1 * line2.dy) + (line2.x1 * line2.dy)) / ((line1.dx * line2.dy) - (line1.dy * line2.dx));
    }

    public static Vector2f getIntersection(Line line1, Line line2) {
        Vector2f intersection = new Vector2f();
        float t = getIntersectionT(line1, line2);

        intersection.x = line1.x1 + (line1.dx * t);
        intersection.y = line1.y1 + (line1.dy * t);

        return intersection;
    }

    public static boolean areIntersecting(Line line1, Line line2) {
        float t = getIntersectionT(line1, line2);
        float t2 = getIntersectionT(line2, line1);

        /*System.out.println("t, t2: "+t+", "+t2);
        System.out.println("vectors have the same root: " + (line1.dx / line1.dy == line2.dx / line2.dy));
        System.out.println("t is bigger or equal to tMin: " + (t >= tMin) + ", tMax = " + tMax);
        System.out.println("t is lower or equal to tMax: " + (t <= tMax) + ", tMin = " + tMin);
        System.out.println("t2 is bigger or equal to tMin: " + (t2 >= tMin) + ", tMax = " + tMax);
        System.out.println("t2 is lower or equal to tMax: " + (t2 <= tMax) + ", tMin = " + tMin);*/

        // the vectors don't have the same root and intersection is on both lines
        return t >= tMin && t <= tMax && t2 >= tMin && t2 <= tMax;
    }

    public static void main(String[] args) {


        // TEST

        Line line1 = new Line();
        Line line2 = new Line();

        line1.x1 = -2.0f;
        line1.y1 = 1.0f;
        line1.x2 = 0.0f;
        line1.y2 = 4.0f;

        line2.x1 = -2.0f;
        line2.y1 = 2.5f;
        line2.x2 = 0.0f;
        line2.y2 = 3.0f;

        line1.init();
        line2.init();

        System.out.println("1dx: " + line1.dx);
        System.out.println("1dy: " + line1.dy);

        System.out.println("2dx: " + line2.dx);
        System.out.println("2dy: " + line2.dy);


        System.out.println(Line.areIntersecting(line1,line2));
        System.out.println(Line.getIntersection(line1,line2).x + " // " + Line.getIntersection(line1, line2).y);


    }

}
