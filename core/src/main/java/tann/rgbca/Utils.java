package tann.rgbca;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public abstract class Utils {
    public static Vector2 makeMouseVec() {
        return new Vector2(
            Gdx.input.getX()/(float)Gdx.graphics.getWidth(),
            (Gdx.graphics.getHeight()-Gdx.input.getY())/(float)Gdx.graphics.getHeight()
        );
    }

    public static Vector2 makeScreenVec() {
        return new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static ShaderProgram makeShader(String folderName) {
        return new ShaderProgram(getVertexShader(folderName).readString(), getFragmentShader(folderName).readString());
    }

    private static FileHandle getFragmentShader(String folderName) {
        return Gdx.files.absolute("X:/code/workspace/rgbca/assets/shaders/"+folderName+"/fragment.glsl");
    }

    private static FileHandle getVertexShader(String folderName) {
        return Gdx.files.absolute("X:/code/workspace/rgbca/assets/shaders/"+folderName+"/vertex.glsl");
    }
}
