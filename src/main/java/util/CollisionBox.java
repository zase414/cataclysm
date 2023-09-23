package util;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;

public class CollisionBox {
    public float x, y;
    public Ray[] bounds;
    public float size;

    public CollisionBox(Ray[] bounds) {
        this.bounds = bounds;
    }
    public CollisionBox(float size, float x, float y) {
        this.size = size;
        Vector2f tl = new Vector2f(x - size, y + size); // top left
        Vector2f tr = new Vector2f(x + size, y + size); // top right
        Vector2f bl = new Vector2f(x - size, y - size); // bottom left
        Vector2f br = new Vector2f(x + size, y - size); // bottom right
        Ray[] bounds = new Ray[4];
        bounds[0] = new Ray(tl, tr);
        bounds[1] = new Ray(tr, br);
        bounds[2] = new Ray(br, bl);
        bounds[3] = new Ray(bl, tl);
        this.bounds = bounds;
    }

    public void update(double dx, double dy) {
        for (Ray b:bounds) {
            b.start.x += dx;
            b.end.x += dx;
            b.start.y += dy;
            b.end.y += dy;
        }
    }

    public void update(Vector2f v) {
        for (Ray b:bounds) {
            b.start.x += v.x;
            b.end.x += v.x;
            b.start.y += v.y;
            b.end.y += v.y;
        }
    }
}
