package util;

import org.joml.Vector2f;

public class Coordinates {
    public float x, y;
    public Coordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Coordinates() {

    }
    public boolean equals(Object o) {
        if (!(o instanceof Coordinates c)) {
            return false;
        }
        return this.hashCode() == c.hashCode();
    }
    @Override
    public int hashCode() {
        int prime = 17;
        float result = 11;

        result = prime * result + x;
        result = prime * result + y;

        return (int) result;
    }
    public void add(Vector2f v) {
        this.x += v.x;
        this.y += v.y;
    }
}
