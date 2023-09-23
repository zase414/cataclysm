package testing;

import main.Map;
import main.Player;
import render.RayCaster;

public class RayCasterTest {
    public static void main(String[] args) {

        Map map = new Map("assets/maps/testmap.json");
        map.compile();
        Player player = new Player(map);
        System.out.println("x: " + player.coordinates.x + ", y: " + player.coordinates.y + ", angle: " + player.viewAngle);

        RayCaster rayCaster = new RayCaster(100, 1000, 100, 300);
        rayCaster.cast(player, map);
        System.out.println("renderDistance: " + rayCaster.renderDistance + ", fov: " + rayCaster.fov + ", rayCount: " + rayCaster.rayCount);
    }
}
