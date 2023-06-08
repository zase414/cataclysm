package main;

import org.joml.Vector2d;
import org.joml.Vector2f;
import util.CollisionBox;
import util.Line;
import util.Ray;
import util.Wall;

import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Player {
    public float posX;
    public float posY;
    public float posZ;
    public float height = 1.5f;
    public float viewAngle;
    public float speed = 10.0f;

    public CollisionBox collisionBox = new CollisionBox();
    public Vector2d movementVector = new Vector2d(0.0, 0.0);
    public Player (Map map) {
        this.posX = map.spawnPoint.x;
        this.posY = map.spawnPoint.y;
        this.viewAngle = map.spawnViewAngle;
        this.posZ = map.spawnHeight;
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
    public void handlePlayerMovement(float dt, Map map) {
        Vector2d dPos = new Vector2d(0.0, 0.0);
        double strafeMultiplier = 1.0;
        boolean playerIsStrafing = (KeyListener.keyBeingPressed(GLFW_KEY_W) || KeyListener.keyBeingPressed(GLFW_KEY_S)) && (KeyListener.keyBeingPressed(GLFW_KEY_D) || KeyListener.keyBeingPressed(GLFW_KEY_A));
        double movementMultipliers = speed * dt * strafeMultiplier;

        if (KeyListener.keyBeingPressed(GLFW_KEY_W)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle));
        }
        if (KeyListener.keyBeingPressed(GLFW_KEY_S)) {
            dPos.x -= movementMultipliers * Math.sin(Math.toRadians(viewAngle));
            dPos.y -= movementMultipliers * Math.cos(Math.toRadians(viewAngle));
        }
        if (KeyListener.keyBeingPressed(GLFW_KEY_A)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle - 90));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle - 90));
        }
        if (KeyListener.keyBeingPressed(GLFW_KEY_D)) {
            dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle + 90));
            dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle + 90));
        }

        // correct strafing speed
        if (playerIsStrafing) {
            dPos.x *= Math.sqrt(2) / 2;
            dPos.y *= Math.sqrt(2) / 2;
        }

        posX += dPos.x;
        // update X coordinate of the collision box
        updateCollisionBox();
        if (isColliding(map.walls)) {
            posX -= dPos.x;
            dPos.x = 0.0f;
        }

        posY += dPos.y;
        // update Y coordinate of the collision box
        updateCollisionBox();
        if (isColliding(map.walls)) {
            posY -= dPos.y;
            dPos.y = 0;
        }

        updateCollisionBox();
        movementVector = dPos;
    }
    public void updateViewAngle() {
        if (glfwGetInputMode(Window.get().glfwWindow, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            viewAngle -= Settings.mouseSensitivity * MouseListener.getDX();
        }
    }
    public boolean isColliding(HashSet<Wall> walls) {
        boolean isClipping = false;
        for (Line bound:collisionBox.bounds) {
            for (Wall wall:walls) {
                if (Ray.areIntersecting(bound, wall) && posZ < wall.topHeight && posZ + height > wall.botHeight) {
                    isClipping = true;
                }
            }
        }
        return isClipping;
    }
    public float jumpPhase = 2.0f;
    public void checkForJump(float dt) {
        final float jumpHeight = 1.5f;
        final float jumpSpeed = 24.0f;

        posZ = - jumpSpeed * jumpPhase * jumpPhase + jumpHeight;
        jumpPhase += dt;
        if (jumpPhase > 0.25f) {
            posZ = 0.0f;
        }
    }
}
