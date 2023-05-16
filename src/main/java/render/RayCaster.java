package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.Player;
import main.Window;
import util.*;

public class RayCaster {
    static float renderDistance = 100;
    static float fov = 120;
    static int rayCount = 100;
    static List<Line> rays;
    static float offset = (float) (1/Math.tan(Math.toRadians(fov/2)));

    static float[] vertexArray;
    static int[] elementArray;

    public static void createRays(double viewAngle) {
        float posX = Player.posX;
        float posY = Player.posY;
        float offset = RayCaster.offset;

        List<Line> rays = new ArrayList<>();
        // degrees -> radians
        double rad = Math.toRadians(viewAngle);
        // calculate vector to the center
        /*

            0  edge1
                *
                    *
                        *1
            centerDX        *
       -------------------------*centerPoint
                            *   |  *
                        *       |     *
             offset *           |        *
                *               |          1*
            *                  centerDY        *
        O player                |                 0  edge2
        */

        float centerDX = offset * (float) Math.sin(rad);
        float centerDY = offset * (float) Math.cos(rad);
        // the normal vectors we use to find the edges
        float nCenterDX = -(float) Math.cos(rad);
        float nCenterDY = (float) Math.sin(rad);
        // absolute center point coordinates
        float centerPointX = posX + centerDX;
        float centerPointY = posY + centerDY;
        // absolute edge point coordinates
        float edge1X = (centerPointX + nCenterDX);
        float edge1Y = (centerPointY + nCenterDY);
        float edge2X = (centerPointX - nCenterDX);
        float edge2Y = (centerPointY - nCenterDY);
        // X and Y steps between points used to cast rays
        float dXStep = (edge2X - edge1X) / (rayCount - 1);
        float dYStep = (edge2Y - edge1Y) / (rayCount - 1);

        for (int i = 0; i < rayCount; i++) {

            Line ray = new Line();
            ray.x1 = posX;
            ray.y1 = posY;

            ray.x2 = renderDistance * ((posX + edge1X + dXStep * i) - ray.x1);
            ray.y2 = renderDistance * ((posY + edge1Y + dYStep * i) - ray.y1);

            ray.init();

            System.out.println("(" + ray.x1 + ", " + ray.y1 + ") (" + ray.x2 + ", " + ray.y2 + ")");
            rays.add(ray);
        }
        RayCaster.rays = rays;
    }

    public static void main(String[] args) {

        // TEST
        createRays(0);
        int index = 0;
        for (Line ray:RayCaster.rays) {
            for (Line wall: Map.get().walls) {
                if (Line.areIntersecting(ray,wall)) {
                    float dx = Player.posX - Line.getIntersection(ray,wall)[0];
                    float dy = Player.posY - Line.getIntersection(ray,wall)[1];
                    System.out.println(Line.getIntersection(ray,wall)[0] + " " + Line.getIntersection(ray,wall)[1]);
                    float distance = (float) Math.sqrt((dx*dx)+(dy*dy));
                }
            }
            index++;
        }
    }
}
