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
    float speed = 1;
    Texture t;
    public GenericScreen(String folderName, int inScale) {
        this(folderName, inScale, null, 1);
    }
    public GenericScreen(String folderName, int inScale, Integer seed, float speed) {
        super();
        this.folderName = folderName;
        this.scale = inScale;
        this.speed = speed;
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
                            changeSpeed(1);
                        }
                    } break;
                    case Input.Keys.MINUS: {
                        if(shift) {
                            setPixelScale(Math.max(1, scale-1));
                        } else {
                            changeSpeed(-1);
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
        t = shaderCalculator.nextFrame();
    }

    private void changeSpeed(int delta) {
        if(delta>0) {
            if (speed >= 1) {
                speed++;
            } else {
                speed *= 2;
            }
        } else {
            if (speed >= 2) {
                speed--;
            } else {
                speed /= 2;
            }
        }
    }

    private void setPixelScale(int i) {
        this.scale = i;
        Main.self().setScreen(copy());
    }

    @Override
    public Screen copy() {
        return new GenericScreen(folderName, scale, shaderCalculator.getSeed(), speed);
    }

    float ticker;

    @Override
    public void act(float delta) {
        ticker +=speed;
        if(Gdx.input.isButtonPressed(3)) {
            float ratio = Gdx.input.getX()/getWidth();
            float mult = Interpolation.linear.apply(0, 1, ratio);
            shaderCalculator.setMultiplier(mult);
        }
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        while(ticker>=1) {
            t = shaderCalculator.nextFrame();
            ticker--;
        }
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.begin();
        batch.draw(t, 0, 0, getWidth(), getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
        super.draw(batch, parentAlpha);
    }
}
