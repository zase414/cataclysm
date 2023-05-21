package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.Line;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;

public class FirstPersonScene extends Scene{

    public float[] vertexArray = {};
    private int[] elementArray = {}; // ONLY COUNTER-CLOCKWISE ORDER WORKS
    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    public FirstPersonScene() { }
    RayCaster rayCaster;
    Map map;
    Player player;
    int positionsSize = 3;
    int colorSize = 4;
    int floatSizeBytes = 4;

    @Override
    public void init() {

        // initialize the rayCaster
        rayCaster = new RayCaster(70,1000,400, 150);

        // initialize the map
        map = new Map("C:\\Users\\zas\\IdeaProjects\\cataclysm\\assets\\maps\\testmap.json");
        map.compile();

        // initialize the player
        player = new Player(map);
        player.collisionBox.size = 0.5f;

        // initialize the camera
        camera = new Camera(new Vector2f());

        // import and compile the shaders
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        // ---------------------------------
        // generate VAO, VBO, EBO and send them to GPU
        // ---------------------------------

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip(); // '.flip()' makes OpenGL understand it correctly or something

        // create VBO upload vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        // create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        // create EBO upload element buffer
        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);

        // add vertex attribute pointers

        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

        // position attributes
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        // color attributes
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }
    @Override
    public void update(float dt) {

        {
            // player view angle
            player.viewAngle -= Settings.mouseSensitivity * MouseListener.getDX();

            // vector of movement
            double playerDX = 0.0;
            double playerDY = 0.0;
            double strafeMultiplier = 1.0;
            boolean playerIsStrafing = (KeyListener.isKeyPressed(GLFW_KEY_W) || KeyListener.isKeyPressed(GLFW_KEY_S)) && (KeyListener.isKeyPressed(GLFW_KEY_D) || KeyListener.isKeyPressed(GLFW_KEY_A));
            double movementMultipliers = player.speed * dt * strafeMultiplier;
            if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
                playerDX -= movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                playerDY -= movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle - 90));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle - 90));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle + 90));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle + 90));
            }

            if (playerIsStrafing) {
                playerDX *= Math.sqrt(2) / 2;
                playerDY *= Math.sqrt(2) / 2;
            }

            player.posX += playerDX;
            // update coordinates of the collision box
            player.collisionBox.update(player);
            if (player.collisionBox.checkForCollisionsPlayer(map, player)) {
                System.out.println(" in a wall");
                player.posX -= playerDX;
            }
            player.posY += playerDY;
            // update coordinates of the collision box
            player.collisionBox.update(player);
            if (player.collisionBox.checkForCollisionsPlayer(map, player)) {
                System.out.println(" in a wall");
                player.posY -= playerDY;
            }
            // update coordinates of the collision box
            player.collisionBox.update(player);

            // ==== debug ==== show speed
            System.out.println(Math.sqrt(playerDX * playerDX + playerDY  * playerDY));

        } // player movement

        {
            // update the rayCaster == update distances to walls
            rayCaster.cast(player, map);

            // update the vertexArray and elementArray using the rayCaster
            List<Float> vertexList = new ArrayList<>();
            List<Integer> elementList = new ArrayList<>();

            // declare background related lists
            List<Float> skyVertexList;
            List<Float> groundVertexList;
            List<Integer> skyElementList;
            List<Integer> groundElementList;

            // create background related vertex lists
            skyVertexList = skyVertexList(map);
            groundVertexList = groundVertexList(map);

            // create background related element lists
            skyElementList = skyElementList(0);
            groundElementList = groundElementList(Collections.max(skyElementList) + 1);

            // create wall related vertex and element lists
            List<Float> wallVertexList = wallVertexList(rayCaster);
            List<Integer> wallElementList = wallElementList(wallVertexList.size(), Collections.max(groundElementList) + 1);

            // add all the lists to the final array in the right order
            vertexList.addAll(skyVertexList);
            elementList.addAll(skyElementList);
            vertexList.addAll(groundVertexList);
            elementList.addAll(groundElementList);
            vertexList.addAll(wallVertexList);
            elementList.addAll(wallElementList);

            this.vertexArray = new float[vertexList.size()];

                for (int i = 0; i < vertexList.size(); i++) {
                    vertexArray[i] = vertexList.get(i);
                }

            this.elementArray = new int[elementList.size()];

                for (int i = 0; i < elementList.size(); i++) {
                    elementArray[i] = elementList.get(i);
                }

        } // vertex and element array build

        {
            // use the shader and upload values
            defaultShader.use();
            defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
            defaultShader.uploadMat4f("uView", camera.getViewMatrix());
            defaultShader.uploadFloat("uTime", Time.getTime());

            // ---------------------------------
            // generate VAO, VBO, EBO and send them to GPU
            // ---------------------------------

            glBindVertexArray(vaoID);

            // create a float buffer of vertices
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
            vertexBuffer.put(vertexArray).flip(); // '.flip()' makes OpenGL understand it correctly or something

            // create VBO upload vertex buffer
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

            // create the indices and upload
            IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
            elementBuffer.put(elementArray).flip();

            // create EBO upload element buffer
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);

            // add vertex attribute pointers
            int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

            // position attributes
            glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
            glEnableVertexAttribArray(0);

            // color attributes
            glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
            glEnableVertexAttribArray(1);

            // draw
            glDrawElements(GL_TRIANGLES, this.elementArray.length, GL_UNSIGNED_INT, 0);

            vertexArray = null;
            elementArray = null;

            // unbind everything
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glBindVertexArray(0);
            defaultShader.detach();

            glDeleteBuffers(vaoID);
            glDeleteBuffers(vboID);
            glDeleteBuffers(eboID);

        } // graphics
        MouseListener.endFrame();
    }

    public List<Float> wallVertexList(RayCaster rayCaster) {

        List<Float> vertexList = new ArrayList<>();

        /*
                0-------1
                |       |
                |       |
                |       |
                |       |
                2-------3
        */

        // index
        int i = 0;
        for (Line ray:rayCaster.rays) {

            // i.e. if the ray intersected something
            if (ray.distanceToWall != 0.0f) {

                float screenPortion = (Window.get().width / (float) rayCaster.rayCount);
                float y = (float) (Window.get().height / 2) / ray.distanceToWall;
                float xl = (float) i * screenPortion - Window.get().width / 2.0f;
                float xr = (i + 1) * screenPortion - Window.get().width / 2.0f;

                // top left vertex (0)
                vertexList.add(xl);      //x
                vertexList.add(y);       //y
                vertexList.add(0.0f);    //z
                vertexList.add(ray.r);   //r
                vertexList.add(ray.g);   //g
                vertexList.add(ray.b);   //b
                vertexList.add(ray.a);    //a

                // top right vertex (1)
                vertexList.add(xr);      //x
                vertexList.add(y);       //y
                vertexList.add(0.0f);    //z
                vertexList.add(ray.r);   //r
                vertexList.add(ray.g);   //g
                vertexList.add(ray.b);   //b
                vertexList.add(ray.a);   //a

                // bottom left vertex (2)
                vertexList.add(xl);           //x
                vertexList.add(-y);           //y
                vertexList.add(0.0f);         //z
                vertexList.add(ray.r);        //r
                vertexList.add(ray.g);        //g
                vertexList.add(ray.b);        //b
                vertexList.add(ray.a);        //a

                // bottom right vertex (3)
                vertexList.add(xr);           //x
                vertexList.add(-y);           //y
                vertexList.add(0.0f);         //z
                vertexList.add(ray.r);        //r
                vertexList.add(ray.g);        //g
                vertexList.add(ray.b);        //b
                vertexList.add(ray.a);        //a
            }
            i++;
        }

        /*System.out.print("{");
        for (float f:vertexList) {
            System.out.print(f + "f, ");
        }
        System.out.print("}");*/

        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int highestPreviousElement) {

        // 2 triangles = 6 integers per 4 vertexes in the vertexArray

        List<Integer> elementList = new ArrayList<>();

        for (int i = 0; i < (wallVertexListLength / 28); i++) {
            // element number shift
            int v = 4 * i;
            elementList.add(highestPreviousElement + 0 + v);
            elementList.add(highestPreviousElement + 2 + v);
            elementList.add(highestPreviousElement + 1 + v);
            elementList.add(highestPreviousElement + 1 + v);
            elementList.add(highestPreviousElement + 2 + v);
            elementList.add(highestPreviousElement + 3 + v);
        }
       return elementList;
    }

    public List<Float> skyVertexList(Map map) {
        List<Float> skyVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // top left
        skyVertexList.add(xl);                //x
        skyVertexList.add(y);                 //y
        skyVertexList.add(0.0f);              //z
        skyVertexList.add(map.skyColor.r);    //r
        skyVertexList.add(map.skyColor.g);    //g
        skyVertexList.add(map.skyColor.b);    //b
        skyVertexList.add(map.skyColor.a);    //a

        // top right
        skyVertexList.add(xr);                //x
        skyVertexList.add(y);                 //y
        skyVertexList.add(0.0f);              //z
        skyVertexList.add(map.skyColor.r);    //r
        skyVertexList.add(map.skyColor.g);    //g
        skyVertexList.add(map.skyColor.b);    //b
        skyVertexList.add(map.skyColor.a);    //a

        // bottom left
        skyVertexList.add(xl);                                       //x
        skyVertexList.add(0.0f);                                     //y
        skyVertexList.add(0.0f);                                     //z
        skyVertexList.add(Math.max(map.skyColor.r - 0.2f, 0.0f));    //r
        skyVertexList.add(Math.max(map.skyColor.g - 0.2f, 0.0f));    //g
        skyVertexList.add(Math.max(map.skyColor.b - 0.2f, 0.0f));    //b
        skyVertexList.add(map.skyColor.r);                           //a

        // bottom right
        skyVertexList.add(xr);                                       //x
        skyVertexList.add(0.0f);                                     //y
        skyVertexList.add(0.0f);                                     //z
        skyVertexList.add(Math.max(map.skyColor.r - 0.2f, 0.0f));    //r
        skyVertexList.add(Math.max(map.skyColor.g - 0.2f, 0.0f));    //g
        skyVertexList.add(Math.max(map.skyColor.b - 0.2f, 0.0f));    //b
        skyVertexList.add(map.skyColor.a);                           //a

        return skyVertexList;
    }

    public List<Integer> skyElementList(int highestPreviousElement) {
        List<Integer> skyElementList = new ArrayList<>();
        skyElementList.add(highestPreviousElement + 0);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 3);
        return skyElementList;
    }
    public List<Float> groundVertexList(Map map) {
        List<Float> groundVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // top left
        groundVertexList.add(xl);                                          //x
        groundVertexList.add(0.0f);                                        //y
        groundVertexList.add(0.0f);                                        //z
        groundVertexList.add(Math.max(map.groundColor.r - 0.2f, 0.0f));    //r
        groundVertexList.add(Math.max(map.groundColor.g - 0.2f, 0.0f));    //g
        groundVertexList.add(Math.max(map.groundColor.b - 0.2f, 0.0f));    //b
        groundVertexList.add(map.groundColor.a);                           //a

        // top right
        groundVertexList.add(xr);                                          //x
        groundVertexList.add(0.0f);                                        //y
        groundVertexList.add(0.0f);                                        //z
        groundVertexList.add(Math.max(map.groundColor.r - 0.2f, 0.0f));    //r
        groundVertexList.add(Math.max(map.groundColor.g - 0.2f, 0.0f));    //g
        groundVertexList.add(Math.max(map.groundColor.b - 0.2f, 0.0f));    //b
        groundVertexList.add(map.groundColor.a);    //a

        // bottom left
        groundVertexList.add(xl);                   //x
        groundVertexList.add(-y);                   //y
        groundVertexList.add(0.0f);                 //z
        groundVertexList.add(map.groundColor.r);    //r
        groundVertexList.add(map.groundColor.g);    //g
        groundVertexList.add(map.groundColor.b);    //b
        groundVertexList.add(map.groundColor.r);    //a

        // bottom right
        groundVertexList.add(xr);                   //x
        groundVertexList.add(-y);                   //y
        groundVertexList.add(0.0f);                 //z
        groundVertexList.add(map.groundColor.r);    //r
        groundVertexList.add(map.groundColor.g);    //g
        groundVertexList.add(map.groundColor.b);    //b
        groundVertexList.add(map.groundColor.a);    //a

        return groundVertexList;
    }

    public List<Integer> groundElementList(int highestPreviousElement) {
        List<Integer> skyElementList = new ArrayList<>();
        skyElementList.add(highestPreviousElement + 0);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 3);
        return skyElementList;
    }
}