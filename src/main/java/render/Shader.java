package render;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

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
        int vertexID, fragmentID;
        // ---------------------------------
        // compile shaders
        // ---------------------------------

        // 1. load and compile the VERTEX shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // 2. pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // check for errors
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int length = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '"+ filepath + "'\n\tVertex shader compilation error.");
            System.out.println(glGetShaderInfoLog(vertexID, length));
            assert false: "";
        }

        // 1. load and compile the FRAGMENT shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // 2. pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // check for errors
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int length = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '"+ filepath + "'\n\tFragment shader compilation error.");
            System.out.println(glGetShaderInfoLog(fragmentID, length));
            assert false: "";
        }
        // ---------------------------------
        // link shaders
        // ---------------------------------

        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // check for linking errors
        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int length = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '"+ filepath +"'\n\tShader linking error.");
            System.out.println(glGetProgramInfoLog(shaderProgramID, length));
            assert false: "";
        }
    }

    public void use() {
        glUseProgram(shaderProgramID);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }
}
