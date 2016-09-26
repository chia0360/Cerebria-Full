package com.oracledragon.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.oracledragon.game.screens.MainMenuScreen;

public class Cerebria extends Game {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    public static float score = 0;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    // this is made public for debugging
    public BitmapFont scoreFont;
    
    private TextureAtlas commonGuiAtlas;
    public TextureRegion guiCross, 
                         guiClock,
                         guiGear,
                         guiHelp;

    public final int SND_BUTTON = 0,
                     SND_CORRECT = 1,
                     SND_WRONG = 2,
                     SND_CLICK = 3;
    
    public Sound[] commonSounds;
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    public SpriteBatch getSpriteBatch() {
        return batch;
    }

    @Override
    public void create () {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            
            // GENERATE SCORE FONT
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("ARCADECLASSIC.TTF"));
            FreeTypeFontParameter param = new FreeTypeFontParameter();
            param.size = 45;
            param.borderColor = Color.BLACK;
            param.color = Color.YELLOW;
            
            scoreFont = gen.generateFont(param);
            gen.dispose();
            
            // load common gui textures
            commonGuiAtlas = new TextureAtlas(Gdx.files.internal("commongui.atlas"));
            
            guiCross = commonGuiAtlas.findRegion("cross");
            guiClock = commonGuiAtlas.findRegion("clock");
            guiGear = commonGuiAtlas.findRegion("gear");
            guiHelp = commonGuiAtlas.findRegion("help");
            
            // load common sound
            commonSounds = new Sound[] {
                Gdx.audio.newSound(Gdx.files.internal("menubutton.wav")),
                Gdx.audio.newSound(Gdx.files.internal("correct.ogg")),
                Gdx.audio.newSound(Gdx.files.internal("wrong.ogg")),
                Gdx.audio.newSound(Gdx.files.internal("click.ogg"))
            };
            
            // set to main screen
            setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose () {
            batch.dispose();
            shapeRenderer.dispose();
            commonGuiAtlas.dispose();
            for(Sound s : commonSounds)
                s.dispose();
    }
    
    public void drawCommonMiniGameGui() {
        drawScore(10,470);
    }
    
    public void drawCommonMiniGameGui(int clockAmount) {
        drawScore(10,470);
        batch.draw(guiClock, 10, 400, 32,32);
        scoreFont.draw(batch, String.valueOf(clockAmount), 45, 424);
    }
    
    public void drawScore(float x, float y) {
        Integer ss = (int)Cerebria.score;
        scoreFont.draw(batch, ss.toString(), x, y);
    }

    public void playSound(Sound snd) {
        snd.play();
    }
}
