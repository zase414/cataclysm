package util;

public class Line {
    float x1, y1, x2, y2;
    float dx = x2 - x1;
    float dy = y2 - y1;
    static float tMin = 0, tMax = 1;

    public void init() {
        this.dx = this.x2 - this.x1;
        this.dy = this.y2 - this.y1;
    }




    public static float getIntersectionT(Line line1, Line line2) {
        float intersectionT = ((line1.y1 * line2.dx) - (line2.y1 * line2.dx) - (line1.x1 * line2.dy) + (line2.x1 * line2.dy)) / ((line1.dx * line2.dy) - (line1.dy * line2.dx));
        return intersectionT;
    }

    public static float[] getIntersection(Line line1, Line line2) {
        float[] intersection = new float[2];
        float t = getIntersectionT(line1, line2);

        intersection[0] = line1.x1 + (line1.dx * t);
        intersection[1] = line1.y1 + (line1.dy * t);

        return intersection;
    }

    public static boolean areIntersecting(Line line1, Line line2) {
        float t = getIntersectionT(line1, line2);
        float t2 = getIntersectionT(line2, line1);

        System.out.println("t, t2: "+t+", "+t2);
        System.out.println("vectors have the same root: " + (line1.dx / line1.dy == line2.dx / line2.dy));
        System.out.println("t is bigger or equal to tMin: " + (t >= tMin) + ", tMax = " + tMax);
        System.out.println("t is lower or equal to tMax: " + (t <= tMax) + ", tMin = " + tMin);
        System.out.println("t2 is bigger or equal to tMin: " + (t2 >= tMin) + ", tMax = " + tMax);
        System.out.println("t2 is lower or equal to tMax: " + (t2 <= tMax) + ", tMin = " + tMin);

        // the vectors don't have the same root and intersection is on both lines
        if (t >= tMin && t <= tMax && t2 >= tMin && t2 <= tMax) {
            return true;
        } else return false;
    }




}
