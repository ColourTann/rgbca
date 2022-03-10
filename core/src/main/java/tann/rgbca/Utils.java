package tann.rgbca;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public abstract class Utils {
    public static Vector2 makeMouseVec() {
        return makeMouseVec(true);
    }

    public static Vector2 makeMouseVec(boolean flip) {
        float y = (Gdx.graphics.getHeight()-Gdx.input.getY())/(float)Gdx.graphics.getHeight();
        if(!flip) y = (Gdx.input.getY())/(float)Gdx.graphics.getHeight();
        return new Vector2(
            Gdx.input.getX()/(float)Gdx.graphics.getWidth(),
            y
        );
    }

    public static Vector2 makeScreenVec() {
        return makeScreenVec(1);
    }

    public static Vector2 makeScreenVec(int scale) {
        return new Vector2((float)Gdx.graphics.getWidth()/scale, (float)Gdx.graphics.getHeight()/scale);
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

    public static long lastModified(String folderName) {
        return getFragmentShader(folderName).lastModified();
    }
}
