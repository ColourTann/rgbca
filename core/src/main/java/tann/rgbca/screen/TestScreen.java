package tann.rgbca.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import space.earlygrey.shapedrawer.ShapeDrawer;
import tann.rgbca.Main;

public class TestScreen extends Screen{

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer sd = Main.self().getSD();
        sd.setColor(1,1,1,1);
        sd.filledRectangle(0,0,getWidth(),getHeight());
//        sd.rectangle(Gdx.input.getX(), 5, 5,5);
        super.draw(batch, parentAlpha);
    }
}
