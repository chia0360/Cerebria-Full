/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.oracledragon.game.Cerebria;

/**
 */
public class WhackAMoleScreen extends BaseScreen implements ClockInvocable {
    
    private Texture imgWrong;
    private int wrongIndexer = 0;
    private float wrongAlpha = 0;
    
    private TextureAtlas gameAtlas;
    private TextureRegion[] regions;
    private Rectangle[] triggers;
    private boolean[] active;
    private int phase;
    private float timeLerp;
    private float meanWaitTime = 3.0f;
    private float meanDuration = 1.0f;
    private float waitTimeSpread = 0.5f;
    private float durationSpread = 0.3f;
    private int chosenMole = 0;
    
    public WhackAMoleScreen(Cerebria game) {
        super(game);
        this.setClearColor(new Color(ButtonsScreen.CORNFLOWER_BLUE));
        
        // screen params
        clockEnabled = true;
        clockLeft = 60;
        clockHandler = this;
        
        // place holder asset
        imgWrong = new Texture(Gdx.files.internal("wrong.png"));
        gameAtlas = new TextureAtlas(Gdx.files.internal("buttons.atlas"));
        regions = new TextureRegion[4];
        regions[0] = gameAtlas.findRegion("010");
        regions[1] = gameAtlas.findRegion("000");
        regions[2] = gameAtlas.findRegion("redflash");
        regions[3] = gameAtlas.findRegion("greenflash");
        
        triggers = new Rectangle[9];
        active = new boolean[9];
        float xstart = 400 - 230; // half of screen dimension - half of 3x3 dimension
        float ystart = 240 - 230;
        float interDistance = 10;
        float size = (460.0f - 2*interDistance) / 3.0f; // (size of 3x3 - 2*interdistance) / 3.0f
        for(int y=0;y<3;y++) {
            for(int x=0;x<3;x++) {
               triggers[y*3+x]=new Rectangle(xstart+(size+interDistance)*x, 
                       ystart+(size+interDistance)*y, size, size);
               active[y*3+x] = false;
            }
        }
        
        phase = 0;
        timeLerp = 1.0f;
        
        drawCommonGui = true;
        
        // set the quit button
        setQuitButton(new MainMenuScreen(game));
    }
    
    public void update(float delta) {
        if(fadeState != 0)
            return;
        
        // -- any updates go here
        switch(phase) {
            case 0: // INIT
                timeLerp-=delta;
                if(timeLerp<=0) {
                   timeLerp = 0;
                   phase = 1;
                }
                break;
                
            case 1: // PREP FOR COUNTDOWN
                timeLerp = (float)(Math.random() * waitTimeSpread - 
                            waitTimeSpread/2.0f + meanWaitTime);
                phase = 2;
                break;
                
            case 2: // COUNTDOWN
                timeLerp -=delta;
                if(timeLerp<=0) {
                    // choose a random mole
                    chosenMole = (int)(Math.random() * 9);
                    timeLerp = (float)(Math.random() * durationSpread - 
                            durationSpread/2.0f + meanDuration);
                    
                    // flash the mole
                    active[chosenMole] = true;
                    
                    phase = 3;
                }
                break;
                
            case 3: // COUNTDOWN TO MISS
                timeLerp -= delta;
                if(timeLerp <= 0) {
                    timeLerp = 0;
                    if(active[chosenMole]) {
                        // mole has not been whacked!
                        wrongIndexer = chosenMole;
                        wrongAlpha = 1.0f;
                        active[chosenMole] = false;
                    }            
                    
                    phase = 1;
                }
                break;
            
        }
        
        if(wrongAlpha>0){
            wrongAlpha -= delta;
            if(wrongAlpha<=0) wrongAlpha = 0;
        }
    }
    
    public void draw(float delta) {
        batch.begin();
        
        // -- any drawing that uses SpriteBatch goes here
        for(int i=0;i<9;i++) {
            batch.draw(regions[active[i]? 1 : 0],triggers[i].x,
                    triggers[i].y, triggers[i].width, triggers[i].height);
        }
        
        if(wrongAlpha>0) {
            Rectangle r = triggers[wrongIndexer];
            batch.setColor(1,1,1,wrongAlpha);
            batch.draw(imgWrong, r.x+r.width/2 - 64, r.y+r.height/2 - 64);
            batch.setColor(1,1,1,1);
        }
        batch.end();
    }
    
    public void onTouch(float x,float y) {
        // -- handle single touch on the screen at (x,y)
        // (x,y) is the world-coordinate, after transformation
        // from screen coord
        
        for(int i=0;i<9;i++) {
            if(triggers[i].contains(x,y)) {
                touchedMole(i);
            }
        }
    }
    
    void touchedMole(int index) {
        if(active[index]) {
            // correct!
            Cerebria.score += 10;
            active[index] = false;
            
            game.playSound(game.commonSounds[game.SND_CORRECT]);
        }
        else {
            // wrong!
            active[chosenMole] = false;
            wrongIndexer = index;
            wrongAlpha = 1.0f;
            
            game.playSound(game.commonSounds[game.SND_WRONG]);
        }
    }
    
    public void onRelease() {
        // -- handle the release of the single touch
        
    }
    
    public void dispose() {
        // -- dispose your game assets here
        gameAtlas.dispose();
    }
    
    @Override
    public void action() {
        // change screen
        clockLeft = 60;
    }
    
}
