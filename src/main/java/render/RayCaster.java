package render;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.Player;
import org.joml.Vector2f;
import util.*;

public class RayCaster {
    float renderDistance;
    float fov;
    public int rayCount;
    List<Line> rays;


    public RayCaster(float fov, float renderDistance, int rayCount) {
        this.renderDistance = renderDistance;
        this.rayCount = rayCount;
        this.fov = fov;
    }
    public void update(Player player) {

        List<Line> rays = new ArrayList<>();

        float dAngle = fov / (rayCount - 1);
        System.out.println("angle step = " + dAngle);
        float startAngle = player.viewAngle - fov / 2;
        System.out.println("start angle = " + startAngle);
        for (int i = 0; i < rayCount; i++) {
            double angle = Math.toRadians(startAngle + i * dAngle);
            Line ray = new Line();
            ray.x1 = player.posX;
            ray.y1 = player.posY;
            float rayDX = renderDistance * (float) (Math.sin(angle));
            float rayDY = renderDistance * (float) (Math.cos(angle));
            ray.x2 = rayDX + ray.x1;
            ray.y2 = rayDY + ray.y1;

            ray.init();
            rays.add(ray);
        }

        for (Line ray:rays
             ) {
            System.out.println(ray.x1 + " " +  ray.y1 + " " +  ray.x2 + " " + ray.y2);
        }
        this.rays = rays;
    }
    public List<Float>[] getDistanceListArray(Map map, Player player) {

        List<Float>[] distances = new ArrayList[rayCount];

        int index = 0;
        for (Line ray:this.rays) {

            if (distances[index] == null) {
                distances[index] = new ArrayList<>();
            }

            for (Line wall: map.walls) {
                if (Line.areIntersecting(ray,wall)) {
                    // calculate the distance of the intersection to the player
                    Vector2f intersection = Line.getIntersection(ray,wall);

                    float dx = player.posX - intersection.x;
                    float dy = player.posY - intersection.y;
                    float distance = (float) Math.sqrt((dx * dx) + (dy * dy));

                    // ==== debug ====
                    System.out.println("intersection: " + intersection.x + " ; " + intersection.y);
                    distances[index].add(distance);
                }
            }
            index++;
        }

        return distances;
    }

    public static void main(String[] args) {

        // TEST
        Map map = new Map("C:\\Users\\zas\\IdeaProjects\\cataclysm\\assets\\maps\\testmap.json");
        map.compile();
        Player player = new Player(map);
        System.out.println("x: " + player.posX + ", y: " + player.posY + ", angle: " + player.viewAngle);

        RayCaster rayCaster = new RayCaster(90, 1000, 100);
        rayCaster.update(player);
        System.out.println("renderDistance: " + rayCaster.renderDistance + ", fov: " + rayCaster.fov + ", rayCount: " + rayCaster.rayCount);

        List<Float>[] distArray = rayCaster.getDistanceListArray(map, player);

        for (int i = 0; i < distArray.length; i++) {
            for (int j = 0; j < distArray[i].size(); j++) {
                System.out.println("distance '"+ i +"': " + distArray[i].get(j));
            }
        }
    }
}