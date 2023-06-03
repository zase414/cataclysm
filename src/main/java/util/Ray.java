package util;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Ray extends Line {
    public List<Boolean> intersectedAnything = new ArrayList<>();
    public List<Wall> intersectedWalls = new ArrayList<>();
    public List<Vector2f> intersections = new ArrayList<>();
    public List<Float> intersectionRelDistanceOnRay = new ArrayList<>();
    public List<Float> intersectionRelDistanceOnWall = new ArrayList<>();
    public List<Color> colors = new ArrayList<>();
    public int id = -1;
    public Ray(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }
}