package util;
public class IntersectionTest {
    public static void main(String[] args) {
        Line line1 = new Line();
        Line line2 = new Line();

        line1.x1 = 1.0f;
        line1.y1 = 1.0f;
        line1.x2 = -6.0f;
        line1.y2 = -6.0f;

        line2.x1 = -6.0f;
        line2.y1 = 4.0f;
        line2.x2 = 2.0f;
        line2.y2 = -1.0f;

        line1.init();
        line2.init();

        if (Line.areIntersecting(line1,line2)) {
            System.out.println("lines are intersecting");
            System.out.println(Line.getIntersection(line1, line2)[0] + " // " + Line.getIntersection(line1, line2)[1]);
        } else System.out.println("no intersections found");

    }
}
