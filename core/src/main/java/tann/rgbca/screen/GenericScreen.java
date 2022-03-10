package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import tann.rgbca.calculator.ShaderCalculator;

public class GenericScreen extends Screen {

    ShaderCalculator shaderCalculator;
    final String folderName;
    final int scale;
    int frames = 1;
    public GenericScreen(String folderName, int scale) {
        super();
        this.folderName = folderName;
        this.scale = scale;
        shaderCalculator = new ShaderCalculator(folderName, Gdx.graphics.getWidth()/scale, Gdx.graphics.getHeight()/scale);
        addListener(new InputListener(){

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.PLUS: case Input.Keys.EQUALS: frames++; break;
                    case Input.Keys.MINUS: frames--; break;
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    @Override
    public Screen copy() {
        return new GenericScreen(folderName, scale);
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(frames <= 0) return;
        batch.end();
        Texture t = null;
        for(int i=0;i<frames;i++) {
            t = shaderCalculator.nextFrame();
        }
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.begin();
        batch.draw(t, 0, 0, getWidth(), getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
        super.draw(batch, parentAlpha);
    }
}
