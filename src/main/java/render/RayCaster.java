package render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import main.Map;
import main.Player;
import org.joml.Vector2f;
import util.*;

public class RayCaster {
    float renderDistance = 100;
    float fov = 90;
    int rayCount = 100;
    List<Line> rays;
    float offset;

    public RayCaster() {
        this.offset = (float) (1/Math.tan(Math.toRadians(fov/2)));
    }
    public void update(double viewAngle) {

        float posX = Player.posX;
        float posY = Player.posY;

        List<Line> rays = new ArrayList<>();
        // degrees -> radians
        double rad = Math.toRadians(viewAngle);
        // calculate vector to the center
        /*

            O  edge1
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
        O player                |                 O  edge2
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

        System.out.println("RAYS: -------------------");
        for (int i = 0; i < rayCount; i++) {

            Line ray = new Line();
            ray.x1 = posX;
            ray.y1 = posY;

            ray.x2 = renderDistance * ((posX + edge1X + dXStep * i) - ray.x1);
            ray.y2 = renderDistance * ((posY + edge1Y + dYStep * i) - ray.y1);

            ray.init();

            // ==== debug tool to check the rays ====
            //System.out.println("(" + ray.x1 + ", " + ray.y1 + ") (" + ray.x2 + ", " + ray.y2 + ")");

            rays.add(ray);
        }
        this.rays = rays;

        List<Vector2f> intersections;
    }
    public float[] getDistanceList() {

        List<Float>[] distances = new ArrayList[];

        int index = 0;
        for (Line ray:this.rays) {

            if (distances[index] == null) {
                distances[index] = new ArrayList<>();
            }

            for (Line wall: Map.get().walls) {

                if (Line.areIntersecting(ray,wall)) {
                    // create intersection point and add it to the list
                    Vector2f intersection = Line.getIntersection(ray,wall);
                    
                    float dx = Player.posX - intersection.x;
                    float dy = Player.posY - intersection.y;

                    distances[index].add((float) Math.sqrt((dx * dx) + (dy * dy)));
                }
            }
        
            index++;
        }


        return distances;
    }

    public static void main(String[] args) {

        // TEST

        RayCaster rayCaster = new RayCaster();
        rayCaster.update(0);
        float[] distArray = rayCaster.getDistanceList();
        for (int i = 0; i < distArray.length; i++) {
            System.out.println("distance '"+ i +"': " + distArray[i]);
        }


    }



}
