package main;
import java.awt.Point;
import java.rmi.UnexpectedException;
import java.util.Optional;

import static java.lang.Math.*;

public class Line {
    float k, b;
    float xMin, xMax;

    public static void main(String[] args) {
        Line line1 = new Line();
        Line line2 = new Line();

        line1.k = 1.0f;
        line1.b = 1.0f;

        line2.k = 2.0f;
        line2.b = 0.0f;

        line1.xMin = 1.0f;
        line1.xMax = 4.0f;
        line2.xMin = 0.0f;
        line2.xMax = 6.0f;

        if (areIntersecting(line1,line2)) {
            System.out.println("lines are intersecting");
            System.out.println(getIntersection(line1, line2)[0] + " / " + getIntersection(line1, line2)[1]);
        } else System.out.println("no intersections found");


    }


    public static float[] getIntersection(Line line1, Line line2) {
        float[] intersection = new float[2];

        float k1 = line1.k;
        float k2 = line2.k;
        float b1 = line1.b;
        float b2 = line2.b;

        float x = (b2 - b1) / (k1 - k2);
        float y = k1 * x + b1;

        intersection[0] = x;
        intersection[1] = y;

        return intersection;
        //return Optional.of(point);
    }

    public static boolean areIntersecting(Line line1, Line line2) {

        float k1 = line1.k;
        float k2 = line2.k;
        float b1 = line1.b;
        float b2 = line2.b;

        float xMax = min(line1.xMax,line2.xMax);
        float xMin = max(line1.xMin,line2.xMin);


        if (k1 == k2) {
            return false;
        }

        float x = (b2 - b1) / (k1 - k2);

        if (x >= xMin && x <= xMax) {
            return true;
        } else return false;
    }




}
