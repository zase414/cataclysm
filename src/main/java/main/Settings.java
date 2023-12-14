package main;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class Settings {
    // input
    static float mouseSensitivity;

    // graphics
    static float renderDistance;
    public static float fadeOutDistance;
    static float fov;
    static int rayCount;
    static boolean mapFullyVisible;
    static boolean cameraFollowsPlayer;

    // movement
    static boolean flappyBird;

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
        Settings.renderDistance = newSettings.renderDistance;
        Settings.fadeOutDistance = newSettings.fadeOutDistance;
        Settings.fov = newSettings.fov;
        Settings.rayCount = newSettings.rayCount;
        Settings.mapFullyVisible = newSettings.mapFullyVisible;
        Settings.cameraFollowsPlayer = newSettings.cameraFollowsPlayer;

        // movement
        Settings.flappyBird = newSettings.flappyBird;
    }
}