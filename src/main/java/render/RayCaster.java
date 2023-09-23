package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.MouseListener;
import main.Player;
import util.*;

import static render.RayCaster.Intersecter.getIntersection;
import static render.RayCaster.Intersecter.getIntersectionT;
import static render.RayCaster.Intersecter.areIntersecting;
import static util.Line.tMax;
import static util.Line.tMin;

public class RayCaster {
    public float renderDistance;
    public float fadeOutDistance;
    public float fov;
    public int rayCount;
    public List<Ray> rays;
    public int renderDepth = 100;
    public RayCaster(float fov, float renderDistance, int rayCount, float fadeOutDistance) {
        this.fadeOutDistance = fadeOutDistance;
        this.renderDistance = renderDistance;
        this.rayCount = rayCount;
        this.fov = fov;
    }

    public List<Ray> createRays(Player player) {

        float offset = 1.0f/ (float) Math.abs(Math.tan(fov/2.0f));
        List<Ray> rays = new ArrayList<>();
        // degrees -> radians
        double rad = Math.toRadians(player.viewAngle);
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
        float centerPointX = player.coordinates.x + centerDX;
        float centerPointY = player.coordinates.y + centerDY;
        // absolute edge point coordinates
        float edge1X = (centerPointX + nCenterDX);
        float edge1Y = (centerPointY + nCenterDY);
        float edge2X = (centerPointX - nCenterDX);
        float edge2Y = (centerPointY - nCenterDY);
        // X and Y steps between points used to cast rays
        float dXStep = (edge2X - edge1X) / (rayCount - 1.0f);
        float dYStep = (edge2Y - edge1Y) / (rayCount - 1.0f);

        for (int i = 0; i < rayCount; i++) {

            float x1 = player.coordinates.x;
            float y1 = player.coordinates.y;
            float dx = (edge1X + (dXStep * i)) - player.coordinates.x;
            float dy = (edge1Y + (dYStep * i)) - player.coordinates.y;
            float x2 = x1 + renderDistance * dx;
            float y2 = y1 + renderDistance * dy;

            Ray ray = new Ray(x1, y1, x2, y2);
            ray.id = i;

            rays.add(ray);
        }
        return rays;
    }
    public void cast(Player player, Map map) {
        List<Ray> rays = createRays(player);

        for (Ray ray:rays) {
            for (Wall wall: map.walls) {
                if (areIntersecting(ray,wall)) {
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
                float wallDistance = getIntersectionT(ray, w) * renderDistance;
                ray.intersectionDistanceOnRay.add(wallDistance);
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
        int depth = 0;
        List<List<Ray>> chainList = divideRays(rays, depth);

        for (List<Ray> sameWallRays : chainList) {
            // extract first and last ray
            Ray startRay = sameWallRays.get(0);
            Ray endRay = sameWallRays.get(sameWallRays.size() - 1);
            // get their relative position on the ray
            float minT = Math.min(startRay.intersectionRelDistanceOnWall.get(depth), endRay.intersectionRelDistanceOnWall.get(depth));
            float maxT = Math.max(startRay.intersectionRelDistanceOnWall.get(depth), endRay.intersectionRelDistanceOnWall.get(depth));

            // update the revealed parts of the wall on the map
            if (minT < startRay.intersectedWalls.get(depth).minVisibleT) {
                startRay.intersectedWalls.get(depth).minVisibleT = minT;
            }
            if (maxT > startRay.intersectedWalls.get(depth).maxVisibleT) {
                startRay.intersectedWalls.get(depth).maxVisibleT = maxT;
            }
        }
    }

    public static List<List<Ray>> divideRays(List<Ray> rays, int depth) {
        // divides rays into sub-lists based on the wall they intersected
        // i.e. example wall ids: 111 222 11 22 333
        List<List<Ray>> sublists = new ArrayList<>();
        List<Ray> currentSublist = new ArrayList<>();
        int currentID = -1;
        for (Ray ray : rays) {
            //currentID = -1; //-- uncomment for rectangles
            if (!ray.intersectedAnything.get(depth)) continue;
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

    public static class Intersecter {

        // SHOWS T ON THE FIRST GIVEN LINE
        public static float getIntersectionT(Line line1, Line line2) {
            return ((line1.start.y * line2.vector.x) - (line2.start.y * line2.vector.x) - (line1.start.x * line2.vector.y) + (line2.start.x * line2.vector.y)) / ((line1.vector.x * line2.vector.y) - (line1.vector.y * line2.vector.x));
        }

        public static Coordinates getIntersection(Line line1, Line line2) {
            // t is the scale of the line1 vector that ends with the intersection
            float t = getIntersectionT(line1, line2);
            return new Coordinates(line1.start.x + (line1.vector.x * t), line1.start.y + (line1.vector.y * t));
        }

        public static boolean areIntersecting(Line line1, Line line2) {

            // t, t2 == vector scale multipliers
            float t = getIntersectionT(line1, line2);
            float t2 = getIntersectionT(line2, line1);

            // the t is not "out of bounds"
            return t >= tMin && t <= tMax && t2 >= tMin && t2 <= tMax;
        }
    }
}