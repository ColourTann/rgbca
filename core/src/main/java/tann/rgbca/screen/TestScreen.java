package tann.rgbca.screen;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import space.earlygrey.shapedrawer.ShapeDrawer;
import tann.rgbca.Main;
import tann.rgbca.Utils;

public class TestScreen extends Screen{

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram.pedantic = false;
        ShaderProgram sp = Utils.makeShader("first");
        if(!sp.isCompiled()) {
            System.out.println(sp.getLog());
            return;
        }
        batch.setShader(sp);
        sp.setUniformf("u_t", Main.t);
        sp.setUniformf("u_mloc", Utils.makeMouseVec());
        sp.setUniformf("u_screen", Utils.makeScreenVec());
        ShapeDrawer sd = Main.self().getSD();
        sd.setColor(1,1,1,1);
        sd.filledRectangle(0,0,getWidth(),getHeight());
        super.draw(batch, parentAlpha);
    }
}
