package render;

import java.util.ArrayList;
import java.util.List;

import util.*;

public class RayCaster {
    static float zoom = 3;
    static int rayCount = 100;
    public static List<Line> createRays(float posX, float posY, double viewAngle) {

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
              zoom  *           |        *
                *               |          1*
            *                  centerDY        *
        O player                |                 0  edge2
        */

        float centerDX = zoom * (float) Math.sin(rad);
        float centerDY = zoom * (float) Math.cos(rad);
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

            ray.x2 = posX + edge1X + (dXStep * i);
            ray.y2 = posY + edge1Y + (dYStep * i);

            ray.init();

            System.out.println(ray.x1 + " ; " + ray.y1 + " | " + ray.x2 + " ; " + ray.y2);
            rays.add(ray);
        }
        return rays;
    }

    public static void main(String[] args) {
        createRays(0,0,-44);

    }

}
