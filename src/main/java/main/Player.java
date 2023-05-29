package main;

import org.joml.Vector2f;
import util.CollisionBox;
import util.Ray;

public class Player {
    public float posX;
    public float posY;
    public float viewAngle;
    public float speed = 10.0f;
    public CollisionBox collisionBox = new CollisionBox();

    public Player (Map map) {
        this.posX = map.spawnPoint.x;
        this.posY = map.spawnPoint.y;
        this.viewAngle = map.spawnViewAngle;
    }
    public void updateCollisionBox() {
        float size = collisionBox.size;
        Ray[] bounds = collisionBox.bounds;
        Vector2f tl = new Vector2f(posX - size, posY + size); // top left
        Vector2f tr = new Vector2f(posX + size, posY + size); // top right
        Vector2f bl = new Vector2f(posX - size, posY - size); // bottom left
        Vector2f br = new Vector2f(posX + size, posY - size); // bottom right
        bounds[0] = new Ray(tl.x, tl.y, tr.x, tr.y);
        bounds[1] = new Ray(tr.x, tr.y, br.x, br.y);
        bounds[2] = new Ray(br.x, br.y, bl.x, bl.y);
        bounds[3] = new Ray(bl.x, bl.y, tl.x, tl.y);
    }
}
