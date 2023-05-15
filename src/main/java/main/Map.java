package main;

import util.Line;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class Map {
    private static Map instance;
    public static String filepath = "C:\\Users\\zas\\IdeaProjects\\cataclysm\\assets\\maps\\testmap.ccmap";
    public HashSet<Line> walls = new HashSet<>();
    public void compile() {

        String filepath = Map.filepath;

        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitLines = source.split("(/)");

            for (int i = 1; i < splitLines.length; i++) {

                String[] splitCoordinates = splitLines[i].split("; +");

                Line line = new Line();
                line.x1 = Float.parseFloat(splitCoordinates[0]);
                line.y1 = Float.parseFloat(splitCoordinates[1]);
                line.x2 = Float.parseFloat(splitCoordinates[2]);
                line.y2 = Float.parseFloat(splitCoordinates[3]);
                line.init();
                walls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: could not open file for map: '" + filepath + "'";
        }
    }
    public static Map get() {
        if (Map.instance == null) {
            Map.instance = new Map();
            instance.compile();
        }
        return Map.instance;
    }
    public static void clear() {
        Map.instance = null;
    }
}
