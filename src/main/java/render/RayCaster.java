package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.MouseListener;
import main.Player;
import util.*;

import static util.Line.getIntersection;
import static util.Line.getIntersectionT;

public class RayCaster {
    public float renderDistance;
    public float fadeOutDistance;
    public float fov;
    public int rayCount;
    public List<Ray> rays;
    public int renderDepth = 10;
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
            for (Wall wall: map.walls) {
                if (Ray.areIntersecting(ray,wall)) {
                    ray.intersectedWalls.add(wall);
                }
            }
            ray.intersectedWalls.sort((wall1, wall2) -> {
                Float intersectionRelDistanceOnRay1 = getIntersectionT(ray, wall1);
                Float intersectionRelDistanceOnRay2 = getIntersectionT(ray, wall2);
                return intersectionRelDistanceOnRay1.compareTo(intersectionRelDistanceOnRay2);
            });
            for (Wall w : ray.intersectedWalls) {
                ray.intersectedAnything.add(true);
                ray.intersectionRelDistanceOnRay.add(getIntersectionT(ray, w));
                ray.intersectionRelDistanceOnWall.add(getIntersectionT(w, ray));
                ray.colors.add(w.color);
                ray.intersections.add(getIntersection(w, ray));
            }
            for (int i = 0; i < renderDepth - ray.intersectedWalls.size(); i++) {
                ray.intersectedAnything.add(false);
            }
        }
        this.rays = rays;
    }
    public void updateMapVisibility() {
        int depth = 1;
        List<List<Ray>> chainList = divideRays(rays, depth);

        for (List<Ray> sameWallRays : chainList) {
            Ray startRay = sameWallRays.get(0);
            Ray endRay = sameWallRays.get(sameWallRays.size() - 1);
            float minT = Math.min(startRay.intersectionRelDistanceOnWall.get(depth), endRay.intersectionRelDistanceOnWall.get(depth));
            float maxT = Math.max(startRay.intersectionRelDistanceOnWall.get(depth), endRay.intersectionRelDistanceOnWall.get(depth));

            if (minT < startRay.intersectedWalls.get(depth).minVisibleT) {
                startRay.intersectedWalls.get(depth).minVisibleT = minT;
            }
            if (maxT > startRay.intersectedWalls.get(depth).maxVisibleT) {
                startRay.intersectedWalls.get(depth).maxVisibleT = maxT;
            }
        }
    }

    public static List<List<Ray>> divideRays(List<Ray> rays, int depth) {
        // divides rays into sublists based on the wall they intersected
        // i.e. example wall ids: 111 222 11 22 333
        List<List<Ray>> sublists = new ArrayList<>();
        List<Ray> currentSublist = new ArrayList<>();
        int currentID = -1;
        for (Ray ray : rays) {
            if (!ray.intersectedAnything.get(depth)) {
                continue;
            }
            int wallID = ray.intersectedWalls.get(depth).id;
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