package util;

import main.Map;
import main.Player;
public class CollisionBox {
    public Ray[] bounds = new Ray[4];
    public float size;
    public static boolean checkForCollisionsPlayer(Map map, Player player) {
        boolean isClipping = false;
        for (Line bound:player.collisionBox.bounds) {
            for (Wall wall:map.walls) {
                if (Ray.areIntersecting(bound, wall)) {
                    isClipping = true;
                }
            }
        }
        return isClipping;
    }
}
