package main;

import org.joml.Vector2d;
import org.joml.Vector2f;
import util.CollisionBox;
import util.Ray;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Player {
    public float posX;
    public float posY;
    public float viewAngle;
    public float speed = 10.0f;
    public CollisionBox collisionBox = new CollisionBox();
    public Vector2d movementVector = new Vector2d(0.0, 0.0);

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
    public void updatePlayerMovementVector(float dt) {
        Vector2d dPos = new Vector2d(0.0, 0.0);
        double strafeMultiplier = 1.0;
        boolean playerIsStrafing = (KeyListener.isKeyPressed(GLFW_KEY_W) || KeyListener.isKeyPressed(GLFW_KEY_S)) && (KeyListener.isKeyPressed(GLFW_KEY_D) || KeyListener.isKeyPressed(GLFW_KEY_A));
        double movementMultipliers = speed * dt * strafeMultiplier;

        if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
            dPos.x -= movementMultipliers * Math.sin(Math.toRadians(viewAngle));
            dPos.y -= movementMultipliers * Math.cos(Math.toRadians(viewAngle));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle - 90));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle - 90));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle + 90));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle + 90));
        }

        if (playerIsStrafing) {
            dPos.x *= Math.sqrt(2) / 2;
            dPos.y *= Math.sqrt(2) / 2;
        }
        movementVector = dPos;
    }
    public void updatePlayerPos(Map map) {
        // vector of movement
        Vector2d dPos = movementVector;

        posX += dPos.x;

        // update X coordinate of the collision box
        updateCollisionBox();
        if (CollisionBox.checkForCollisionsPlayer(map, this)) {
            posX -= dPos.x;
        }

        posY += dPos.y;

        // update Y coordinate of the collision box
        updateCollisionBox();
        if (CollisionBox.checkForCollisionsPlayer(map, this)) {
            posY -= dPos.y;
        }

        updateCollisionBox();
    }
    public void updateViewAngle() {
        if (glfwGetInputMode(Window.get().glfwWindow, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            viewAngle -= Settings.mouseSensitivity * MouseListener.getDX();
        }
    }
}
