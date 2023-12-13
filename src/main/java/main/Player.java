package main;

import org.joml.Vector2f;
import org.joml.Vector3f;
import util.*;

import java.util.HashSet;

import static main.KeyListener.keyBeingPressed;
import static render.RayCaster.Intersecter.areIntersecting;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    public Coordinates coordinates = new Coordinates();
    public float posZ;
    public float height = 1.5f;
    public float viewAngle;
    public float speed = 4.0f;
    public Vector3f inertia = new Vector3f();
    private float[] keyPressVector = {0.0f, 0.0f, 0.0f, 0.0f};
    public CollisionBox collisionBox;
    private float friction;
    private float strafeSpeed;
    public Player (Map map) {
        this.coordinates.x = map.spawnPoint.x;
        this.coordinates.y = map.spawnPoint.y;
        this.viewAngle = map.spawnViewAngle;
        this.posZ = map.spawnHeight;
        this.collisionBox = new CollisionBox(0.2f, map.spawnPoint.x, map.spawnPoint.y);
        this.strafeSpeed = (float) Math.sqrt(2) / 2.0f;
        this.friction = 4.0f;
    }
    public Player (Map map, float playerSize) {
        this(map);
        this.collisionBox.size = playerSize;
    }
    public Player (float posX, float posY, float posZ, float viewAngle, CollisionBox collisionBox, float friction, float strafeSpeed) {
        this.coordinates.x = posX;
        this.coordinates.y = posY;
        this.posZ = posZ;
        this.viewAngle = viewAngle;
        this.collisionBox = collisionBox;
        this.friction = friction;
        this.strafeSpeed = strafeSpeed;
    }
    public boolean isStrafing() {
        return ((keyPressVector[0] + keyPressVector[1]) * (keyPressVector[2] + keyPressVector[3])) != 0;
    }
    public void updateCollisionBox(float dx, float dy) {
        collisionBox.update(dx, dy);
    }

    public void updateCollisionBox(Vector2f v) {
        collisionBox.update(v);
    }
    public boolean isInAir;
    public void handlePlayerMovement(float dt, Map map) {

        float friction = isInAir ? this.friction * 0.2f : this.friction;
        float speed = this.speed;
        float strafeSpeed = this.strafeSpeed;

        Vector3f dPos = getMovementVector(dt, friction, speed, strafeSpeed);

        applyMove(map, dPos.x, dPos.y);

        // process jumping
        float phaseStart = jumpPhase;
        float phaseEnd = phaseStart + dt;
        dPos.z = getPhaseHeight(phaseEnd) - getPhaseHeight(phaseStart);
        posZ += dPos.z;
        updateCollisionBox(0.0f, 0.0f);
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

        updateCollisionBox(0.0f, 0.0f);

        inertia = dPos;
    }

    public void applyMove(Map map, float dx, float dy) {

        for (Vector2f d : new Vector2f[]{new Vector2f(dx, 0f), new Vector2f(0f, dy)}) {

            // update collision box for each component
            updateCollisionBox(d);

            // if it causes a collision, revert it and nullify the vector
            if (!isColliding(map.walls)) {
                coordinates.add(d);
            } else updateCollisionBox(d.mul(-1.0f));
        }
    }

    public Vector3f getMovementVector(float dt, float friction, float speed, float strafeSpeed) {
        Vector3f dPos = new Vector3f();
        double strafeMultiplier;
        if (this.isStrafing()) {
            strafeMultiplier = strafeSpeed;
        } else strafeMultiplier = 1.0;

        double multiplierBase = speed * strafeMultiplier * dt;
        double[] movementMultipliers = {multiplierBase, -multiplierBase, multiplierBase, multiplierBase};
        int[] keys = {GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D};
        float[] angles = {viewAngle, viewAngle, viewAngle - 90f, viewAngle + 90f};
        for (int i = 0; i < keys.length; i++) {
            keyPressVector[i] = updateComponent(keyBeingPressed(keys[i]), keyPressVector[i], dt, friction);
            if (keyPressVector[i] > 0) {
                dPos.x += (float) ((float) movementMultipliers[i] * Math.sin(Math.toRadians(angles[i])) * keyPressVector[i]);
                dPos.y += (float) ((float) movementMultipliers[i] * Math.cos(Math.toRadians(angles[i])) * keyPressVector[i]);
            }
        }
        return dPos;
    }

    public static float updateComponent(boolean condition, float component, float dt, float friction) {
        if (condition) {
           return Math.min(1, component + friction * dt);
        } else return Math.max(0, component - friction * dt);
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
    public float getPhaseHeight(float x) {
        return - velocity * (x*x);
    }
}
