package tann.rgbca;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import space.earlygrey.shapedrawer.ShapeDrawer;
import tann.rgbca.screen.Screen;
import tann.rgbca.screen.TestScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture image;
	private Stage stage;
	public static float t;
	@Override
	public void create() {
		stage = new Stage();
		batch = (SpriteBatch)stage.getBatch();
		setScreen(new TestScreen());
		image = new Texture("libgdx.png");
	}

	private void setScreen(Screen screen) {
		stage.addActor(screen);
	}

	@Override
	public void render() {
		update(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
//		batch.begin();
//		batch.draw(image, 140, 210);
//		batch.end();
	}

	private void update(float deltaTime) {
		t += deltaTime;
	}

	@Override
	public void dispose() {
		batch.dispose();
		image.dispose();
	}

	public static Main self() {
		return (Main) Gdx.app.getApplicationListener();
	}

	ShapeDrawer sd;
	public ShapeDrawer getSD() {
		if(sd == null){
			Pixmap p = new Pixmap(1,1, Pixmap.Format.RGBA8888);
			p.setColor(Color.WHITE);
			p.drawPixel(0,0);
			Texture t = new Texture(p);
			TextureRegion tr = new TextureRegion(t, 0,0,1,1);
			sd = new ShapeDrawer(batch, tr);
		}
		return sd;
	}
}