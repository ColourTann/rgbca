package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import tann.rgbca.Main;
import tann.rgbca.calculator.ShaderCalculator;

public class GenericScreen extends Screen {

    ShaderCalculator shaderCalculator;
    String folderName;
    int scale;
    int frames = 1;
    public GenericScreen(String folderName, int inScale) {
        this(folderName, inScale, null);
    }
    public GenericScreen(String folderName, int inScale, Integer seed) {
        super();
        this.folderName = folderName;
        this.scale = inScale;
        shaderCalculator = new ShaderCalculator(folderName, Gdx.graphics.getWidth()/inScale, Gdx.graphics.getHeight()/inScale);
        if(seed != null) {
            shaderCalculator.reseed(seed);
        }
        addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(button == 2) {
                    shaderCalculator.randomiseState();
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
                switch (keycode) {
                    case Input.Keys.PLUS: case Input.Keys.EQUALS: {
                        if(shift) {
                            setPixelScale(scale+1);
                        } else {
                            frames++;
                        }
                    } break;
                    case Input.Keys.MINUS: {
                        if(shift) {
                            setPixelScale(Math.max(1, scale-1));
                        } else {
                            frames = Math.max(1, frames-1);
                        }
                    } break;
                    case Input.Keys.S:
                        shaderCalculator.pasteFolder();
                        GenericScreen.this.folderName = Gdx.app.getClipboard().getContents();
                        break;
                    case Input.Keys.R:
                        if(shift) {
                            try {
                                int i = Integer.parseInt(Gdx.app.getClipboard().getContents());
                                shaderCalculator.reseed(i);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            shaderCalculator.reseed();
                        }
                        break;
                    case Input.Keys.T: {
                        shaderCalculator.pasteTexture();
                    } break;
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    private void setPixelScale(int i) {
        this.scale = i;
        Main.self().setScreen(copy());
    }

    @Override
    public Screen copy() {
        return new GenericScreen(folderName, scale, shaderCalculator.getSeed());
    }

    @Override
    public void act(float delta) {
        if(Gdx.input.isButtonPressed(3)) {
            float ratio = Gdx.input.getX()/getWidth();
            float mult = Interpolation.linear.apply(0, 1, ratio);
            shaderCalculator.setMultiplier(mult);
        }
        super.act(delta);
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
