package tann.rgbca.calculator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.CpuSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class ShaderCalculator {

    FrameBuffer buffer;
    Texture previous;
    Batch batch;
    String folderName;
    ShaderProgram sp;
    long lastModified;
    int seed;
    float mult = .2f;

    public ShaderCalculator(String folderName, int size) {
        this(folderName, size, size);
    }

    public ShaderCalculator(String folderName, int width, int height) {
        this.folderName = folderName;
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        previous = buffer.getColorBufferTexture();
        batch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(0,0,width,height);
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
        randomiseState();
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

        batch.begin();
        buffer.begin();
//        Gdx.gl.glClearColor(0, 0, 0, 1f);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setShader(sp);
        setUniforms(sp);

        batch.draw(previous,
            0, 0,
            buffer.getWidth(), buffer.getHeight(),
            0, 0,
            buffer.getWidth(), buffer.getHeight(),
            false, flip);

        batch.end();

        buffer.end();
        Texture result = buffer.getColorBufferTexture();
        previous = result;
        flip = true;
        randomise = false;
        return result;
    }

    private void setUniforms(ShaderProgram sp) {
        sp.setUniformf("u_t", Main.t);
        sp.setUniformf("u_mult", mult);
        sp.setUniformi("u_seed", seed);
        sp.setUniformi("u_randomise", randomise?1:0);
        sp.setUniformf("u_mloc", Utils.makeMouseVec(true));
        sp.setUniformf("u_screen", new Vector2(buffer.getWidth(), buffer.getHeight()));
        sp.setUniformf("u_ml", Gdx.input.isButtonPressed(0) ? 1 : 0);
        sp.setUniformf("u_mr", Gdx.input.isButtonPressed(1) ? 1 : 0);
    }

    public void setMultiplier(float mult) {
        this.mult = mult;
    }

    boolean randomise;
    public void randomiseState() {
        randomise = true;
    }

    public int getSeed() {
        return seed;
    }
}
