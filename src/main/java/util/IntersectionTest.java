package util;
public class IntersectionTest {
    public static void main(String[] args) {
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
            System.out.println(Line.getIntersection(line1,line2)[0] + " // " + Line.getIntersection(line1, line2)[1]);


    }
}
