package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import tann.rgbca.calculator.ShaderCalculator;

public class GenericScreen extends Screen {

    ShaderCalculator shaderCalculator;
    final String folderName;
    final int scale;
    public GenericScreen(String folderName, int scale) {
        this.folderName = folderName;
        this.scale = scale;
        shaderCalculator = new ShaderCalculator(folderName, Gdx.graphics.getWidth()/scale);
    }

    @Override
    public Screen copy() {
        return new GenericScreen(folderName, scale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        Texture t = shaderCalculator.nextFrame();
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.begin();
        batch.draw(t, 0, 0, getWidth(), getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
        super.draw(batch, parentAlpha);
    }
}
