package tann.rgbca.calculator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class ShaderCalculator {

    FrameBuffer buffer1;
    FrameBuffer buffer2;
    Texture previous;
    Batch batch;
    String folderName;
    ShaderProgram sp;
    long lastModified;
    int seed;
    float mult = .2f;
    float mix = .5f;
    private Mesh mesh;

    public ShaderCalculator(String folderName, int size) {
        this(folderName, size, size);
    }

    public ShaderCalculator(String folderName, int width, int height) {
        this.folderName = folderName;
        buffer1 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        buffer2 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        previous = buffer1.getColorBufferTexture();
        batch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(0,0,width,height);
        ShaderProgram.pedantic = false;
        reseed();
        compileShader();
//        mesh = createFullScreenQuad();
//        mesh = fromLibgdxWiki();
        mesh = createFullScreenQuadFromWIki();
    }

    private Mesh createFullScreenQuad() {
        Mesh mesh = new Mesh( true, 4, 0,  // static mesh with 4 vertices and no indices
            new VertexAttribute( VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ),
            new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );
        float[] verts = new float[]{
            -1, -1, 0, 0, 0,
            1, -1, 0, 1, 0,
            1, 1, 0, 1, 1,
            -1, 1, 0, 0, 1
        };
        mesh.setVertices( verts );
        mesh.disableInstancedRendering();
        return mesh;
    }

    private Mesh createFullScreenQuadFromWIki() {

        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i++] = 1f; // v4

        Mesh mesh = new Mesh( true, 4, 0,  // static mesh with 4 vertices and no indices
            new VertexAttribute( VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ),
            new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );

        mesh.setVertices( verts );
        return mesh;
    }

    private Mesh fromLibgdxWiki() {
        Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[] {
            -0.5f, -0.5f, 0, 1, 1, 1, 1, 0, 1,
            0.5f, -0.5f, 0, 1, 1, 1, 1, 1, 1,
            0.5f, 0.5f, 0, 1, 1, 1, 1, 1, 0,
            -0.5f, 0.5f, 0, 1, 1, 1, 1, 0, 0
        });
        mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        return mesh;
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

    public Texture pasteTexture() {
        FileHandle fh = Gdx.files.internal("images/"+Gdx.app.getClipboard().getContents()+".png");
        if(fh.exists()) {
            previous = new Texture(fh);
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
        sp.bind();
        setUniforms(sp);
        previous.bind();
        sp.setUniformi("u_texture", 0);

        buffer1.begin();
        mesh.render(sp, GL20.GL_TRIANGLE_FAN);
        buffer1.end();
        Texture result = buffer1.getColorBufferTexture();
        previous = result;
        swapBuffers();
//        previous.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
//        previous.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        randomise = false;
        return result;
    }

    private void swapBuffers() {
        FrameBuffer tmp = buffer1;
        buffer1 = buffer2;
        buffer2 = tmp;
    }

    private void setUniforms(ShaderProgram sp) {
        sp.setUniformf("u_t", Main.t);
        sp.setUniformf("u_mult", mult);
        sp.setUniformf("u_mix", mix);
        sp.setUniformi("u_seed", seed);
        sp.setUniformi("u_randomise", randomise?1:0);
        sp.setUniformf("u_mloc", Utils.makeMouseVec(true));
        sp.setUniformi("u_screen", buffer1.getWidth(), buffer1.getHeight());
        sp.setUniformf("u_ml", Gdx.input.isButtonPressed(0) ? 1 : 0);
        sp.setUniformf("u_mr", Gdx.input.isButtonPressed(1) ? 1 : 0);
    }

    public void setMultiplier(float mult) {
        this.mult = mult;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    boolean randomise;
    public void randomiseState() {
        randomise = true;
    }

    public int getSeed() {
        return seed;
    }
}
