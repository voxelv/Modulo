package com.derelictech.macromachine.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import com.badlogic.gdx.utils.viewport.*;
import com.derelictech.macromachine.util.Const;
import com.derelictech.macromachine.util.GridDirection;
import com.derelictech.macromachine.util.Level;
import com.derelictech.macromachine.util.Slot;

/**
 * Main Game Screen
 * @author Tim Slippy, voxelv
 * @author Nate Groggett, Ghost4dot2
 */
public class GameScreen extends AbstractGameScreen {

    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Slot selectedSlot;



    private Stage hud;
    private Skin skin;
    Texture texture1;
    Label value;
    public Button buttonMulti;
    public Button levelUp;

    private Table table;

    private InputMultiplexer multiplexer;

    private Level level;

    /**
     * Constructor for {@link GameScreen}
     * @param game The game to set. Used to switch screens.
     */
    public GameScreen(Game game) {
        super(game);
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new ExtendViewport(Const.VIEWPORT_W,Const.VIEWPORT_H, Const.VIEWPORT_W, Const.VIEWPORT_H, camera);
//        viewport= new FitViewport(Const.VIEWPORT_W, Const.VIEWPORT_H);
        stage = new Stage(viewport);
        camera.update();


        hud = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("uiskin.json"));

//        table = new Table();
//        table.setWidth(hud.getWidth());
//        table.align( Align.center|Align.top);
//        table.setPosition(0,Gdx.graphics.getHeight());

        buttonMulti = new TextButton("test", skin, "toggle");
        buttonMulti.setWidth(200);
        buttonMulti.setHeight(50);
        buttonMulti.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/8);
        Gdx.app.debug("height", Float.toString(Gdx.graphics.getHeight()));

        levelUp = new TextButton("Level up", skin);
        levelUp.setWidth(200);
        levelUp.setHeight(50);
        levelUp.setPosition(0, 0);

        levelUp.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.debug("GameScreen", "GO UP ONE LEVEL");
                level.upLevel();
            }
        });

//        table.add(buttonMulti);
//        table.row();
//        table.add(levelUp);

//        hud.addActor(table);
        hud.addActor(buttonMulti);
        hud.addActor(levelUp);




        //buttonMulti.addListener(new TextTooltip("this is a tooltip test!", skin));









        stage.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector3 mouseRaw = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                float gutterX = viewport.getWorldWidth();
                float gutterY = viewport.getWorldHeight();
                int width = viewport.getScreenWidth();
                int height = viewport.getScreenHeight();
                Vector3 prevWorldMouse = new Vector3(camera.unproject(mouseRaw));
                Gdx.app.debug("GameScreen", "TouchLoc: " + prevWorldMouse.toString());
                Gdx.app.debug("GameScreen", "\nmouseRaw: " + mouseRaw.toString());
                Gdx.app.debug("GameScreen", "\nGutter: " + Float.toString(gutterX) + " " + Float.toString(gutterY));
                Gdx.app.debug("GameScreen", "\nScreen Width/height: " + Integer.toString(width) + " " + Integer.toString(height));
                if(event.getRelatedActor() != null) Gdx.app.debug("GameScreen", " Touched: " + event.getRelatedActor().toString());
                else Gdx.app.debug("--", "\n");
                return true;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                Vector3 mouseRaw = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 prevWorldMouse = new Vector3(viewport.unproject(mouseRaw)); // Get mouse location before zoom

                if(amount > 0) { // Scrolling Out
                    viewport.setWorldSize(viewport.getWorldWidth() + Math.abs(amount), viewport.getWorldHeight() + Math.abs(amount)); // Change viewport

                    if(viewport.getWorldWidth() > Const.VIEWPORT_W || viewport.getWorldHeight() > Const.VIEWPORT_H) {
                        viewport.setWorldSize(Const.VIEWPORT_W, Const.VIEWPORT_H); // CLAMP
                    }
                }
                else if(amount < 0) { // Scrolling In
                    viewport.setWorldSize(viewport.getWorldWidth() - Math.abs(amount), viewport.getWorldHeight() - Math.abs(amount)); // Change viewport
                    if(viewport.getWorldWidth() < 1 || viewport.getWorldHeight() < 1) {
                        viewport.setWorldSize(1, 1); // CLAMP
                    }
                }
                viewport.apply(true); // Update viewport, don't center camera.

                mouseRaw.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 newWorldMouse = new Vector3(viewport.unproject(mouseRaw)); // Get new mouse location
                prevWorldMouse.sub(newWorldMouse); // Math

                camera.translate(prevWorldMouse); // Move camera to correct location so that mouse is over the exact spot
                // it was over before zooming (unless clamping disallows as per below)

                if(camera.position.x - camera.viewportWidth/2 < 0) {
                    camera.position.x = camera.viewportWidth/2;
                }
                if(camera.position.y - camera.viewportHeight/2 < 0) {
                    camera.position.y = camera.viewportHeight/2;
                }
                if(camera.position.x + camera.viewportWidth/2 > Const.VIEWPORT_W) {
                    camera.position.x = Const.VIEWPORT_W - camera.viewportWidth/2;
                }
                if(camera.position.y + camera.viewportHeight/2 > Const.VIEWPORT_H) {
                    camera.position.y = Const.VIEWPORT_H - camera.viewportHeight/2;
                }


                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch(keycode) {
                    case Input.Keys.RIGHT:
                        Gdx.app.debug("GameScreen", "Move cell returns: " + level.moveCell(GridDirection.RIGHT));
                        break;
                    case Input.Keys.UP:
                        Gdx.app.debug("GameScreen", "Move cell returns: " + level.moveCell(GridDirection.UP));
                        break;
                    case Input.Keys.LEFT:
                        Gdx.app.debug("GameScreen", "Move cell returns: " + level.moveCell(GridDirection.LEFT));
                        break;
                    case Input.Keys.DOWN:
                        Gdx.app.debug("GameScreen", "Move cell returns: " + level.moveCell(GridDirection.DOWN));
                        break;
                    case Input.Keys.U:
                        Gdx.app.debug("GameScreen", "GO UP ONE LEVEL");
                        level.upLevel();
                        break;
                    case Input.Keys.SPACE:
                        level.getCell().doMining();
                        break;
                    case Input.Keys.R:
                        if(selectedSlot != null && selectedSlot.getTile() != null) selectedSlot.getTile().rotate90();
                        break;

                    default:
                        break;
                }
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(event.getRelatedActor() instanceof Slot) {
                    selectedSlot = ((Slot) event.getRelatedActor());
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                selectedSlot = null;
            }
        });


        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        level = new Level(this, 0);
        stage.addActor(level);
    }

    /**
     * Sets the background color and renders the stage
     * @param delta Amount of time between calls
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        stage.act(delta);
        hud.act(delta);


        camera.update();


        stage.draw();
        hud.draw();
    }

    /**
     * Called when the window is resized. Updates the viewport with the new information. Updates the camera.
     * @param width New width of the window
     * @param height New height of the window
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        camera.update();



        hud.getViewport().update(width, height, true);

    }

    @Override
    public void dispose() {
        stage.dispose();
        hud.dispose();
    }
}
