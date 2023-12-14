package main;
public class Main {
    public static void main(String[] args) {

        String jsonFilePath = "assets/settings.json";
        Settings.loadSettingsFromJsonFile(jsonFilePath);

        Window window = Window.get();
        window.run();
    }
}
