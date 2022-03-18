package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;

public abstract class Screen extends Group {

    public Screen() {
        setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setTransform(false);
    }

    public Screen copy() {
        try {
            return (Screen) getClass().getConstructors()[0].newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TestScreen();
    }
}
