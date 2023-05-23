package util;

import main.Map;
import main.Player;
import org.joml.Vector2f;
public class CollisionBox {
    public Ray[] bounds = new Ray[4];
    public float size;
    public void update(Player player) {
        float posX = player.posX;
        float posY = player.posY;
        Vector2f tl = new Vector2f(posX - size, posY + size); // top left
        Vector2f tr = new Vector2f(posX + size, posY + size); // top right
        Vector2f bl = new Vector2f(posX - size, posY - size); // bottom left
        Vector2f br = new Vector2f(posX + size, posY - size); // bottom right
        bounds[0] = new Ray(tl.x, tl.y, tr.x, tr.y);
        bounds[1] = new Ray(tr.x, tr.y, br.x, br.y);
        bounds[2] = new Ray(br.x, br.y, bl.x, bl.y);
        bounds[3] = new Ray(bl.x, bl.y, tl.x, tl.y);
    }

    public boolean checkForCollisionsPlayer(Map map, Player player) {
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
