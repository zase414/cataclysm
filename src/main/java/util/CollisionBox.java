package util;

import main.Map;
import main.Player;
import org.joml.Vector2f;
public class CollisionBox {
    public Line[] bounds = new Line[4];
    public float size;
    public void update(Player player) {
        float posX = player.posX;
        float posY = player.posY;
        Vector2f tl = new Vector2f(posX - size, posY + size); // top left
        Vector2f tr = new Vector2f(posX + size, posY + size); // top right
        Vector2f bl = new Vector2f(posX - size, posY - size); // bottom left
        Vector2f br = new Vector2f(posX + size, posY - size); // bottom right
        bounds[0] = new Line(tl.x, tl.y, tr.x, tr.y);
        bounds[1] = new Line(tr.x, tr.y, br.x, br.y);
        bounds[2] = new Line(br.x, br.y, bl.x, bl.y);
        bounds[3] = new Line(bl.x, bl.y, tl.x, tl.y);
    }

    public boolean checkForCollisionsPlayer(Map map, Player player) {
        boolean isClipping = false;
        for (Line bound:player.collisionBox.bounds) {
            for (Line wall:map.walls) {
                if (Line.areIntersecting(bound, wall)) {
                    isClipping = true;
                }
            }
        }
        return isClipping;
    }


}
