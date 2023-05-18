package main;

public class Player {
    public float posX;
    public float posY;
    public float viewAngle;
    public float speed = 10.0f;

    public Player (Map map) {
        this.posX = map.spawnPoint.x;
        this.posY = map.spawnPoint.y;
        this.viewAngle = map.spawnViewAngle;
    }

    public Player () {

    }
}
