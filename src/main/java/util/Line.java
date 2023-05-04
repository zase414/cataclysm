package util;

public class Line {
    float x1, y1, x2, y2;
    float dx = x2 - x1;
    float dy = y2 - y1;
    float tMin, tMax;

    public void init() {
        this.dx = this.x2 - this.x1;
        this.dy = this.y2 - this.y1;
        this.tMin = 0;
        this.tMax = (this.x2 - this.x1) / this.dx;
    }




    public static float getIntersectionT(Line line1, Line line2) {

        float intersectionT =     ((line2.dx * line1.y1) - (line2.dx * line2.y1) - (line2.dy * line1.x1) + (line2.dy * line2.x1)) /
                                                  ((line2.dy * line1.dx) - (line2.dx * line1.dy));

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

        // the vectors don't have the same root
        if ((!(
            line1.dx / line1.dy == line2.dx / line2.dy)) &&
            t >= line1.tMin &&
            t <= line1.tMax) {
            return true;
        } else return false;
    }




}
