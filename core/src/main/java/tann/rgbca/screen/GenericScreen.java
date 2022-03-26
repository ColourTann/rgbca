package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import tann.rgbca.Main;
import tann.rgbca.calculator.SafetyCalculator;
import tann.rgbca.calculator.ShaderCalculator;

public class GenericScreen extends Screen {
    Texture chk;
    ShaderCalculator shaderCalculator;
    SafetyCalculator safetyCalculator;
    String folderName;
    int scale;
    float speed = 1;
    Texture calcTexture;
    public GenericScreen(String folderName, int inScale) {
        this(folderName, inScale, null, 1);
    }
    public GenericScreen(String folderName, int inScale, Integer seed, float speed) {
        super();
        this.folderName = folderName;
        this.scale = inScale;
        this.speed = speed;
        safetyCalculator = new SafetyCalculator(Gdx.graphics.getWidth()/inScale);
        shaderCalculator = new ShaderCalculator(folderName, Gdx.graphics.getWidth()/inScale, Gdx.graphics.getHeight()/inScale);
        if(seed != null) {
            shaderCalculator.reseed(seed);
        }
        addListener(new ClickListener(){



            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(button == 3) {
                    shaderCalculator.setMiddle();
                } else if(button == 2) {
                    shaderCalculator.randomiseState();
                } else if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    if(shaderCalculator.isShowMore()) {
                        shaderCalculator.addWeight(x/getWidth());
                        shaderCalculator.addWeight(y/getHeight());
                        shaderCalculator.randomiseState();
                    }
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
                    case Input.Keys.M:
                        shaderCalculator.toggleShowMore();
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
                    case Input.Keys.BACKSPACE: {
                        shaderCalculator.popWeight();
                        shaderCalculator.popWeight();
                        break;
                    }
                    case Input.Keys.ENTER: {
                        shaderCalculator.incReseeds();
                        shaderCalculator.randomiseState();
                        break;
                    }
                }
                return super.keyDown(event, keycode);
            }
        });
        calcTexture = shaderCalculator.nextFrame();

        FileHandle fh = Gdx.files.internal("images/checker.png");
        chk = new Texture(fh);

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
        System.out.println("new speed: "+speed);
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
        while(ticker>=1) {
            calcTexture = shaderCalculator.nextFrame();
            ticker--;
        }
        drawTexture = safetyCalculator.nextFrame(calcTexture);
        super.act(delta);
    }
    Texture drawTexture;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.flush();
        batch.draw(chk, 0, 0, getWidth(), getHeight());
        batch.flush();
        batch.draw(drawTexture, 0, 0, getWidth(), getHeight(), 0, 0, (int)getWidth(), (int)getHeight(), false, true);

        super.draw(batch, parentAlpha);
    }
}
