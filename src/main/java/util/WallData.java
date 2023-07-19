package util;

public class WallData {
    public Color color;
    public float x1, x2, y1, y2;
    public float topHeight, botHeight;
    public WallData(float x1, float y1, float x2, float y2, Color color, float hTop, float hBot) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.color = color;
    this.topHeight = hTop;
    this.botHeight = hBot;
}

}
