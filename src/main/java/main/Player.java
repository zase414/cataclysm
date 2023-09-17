package main;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector4d;
import util.CollisionBox;
import util.Line;
import util.Ray;
import util.Wall;

import java.util.HashSet;

import static main.Intersecter.areIntersecting;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Player {
    public float posX;
    public float posY;
    public float posZ;
    public float height = 1.5f;
    public float viewAngle;
    public float speed = 4.0f;
    Vector4d k  = new Vector4d(0.0, 0.0, 0.0, 0.0);

    public CollisionBox collisionBox = new CollisionBox();
    public Vector3d inertia = new Vector3d();
    public Player (Map map) {
        this.posX = map.spawnPoint.x;
        this.posY = map.spawnPoint.y;
        this.viewAngle = map.spawnViewAngle;
        this.posZ = map.spawnHeight;
        this.collisionBox.size = 0.2f;
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
    public boolean isInAir;
    public void handlePlayerMovement(double dt, Map map) {

        Vector3d dPos = new Vector3d(0.0, 0.0, 0.0);
        double strafeMultiplier = 1.0;

        boolean playerIsStrafing = (KeyListener.keyBeingPressed(GLFW_KEY_W) || KeyListener.keyBeingPressed(GLFW_KEY_S)) && (KeyListener.keyBeingPressed(GLFW_KEY_D) || KeyListener.keyBeingPressed(GLFW_KEY_A));

        if (playerIsStrafing) {
            strafeMultiplier = Math.sqrt(2) / 2;
        }

        double friction = 4.0;

        double movementMultipliers = speed * strafeMultiplier * dt;


        if (KeyListener.keyBeingPressed(GLFW_KEY_W)) {

            k.x = Math.min(1, k.x + friction*dt);
        } else k.x = Math.max(0, k.x-friction*dt);
        dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle)) * k.x;
        dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle)) * k.x;
        if (KeyListener.keyBeingPressed(GLFW_KEY_S)) {

            k.y = Math.min(1, k.y + friction*dt);
        } else k.y = Math.max(0, k.y-friction*dt);
        dPos.x -= movementMultipliers * Math.sin(Math.toRadians(viewAngle)) * k.y;
        dPos.y -= movementMultipliers * Math.cos(Math.toRadians(viewAngle)) * k.y;
        if (KeyListener.keyBeingPressed(GLFW_KEY_A)) {

            k.z = Math.min(1, k.z + friction*dt);
        } else k.z = Math.max(0, k.z-friction*dt);
        dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle - 90)) * k.z;
        dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle - 90)) * k.z;
        if (KeyListener.keyBeingPressed(GLFW_KEY_D)) {

            k.w = Math.min(1, k.w + friction*dt);
        } else k.w = Math.max(0, k.w-friction*dt);
        dPos.x += movementMultipliers * Math.sin(Math.toRadians(viewAngle + 90)) * k.w;
        dPos.y += movementMultipliers * Math.cos(Math.toRadians(viewAngle + 90)) * k.w;


        // update X coordinate
        posX += dPos.x;
        updateCollisionBox();
        if (isColliding(map.walls)) {
            posX -= dPos.x;
            dPos.x = 0.0f;
        }

        // update Y coordinate
        posY += dPos.y;
        updateCollisionBox();
        // if it causes a collision, revert it and nullify the vector
        if (isColliding(map.walls)) {
            posY -= dPos.y;
            dPos.y = 0.0f;
        }

        // update Z coordinate
        double phaseStart = jumpPhase;
        double phaseEnd = phaseStart + dt;
        dPos.z = getPhaseHeight(phaseEnd) - getPhaseHeight(phaseStart);
        posZ += dPos.z;
        updateCollisionBox();
        // if it causes a collision, revert it and nullify the phase
        if (isColliding(map.walls)) {
            isInAir = false;
            posZ = contactZ;
            jumpPhase = 0.0f;
        } else if (posZ < 0.0f) {
            isInAir = false;
            posZ = 0.0f;
            jumpPhase = 0.0f;
        } else isInAir = true;
        jumpPhase += dt;

        updateCollisionBox();

        inertia = dPos;
    }
    public void updateViewAngle() {
        if (glfwGetInputMode(Window.get().glfwWindow, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            viewAngle -= Settings.mouseSensitivity * MouseListener.getDX();
        }
    }
    float contactZ;
    public boolean isColliding(HashSet<Wall> walls) {
        boolean isClipping = false;
        for (Line bound:collisionBox.bounds) {
            for (Wall wall:walls) {
                if (areIntersecting(bound, wall) && posZ < wall.topHeight && posZ + height > wall.botHeight) {
                    isClipping = true;
                    if (posZ > wall.botHeight) {
                        contactZ = wall.topHeight;
                    } else contactZ = wall.botHeight - height;
                }
            }
        }
        return isClipping;
    }
    public float jumpPhase = 0.0f;
    public float minPhase = -0.25f;
    final float maxPhase = - minPhase;
    final float velocity = 24.0f;
    public double getPhaseHeight(double x) {
        return - velocity * (x*x);
    }
}
