package main;

import org.joml.Vector2f;
import util.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import util.Wall;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class Map {
    public String filepath;
    public HashSet<Wall> walls = new HashSet<>();
    public Vector2f spawnPoint = new Vector2f();
    public float spawnViewAngle;
    public Color skyColor = new Color();
    public Color groundColor = new Color();
    public int lastWallID;
    public Map (String filepath) {
        this.filepath = filepath;
    }

    public void compile() {
        try {
            // read the JSON file
            FileReader fileReader = new FileReader(filepath);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(fileReader).getAsJsonObject();

            // extract spawnpoint coordinates
            JsonObject spawnpointObj = jsonObject.getAsJsonObject("spawnpoint");
            spawnPoint.x = spawnpointObj.get("x").getAsFloat();
            spawnPoint.y = spawnpointObj.get("y").getAsFloat();

            // extract the background colors
            JsonObject skyColorObj = jsonObject.getAsJsonObject("sky_color");
            skyColor.r = skyColorObj.get("r").getAsFloat();
            skyColor.g = skyColorObj.get("g").getAsFloat();
            skyColor.b = skyColorObj.get("b").getAsFloat();
            skyColor.a = skyColorObj.get("a").getAsFloat();
            JsonObject groundColorObj = jsonObject.getAsJsonObject("ground_color");
            groundColor.r = groundColorObj.get("r").getAsFloat();
            groundColor.g = groundColorObj.get("g").getAsFloat();
            groundColor.b = groundColorObj.get("b").getAsFloat();
            groundColor.a = groundColorObj.get("a").getAsFloat();


            // print the spawn coordinates
            System.out.println("Spawn coords: " + spawnPoint.x + "; " + spawnPoint.y);

            // extract spawn view angle
            this.spawnViewAngle = jsonObject.get("spawn_view_angle").getAsFloat();

            // print the spawn_view_angle
            System.out.println("Spawn View Angle: " + spawnViewAngle);

            // extract walls coordinates
            JsonArray wallsArray = jsonObject.getAsJsonArray("walls");
            int id = 0;
            for (int i = 0; i < wallsArray.size(); i++) {
                JsonObject wallObj = wallsArray.get(i).getAsJsonObject();

                float x1 = wallObj.get("x1").getAsFloat();
                float y1 = wallObj.get("y1").getAsFloat();
                float x2 = wallObj.get("x2").getAsFloat();
                float y2 = wallObj.get("y2").getAsFloat();

                Wall wall = new Wall(x1, y1, x2, y2);



                float r = wallObj.get("r").getAsFloat();
                float g = wallObj.get("g").getAsFloat();
                float b = wallObj.get("b").getAsFloat();
                float a = wallObj.get("a").getAsFloat();
                wall.color = new Color(r, g, b, a);

                float h = wallObj.get("height").getAsFloat();
                wall.height = h;

                // ==== debug ====
                //System.out.println("Line coordinates " + (i + 1) + ": (" + wall.x1 + ", " + wall.y1 + ", " + wall.x2 + ", " + wall.y2 + ")");
                wall.id = id;
                lastWallID = id;
                walls.add(wall);
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: could not open file for map: '" + filepath + "'";
        }
    }
}
