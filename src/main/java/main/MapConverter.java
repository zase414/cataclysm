package main;

import org.joml.Vector3f;
import util.Color;
import util.WallData;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapConverter {
    public List<String> generateOccurrenceList(String regex, String input) {
        // match the color lines
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // create a list with all the lines
        List<String> occurrences = new ArrayList<>();
        while (matcher.find()) {
            String occurrence = matcher.group();
            occurrences.add(occurrence);
        }
        return occurrences;
    }

    public String findSingularOccurrence(String regex, String input) {
        Pattern widthPattern = Pattern.compile(regex);
        Matcher widthMatcher = widthPattern.matcher(input);
        String output = "";
        while (widthMatcher.find()) {
            output = widthMatcher.group();
        }
        return output;
    }

    public List<WallData> extractWallObjsFromFile(String filepath) {
        List<WallData> wallDataList = new ArrayList<>();

        try {
            Path file = Path.of(filepath);
            String input = Files.readString(file);

            List<String> colorOccurrenceList = generateOccurrenceList("[{].{6}[}{]{2}rgb[}{]{2}.+[}]", input);

            // create a hashmap to identify the colors later
            HashMap<String, Vector3f> colorMap = new HashMap<>();
            for (String occurrence : colorOccurrenceList) {
                // get the key
                String key = occurrence.substring(1, 7);
                // get the part with rgb values
                Pattern rgbPattern = Pattern.compile("\\d[.]?\\d*,\\d[.]?\\d*,\\d[.]?\\d*");
                Matcher rgbMatcher = rgbPattern.matcher(occurrence);
                String rgbString = "";
                while (rgbMatcher.find()) {
                    rgbString = rgbMatcher.group();
                }
                // split the values based on commas
                String[] valueArray = rgbString.split(",");
                float r = Float.parseFloat(valueArray[0]);
                float g = Float.parseFloat(valueArray[1]);
                float b = Float.parseFloat(valueArray[2]);
                // create a Vector3f with the values and put it into the map
                Vector3f rgbValues = new Vector3f(r, g, b);
                colorMap.put(key, rgbValues);
            }

            List<String> wallOccurrenceList = generateOccurrenceList("draw .line width.+;", input);
            for (String occurrence : wallOccurrenceList) {
                float wallHeight = Float.parseFloat(findSingularOccurrence("width=\\d[.]?\\d*", occurrence).replace("width=", ""));
                String[] firstPoint = findSingularOccurrence("] [(].?\\d+[.]?\\d*,.?\\d+[.]?\\d*", occurrence).replace("] (", "").split(",");
                float x1 = Float.parseFloat(firstPoint[0]);
                float y1 = Float.parseFloat(firstPoint[1]);
                String[] secondPoint = findSingularOccurrence("-- [(].?\\d+[.]?\\d*,.?\\d+[.]?\\d*", occurrence).replace("-- (", "").split(",");
                float x2 = Float.parseFloat(secondPoint[0]);
                float y2 = Float.parseFloat(secondPoint[1]);
                String colorKey = findSingularOccurrence("color=.{6}", occurrence).replace("color=", "");
                Color color;
                Vector3f colorData = colorMap.get(colorKey);
                if (colorKey.isEmpty()) {
                    color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    color = new Color(colorData.x, colorData.y, colorData.z, 1.0f);
                }

                //System.out.println("(x1,y1): " + x1 + "; " + y1 + " (x2,y2): " + x2 + "; " + y2 + " color: " + color.r + " " + color.g + " " + color.b + " " + color.a + " height " + wallHeight);

                wallDataList.add(new WallData(x1, y1, x2, y2, color, wallHeight, 0.0f));
            }
        } catch (IOException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }

        return wallDataList;
    }

    public void exportMap(String inputFilepath, String exportFilepath, float spawnX, float spawnY, Color skyColor, Color groundColor, float spawnAngle, float spawnHeight) {
        String head = "{\n" +
                "  \"spawnpoint\": {\n" +
                "    \"x\": " + spawnX + ",\n" +
                "    \"y\": " + spawnY + "\n" +
                "  },\n" +
                "  \"sky_color\": {\n" +
                "    \"r\": " + skyColor.r + ",\n" +
                "    \"g\": " + skyColor.g + ",\n" +
                "    \"b\": " + skyColor.b + ",\n" +
                "    \"a\": " + skyColor.a + "\n" +
                "  },\n" +
                "  \"ground_color\": {\n" +
                "    \"r\": " + groundColor.r + ",\n" +
                "    \"g\": " + groundColor.g + ",\n" +
                "    \"b\": " + groundColor.b + ",\n" +
                "    \"a\": " + groundColor.a + "\n" +
                "  },\n" +
                "  \"spawn_view_angle\": " + spawnAngle + ",\n" +
                "  \"spawn_height\": " + spawnHeight + ",\n" +
                "  \"walls\": [\n";
        String foot = "  ]\n" +
                "}";
        String output;

        List<WallData> wallData = extractWallObjsFromFile(inputFilepath);

        output = head;
        int index = 0;
        for (WallData w : wallData) {
            String wallString = toJSON(w);
            if (!(index == 0)) output = output.concat(",");
            output = output.concat(wallString);
            index++;
        }
        output = output.concat(foot);

        try {
            File export = new File(exportFilepath);
            FileWriter fw = new FileWriter(export.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(output);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toJSON(WallData wallData) {
        return "{\n" +
                "  \"x1\": " + wallData.x1 + ",\n" +
                "  \"y1\": " + wallData.y1 + ",\n" +
                "  \"x2\": " + wallData.x2 + ",\n" +
                "  \"y2\": " + wallData.y2 + ",\n" +
                "  \"r\": " + wallData.color.r + ",\n" +
                "  \"g\": " + wallData.color.g + ",\n" +
                "  \"b\": " + wallData.color.b + ",\n" +
                "  \"a\": " + wallData.color.a + ",\n" +
                "  \"hmax\": " + wallData.topHeight + ",\n" +
                "  \"hmin\": " + wallData.botHeight + "\n" +
                "}";
    }

    public File chooseImportFile() {
        File file = null;
        try {
            FileDialog dialog = new FileDialog((Frame)null, "Select a PGF/TikZ file");
            dialog.setFile("*.txt");
            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory("assets/import");
            dialog.setVisible(true);
            file = dialog.getFiles()[0];
            dialog.dispose();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No file loaded.");
        }
        return file;
    }

    public void run() {
        // WHERE TO IMPORT FROM
        File file = chooseImportFile();
        final String inputFilepath;
        if (file == null) {
            return;
        } else inputFilepath = file.getAbsolutePath();

        // WHERE TO EXPORT TO
        final String exportFilepath = "assets/maps/conversion_example.json";

        // SETTINGS THAT CAN'T BE IMPORTED
        final float spawnX = 0.0f, spawnY = 0.0f;
        final Color skyColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
        final Color groundColor = new Color(0.4f, 0.4f, 0.4f, 1.0f);
        final float spawnAngle = 0.0f;
        final float spawnHeight = 0.0f;

        exportMap(inputFilepath, exportFilepath, spawnX, spawnY, skyColor, groundColor, spawnAngle, spawnHeight);

        System.out.println("Data successfully exported to " + exportFilepath);
    }
}