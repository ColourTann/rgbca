package tann.rgbca.calculator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class ShaderCalculator {

    FrameBuffer fb;
    Texture previous;
    SpriteBatch sb;
    final String folderName;
    public ShaderCalculator(String folderName, int size) {
        this.folderName = folderName;
        fb = new FrameBuffer(Pixmap.Format.RGBA8888, size, size, false);
        previous = fb.getColorBufferTexture();
        sb = new SpriteBatch();
    }

    public Texture nextFrame() {
        ShaderProgram.pedantic = false;
        ShaderProgram sp = Utils.makeShader(folderName);
        if(!sp.isCompiled()) {
            System.out.println(sp.getLog());
            return previous;
        }

        sb.begin();
        fb.begin();

        sb.setShader(sp);
        sp.setUniformf("u_t", Main.t);
        sp.setUniformf("u_mloc", Utils.makeMouseVec(true));
        sp.setUniformf("u_screen", Utils.makeScreenVec());
        sp.setUniformf("u_ml", Gdx.input.isButtonPressed(0) ? 1 : 0);
        sp.setUniformf("u_mr", Gdx.input.isButtonPressed(1) ? 1 : 0);
        sp.setUniformf("u_mm", Gdx.input.isButtonPressed(2) ? 1 : 0);

        sb.draw(previous, 0, 0, fb.getWidth(), fb.getHeight(), 0, 0,
            (int) fb.getWidth(), (int) fb.getHeight(),
            false, true);
        sb.end();

        fb.end();
        Texture result = fb.getColorBufferTexture();
        previous = result;
        return result;
    }
}
