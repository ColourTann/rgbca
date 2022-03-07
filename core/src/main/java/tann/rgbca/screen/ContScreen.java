package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class ContScreen extends Screen {

    FrameBuffer fb;
    Texture previous;

    public ContScreen() {
        fb = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        previous = fb.getColorBufferTexture();
        setTransform(false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram.pedantic = false;
        ShaderProgram sp = Utils.makeShader("cont");
        if(!sp.isCompiled()) {
            System.out.println(sp.getLog());
            return;
        }
        Texture result = null;
        batch.flush();
        for(int i=0;i<1;i++) {
            fb.begin();

            batch.setShader(sp);
            sp.setUniformf("u_t", Main.t);
            sp.setUniformf("u_mloc", Utils.makeMouseVec(true));
            sp.setUniformf("u_screen", Utils.makeScreenVec());
            sp.setUniformf("u_ml", Gdx.input.isButtonPressed(0) ? 1 : 0);
            sp.setUniformf("u_mr", Gdx.input.isButtonPressed(1) ? 1 : 0);
            sp.setUniformf("u_mm", Gdx.input.isButtonPressed(2) ? 1 : 0);

            batch.draw(previous, 0, 0, getWidth(), getHeight(), 0, 0, (int) getWidth(), (int) getHeight(), false, true);
            batch.flush();

            fb.end();
            result = fb.getColorBufferTexture();
            previous = result;
        }

        batch.draw(result, 0, 0, getWidth(), getHeight(), 0, 0, (int)getWidth(), (int)getHeight(), false, true);
        super.draw(batch, parentAlpha);
    }
}
