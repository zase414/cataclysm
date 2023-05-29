package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
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
            float minDistance = renderDistance + 1;
            for (Wall wall: map.walls) {
                if (Ray.areIntersecting(ray,wall)) {
                    // calculate the distance of the intersection to the player
                    Vector2f intersection = Ray.getIntersection(ray,wall);
                    float intersectionT = Ray.getIntersectionT(ray, wall);
                    ray.intersectedAnything = true;

                    double dx = player.posX - intersection.x;
                    double dy = player.posY - intersection.y;
                    double wallDistance = Math.sqrt((dx * dx) + (dy * dy));

                    if (wallDistance < minDistance) {
                        minDistance = (float) wallDistance;
                        ray.r = Math.max(Math.min(wall.r - wall.r * (minDistance / fadeOutDistance), wall.r), wall.r / 100.0f);
                        ray.g = Math.max(Math.min(wall.g - wall.g * (minDistance / fadeOutDistance), wall.g), wall.g / 100.0f);
                        ray.b = Math.max(Math.min(wall.b - wall.b * (minDistance / fadeOutDistance), wall.b), wall.b / 100.0f);
                        ray.a = wall.a;
                        ray.firstIntersection = intersection;
                        ray.intersectedWall = wall;
                        ray.intersectionT = intersectionT;
                    }


                    // ==== debug ====
                    //System.out.println("intersection: " + intersection.x + " ; " + intersection.y);
                }
            }
            if (minDistance != 0.0 && minDistance < renderDistance + 1) {
                ray.distanceToWall = minDistance;

            }
            //System.out.println(ray.distanceToWall);
        }
        this.rays = rays;
    }
}