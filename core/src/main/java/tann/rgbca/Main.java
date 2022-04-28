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
import tann.rgbca.screen.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Stage stage;
	private Screen current;
	public static float t;
	@Override
	public void create() {
		setupStage();
		setScreen(new GenericScreen("omni-mono", 1));
	}

	private void setupStage() {
		stage = new Stage();
		batch = (SpriteBatch)stage.getBatch();
		Gdx.input.setInputProcessor(stage);
	}

	public void setScreen(Screen screen) {
		if(current != null) {
			current.remove();
		}
		current = screen;
		stage.addActor(screen);
		stage.setKeyboardFocus(screen);
	}

	@Override
	public void render() {
		update(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	private void update(float deltaTime) {
		t += deltaTime;
		stage.act(deltaTime);
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

	@Override
	public void resize(int width, int height) {

		setupStage();
		if(current != null) {
			setScreen(current.copy());
		}
		super.resize(width, height);
	}

}