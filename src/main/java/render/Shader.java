package render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {

    private int shaderProgramID;
    private String vertexSource;
    private String fragmentSource;
    private String filepath;
    public Shader (String filepath) {
        this.filepath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // get the first pattern (vertex / fragment)
            int index = source.indexOf("#type") + 6; // index after "#type"
            int eol = source.indexOf("\r\n", index); // index at the end of line
            String firstPattern = source.substring(index,eol).trim(); // get the type, .trim() = remove whitespace

            // get the second pattern
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index,eol).trim();

            // assign source to 1st splitString
            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else throw new IOException("Unexpected token: '" + firstPattern);

            // assign source to 2nd splitString
            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else throw new IOException("Unexpected token: '" + secondPattern);

        } catch(IOException e) {
            e.printStackTrace();
            assert false : "Error: could not open file for shader: '" + filepath + "'";
        }



    }

    public void compile() {

    }

    public void use() {

    }

    public void detach() {

    }
}
