package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.MouseListener;
import main.Player;
import org.joml.Vector2f;
import util.*;

public class RayCaster {
    public float renderDistance;
    public float fadeOutDistance;
    public float fov;
    public int rayCount;
    public List<Ray> rays;


    public RayCaster(float fov, float renderDistance, int rayCount, float fadeOutDistance) {
        this.fadeOutDistance = fadeOutDistance;
        this.renderDistance = renderDistance;
        this.rayCount = rayCount;
        this.fov = fov;
    }
    public void cast(Player player, Map map) {

        List<Ray> rays = new ArrayList<>();

        float dAngle = fov / (rayCount - 1);
        //System.out.println("angle step = " + dAngle);
        float startAngle = player.viewAngle - fov / 2;
        //System.out.println("start angle = " + startAngle);
        for (int i = 0; i < rayCount; i++) {
            double rayAngle = Math.toRadians(startAngle + i * dAngle);

            float x1 = player.posX;
            float y1 = player.posY;
            float dx = renderDistance * (float) (Math.sin(rayAngle));
            float dy = renderDistance * (float) (Math.cos(rayAngle));
            float x2 = dx + x1;
            float y2 = dy + y1;

            Ray ray = new Ray(x1, y1, x2, y2);

            ray.id = i;
            rays.add(ray);
        }
        for (Ray ray:rays) {
            float minDist = renderDistance + 1;
            Wall nearestXWall = new Wall();
            Vector2f nearestXCoordinates = new Vector2f();
            float nearestXT = 0;
            for (Wall wall: map.walls) {
                if (Ray.areIntersecting(ray,wall)) {
                    ray.intersectedAnything = true;

                    Vector2f intersection = Ray.getIntersection(ray,wall);
                    float intersectionT = Ray.getIntersectionT(wall, ray);

                    // calculate the distance of the intersection to the player
                    double dx = player.posX - intersection.x;
                    double dy = player.posY - intersection.y;
                    double wallDistance = Math.sqrt((dx * dx) + (dy * dy));

                    // get the values of the nearest wall
                    if (wallDistance < minDist) {
                        minDist = (float) wallDistance;
                        nearestXWall = wall;
                        nearestXCoordinates = intersection;
                        nearestXT = intersectionT;
                    }
                }
            }
            if (ray.intersectedAnything) {
                ray.distanceToWall = minDist;
                float r = nearestXWall.r, g = nearestXWall.g, b = nearestXWall.b, a = nearestXWall.a;
                ray.r = Math.max(Math.min(r - r * (minDist / fadeOutDistance), r), r / 100.0f);
                ray.g = Math.max(Math.min(g - g * (minDist / fadeOutDistance), g), g / 100.0f);
                ray.b = Math.max(Math.min(b - b * (minDist / fadeOutDistance), b), b / 100.0f);
                ray.a = a;
                ray.firstIntersection = nearestXCoordinates;
                ray.intersectedWall = nearestXWall;
                ray.intersectionT = nearestXT;
            }
        }
        this.rays = rays;
    }

    public void updateMapVisibility() {
        List<List<Ray>> chainList = divideRays(rays);

        for (List<Ray> sameWallRays : chainList) {
            Ray startRay = sameWallRays.get(0);
            Ray endRay = sameWallRays.get(sameWallRays.size() - 1);
            float minT = Math.min(startRay.intersectionT, endRay.intersectionT);
            float maxT = Math.max(startRay.intersectionT, endRay.intersectionT);

            if (minT < startRay.intersectedWall.minVisibleT) {
                startRay.intersectedWall.minVisibleT = minT;
            }
            if (maxT > startRay.intersectedWall.maxVisibleT) {
                startRay.intersectedWall.maxVisibleT = maxT;
            }
        }
    }

    public static List<List<Ray>> divideRays(List<Ray> rays) {
        // divides rays into sublists based on the wall they intersected
        // i.e. example wall ids: 111 222 11 22 333
        List<List<Ray>> sublists = new ArrayList<>();
        List<Ray> currentSublist = new ArrayList<>();
        int currentID = -1;
        for (Ray ray : rays) {
            int wallID = ray.intersectedWall.id;
            if (!ray.intersectedAnything) {
                continue;
            }
            if (wallID != currentID) {
                currentSublist = new ArrayList<>();
                sublists.add(currentSublist);
            }
            currentSublist.add(ray);
            currentID = wallID;
        }
        return sublists;
    }
    public void adjustRayCount() {
        rayCount = Math.max(rayCount + (int) MouseListener.getScrollY(), 5);
    }
}