package tann.rgbca.calculator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class ShaderCalculator {

    FrameBuffer fb;
    Texture previous;
    SpriteBatch sb;
    String folderName;
    ShaderProgram sp;
    long lastModified;
    int seed;

    public ShaderCalculator(String folderName, int size) {
        this(folderName, size, size);
    }

    public ShaderCalculator(String folderName, int width, int height) {
        this.folderName = folderName;
        fb = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        previous = fb.getColorBufferTexture();
        sb = new SpriteBatch();
        sb.getProjectionMatrix().setToOrtho2D(0,0,width,height);
        ShaderProgram.pedantic = false;
        reseed();
        compileShader();
    }

    public void reseed() {
        reseed((int) (Math.random()*9999999));
    }

    public void reseed(int num) {
        this.seed = num;
        System.out.println("seed: " +seed);
    }

    public void pasteFolder() {
        folderName = Gdx.app.getClipboard().getContents();
        compileShader();
    }

    private void compileShader() {
        if(sp != null) sp.dispose();
        ShaderProgram tmp = Utils.makeShader(folderName);
        if(tmp != null) {
            sp = tmp;
            lastModified = Utils.lastModified(folderName);
        }
    }

    boolean flip;
    public Texture pasteTexture() {
        FileHandle fh = Gdx.files.internal("images/"+Gdx.app.getClipboard().getContents()+".png");
        if(fh.exists()) {
            previous = new Texture(fh);
            flip = false;
            return previous;
        }
        return null;
    }

    public Texture nextFrame() {
        if(Utils.lastModified(folderName) != lastModified) {
            compileShader();
        }
        if(!sp.isCompiled()) {
            System.out.println(sp.getLog());
            return previous;
        }

        sb.begin();
        fb.begin();

        sb.setShader(sp);
        sp.setUniformf("u_t", Main.t);
        sp.setUniformi("u_seed", seed);
        sp.setUniformf("u_mloc", Utils.makeMouseVec(true));
        sp.setUniformf("u_screen", new Vector2(fb.getWidth(), fb.getHeight()));
        sp.setUniformf("u_ml", Gdx.input.isButtonPressed(0) ? 1 : 0);
        sp.setUniformf("u_mr", Gdx.input.isButtonPressed(1) ? 1 : 0);
        sp.setUniformf("u_mm", Gdx.input.isButtonPressed(2) ? 1 : 0);

        sb.draw(previous, 0, 0, fb.getWidth(), fb.getHeight(), 0, 0,
            fb.getWidth(), fb.getHeight(),
            false, flip);
        sb.end();

        fb.end();
        Texture result = fb.getColorBufferTexture();
        previous = result;
        flip = true;
        return result;
    }
}
