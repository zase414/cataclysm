package main;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class Settings {
    // input
    static float mouseSensitivity;

    // graphics
    static int windowWidth;
    static int windowHeight;
    static float renderDistance;
    public static float fadeOutDistance;
    static float fov;
    static int rayCount;
    static boolean mapFullyVisible;
    static boolean cameraFollowsPlayer;

    // movement
    static boolean flappyBird;

    // keybinds
    static int forward;
    static int backward;
    static int right;
    static int left;
    static int sprint;
    static int jump;
    static int map;
    static int cursor;
    static int startMap;
    static int importPGFTikZ;
    static int changeScene;


    public static void loadSettingsFromJsonFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            SettingsData newSettings = gson.fromJson(reader, SettingsData.class);
            updateSettings(newSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateSettings(SettingsData newSettings) {
        // input
        Settings.mouseSensitivity = newSettings.mouseSensitivity;

        // graphics
        Settings.windowHeight = newSettings.windowHeight;
        Settings.windowWidth = newSettings.windowWidth;
        Settings.renderDistance = newSettings.renderDistance;
        Settings.fadeOutDistance = newSettings.fadeOutDistance;
        Settings.fov = newSettings.fov;
        Settings.rayCount = newSettings.rayCount;
        Settings.mapFullyVisible = newSettings.mapFullyVisible;
        Settings.cameraFollowsPlayer = newSettings.cameraFollowsPlayer;

        // movement
        Settings.flappyBird = newSettings.flappyBird;

        // keybinds
        Settings.forward = newSettings.forward;
        Settings.backward = newSettings.backward;
        Settings.right = newSettings.right;
        Settings.left = newSettings.left;
        Settings.jump = newSettings.jump;
        Settings.map = newSettings.map;
        Settings.cursor = newSettings.cursor;
        Settings.startMap = newSettings.startMap;
        Settings.importPGFTikZ = newSettings.importPGFTikZ;
        Settings.sprint = newSettings.sprint;
        Settings.changeScene = newSettings.changeScene;
    }
}