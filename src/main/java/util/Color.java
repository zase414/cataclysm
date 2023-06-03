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
        Color shadedColor = new Color();
        shadedColor.r = Math.max(Math.min(r - r * (distance / fadeOutDistance), r), r / 100.0f);
        shadedColor.g = Math.max(Math.min(g - g * (distance / fadeOutDistance), g), g / 100.0f);
        shadedColor.b = Math.max(Math.min(b - b * (distance / fadeOutDistance), b), b / 100.0f);
        return shadedColor;
    }
}
