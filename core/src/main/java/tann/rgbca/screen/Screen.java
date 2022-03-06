package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;

public abstract class Screen extends Group {

    public Screen() {
        setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
