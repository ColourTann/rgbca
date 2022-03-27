package tann.rgbca.calculator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import tann.rgbca.Utils;

public class SafetyCalculator {

    FrameBuffer buffer;
    FrameBuffer swap;
    Texture previous;
    ShaderProgram sp;
    long lastModified;
    private Mesh mesh;
    int scale;

    public SafetyCalculator(int size) {
        this(size, size, 1);
    }

    public SafetyCalculator(int width, int height, int scale) {
        this.scale = scale;
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        swap = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        previous = buffer.getColorBufferTexture();
        ShaderProgram.pedantic = false;
        compileShader();
        mesh = createFullScreenQuadFromWIki();
    }

    public static Mesh createFullScreenQuadFromWIki() {

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

    private void compileShader() {
        if(sp != null) sp.dispose();
        ShaderProgram tmp = Utils.makeShader("safety");
        if(tmp != null) {
            sp = tmp;
            lastModified = Utils.lastModified("safety");
        }
    }

    public Texture nextFrame(Texture newTexture) {
        if(Utils.lastModified("safety") != lastModified) {
            compileShader();
        }
        if(!sp.isCompiled()) {
            System.out.println(sp.getLog());
            return previous;
        }
        sp.bind();
//        newTexture = copy(newTexture);
        newTexture.bind(0);
        previous.bind(1);
        sp.setUniformi("u_texture0", 0);
//        sp.setUniformi("u_texture1", 0);
        sp.setUniformi("u_texture1", 1);

        buffer.begin();
        mesh.render(sp, GL20.GL_TRIANGLE_FAN);
        buffer.end();
        Texture result = buffer.getColorBufferTexture();
        previous = result;
        swapBuffers();
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        return result;
    }

    private static Texture copy(Texture input) {
        FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, input.getWidth(), input.getHeight(), false);
        SpriteBatch sb = new SpriteBatch();
        fb.begin();
        sb.begin();
        sb.draw(input, 0, 0);
        sb.end();
        fb.end();
        return fb.getColorBufferTexture();
    }

    private void swapBuffers() {
        FrameBuffer t = buffer;
        buffer = swap;
        swap = t;
    }
}
