package util;

import static main.Settings.fadeOutDistance;

public class Color {
    public float r, g ,b ,a;
    public Color() {

    }
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color shade(float distance) {
        float r = Math.max(Math.min(this.r - this.r * (distance / fadeOutDistance), this.r), this.r / 100.0f);
        float g = Math.max(Math.min(this.g - this.g * (distance / fadeOutDistance), this.g), this.g / 100.0f);
        float b = Math.max(Math.min(this.b - this.b * (distance / fadeOutDistance), this.b), this.b / 100.0f);
        return new Color(r, g, b, this.a);
    }

    public Color texFade(float distance) {
        float c = Math.min(1f - (distance/fadeOutDistance), 1.0f);
        return new Color(c,c,c,1f);
    }
}
