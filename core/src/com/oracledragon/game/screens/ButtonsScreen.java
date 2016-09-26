/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.oracledragon.game.Cerebria;

/**
 *
 * @author Billy
 */
public class ButtonsScreen extends BaseScreen implements ClockInvocable {
    private final int READY_TO_PLAY = 99;
    private final int START = 98;
    private TextureAtlas buttonAtlas;
    private TextureRegion[] regions;
    private Rectangle[] triggers;
    private Sprite flash;
    private float flashAlpha;
    
    private int phase = 0;
    
    public static final int CORNFLOWER_BLUE = 0x6495EDFF;
    Color origColor = new Color(CORNFLOWER_BLUE);
    
    private boolean touched = false;
    private boolean[] active = {false, false, false, false, false};
    
    private int[] sequence = {-1,-1,-1,-1,-1,-1};
    private float flashDuration = 1.5f;
    private float betweenDuration = 0.25f; // should be less than flashDuration
    private boolean inputEnabled = false;
    private int sequenceIndex = 0;
    
    public ButtonsScreen(Cerebria game) {
        super(game);
        this.setClearColor(origColor);
        
        // load texture
        buttonAtlas = new TextureAtlas(Gdx.files.internal("buttons.atlas"));
        regions = new TextureRegion[12];
        regions[0] = buttonAtlas.findRegion("010");
        regions[1] = buttonAtlas.findRegion("011");
        regions[2] = buttonAtlas.findRegion("012");
        regions[3] = buttonAtlas.findRegion("013");
        regions[4] = buttonAtlas.findRegion("014");
        regions[5] = buttonAtlas.findRegion("000");
        regions[6] = buttonAtlas.findRegion("001");
        regions[7] = buttonAtlas.findRegion("002");
        regions[8] = buttonAtlas.findRegion("003");
        regions[9] = buttonAtlas.findRegion("004");
        regions[10] = buttonAtlas.findRegion("redflash");
        regions[11] = buttonAtlas.findRegion("greenflash");
        
        flash = new Sprite(regions[10]);
        flash.setX(0);
        flash.setY(0);
        flash.setSize(800,480);
        flash.setColor(1,1,1,0);
        
        triggers = new Rectangle[5];
        for(int i=0;i<5;i++) {
            triggers[i] = new Rectangle(60+138*i, 240 - 64, 128,128);
        }
        
        drawCommonGui = true;
        setQuitButton(new MainMenuScreen(game));
        clockHandler = this;
    }
    
    @Override
    public void draw(float delta) {
        batch.begin();
        
        // draw the buttons
        for(int i=0;i<5;i++) 
            batch.draw(regions[i + (active[i]? 5 : 0)], triggers[i].x, triggers[i].y);
        
        if(flashAlpha>0) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            flash.draw(batch);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.end();
    }
    
    public void flashScreen(boolean red) {
        flash.setRegion(regions[red ? 10:11]);
        flashAlpha = 1.0f;
        
    }
    
    @Override
    public void onTouch(float x,float y) {
        if(!inputEnabled) return;
        int j;
        for(int i=0;i<5;i++) {
            
            if(triggers[i].contains(x,y)) {
                active[i] = true;       
                game.playSound(game.commonSounds[game.SND_CLICK]);
            }
        }
    }
    
    @Override
    public void onRelease() {
        if(!inputEnabled) return ;
        
        for(int i=0;i<5;i++) {
            if(active[i]) {
                 if(sequence[sequenceIndex] == i) {
                    // found the correct button in sequence
                    sequenceIndex++;
                    if(sequenceIndex >= sequence.length || sequence[sequenceIndex] == -1) {
                            // all correct
                            flashScreen(false);
                            game.playSound(game.commonSounds[game.SND_CORRECT]);
                            stopTiming();
                            
                            // SCORE
                            Cerebria.score += 10;
                            
                            // restart the run
                            phase = 0;
                    }
                } else {
                    // wrong button
                    flashScreen(true); // red flash
                    game.playSound(game.commonSounds[game.SND_WRONG]);
                    stopTiming();
                    
                    int j;
                    for(j=sequenceIndex; 
                        j<sequence.length && sequence[j]>-1; j++);
                    
                    // j is the sequence length for this run
                    Cerebria.score += sequenceIndex*10.0f / j;
                    
                    // restart the run
                    phase = 0;
                }
            }
            active[i] = false;
        }
    }
    
    public void setInputEnabled(boolean enabled) {
        inputEnabled = enabled;
        if(!enabled) {
            for(int i=0;i<5;i++)
            {
                active[i] = false;
            }
        }
    }
    
    private float tempLerp = 0;
    private int scratchInt = 0;
    
    @Override
    public void update(float delta) {
        if(fadeState!=0)
            return;
        
        if(flashAlpha>0) {
            flashAlpha-=delta;
            if(flashAlpha<0) flashAlpha = 0;
            flash.setColor(1,1,1,flashAlpha);
        }
        
        int tempInt,i,j;
        
        switch(phase) {
            case 0: // darken background
                tempLerp += delta;
                if(tempLerp>1) {
                    tempLerp = 1;
                    phase = 1;
                }
                if(inputEnabled) setInputEnabled(false);
                origColor.set(CORNFLOWER_BLUE);
                this.setClearColor(origColor.lerp(Color.BLACK, tempLerp));
                break;
                
            case 1: // prepare random sequence
                tempInt = (int)(Math.random()*3) + 3;
                for(i=0;i<sequence.length;i++) {
                    if(i<tempInt)
                        sequence[i] = (int)(Math.random() * 5);
                    else
                        sequence[i] = -1;
                }
                phase = 2;
                
                scratchInt = 0;
                break;
                
            case 2:
                
                if(scratchInt>= sequence.length || sequence[scratchInt] == -1) {
                    // move to next phase
                    phase=4;
                    tempLerp = 1;
                    break;
                }
                
                // flash the appropriate button
                active[sequence[scratchInt]] = true;       
                game.playSound(game.commonSounds[game.SND_CLICK]);
                
                // set countdown
                tempLerp = flashDuration;
                phase = 3;
                break;
            
            case 3:
                tempLerp -= delta;
                
                if(active[sequence[scratchInt]] && tempLerp < betweenDuration) 
                    active[sequence[scratchInt]] = false;
                
                if(tempLerp<=0) {
                    phase = 2;
                    scratchInt++;
                }
                break;
                
            case 4: // brighten background
                tempLerp -= delta;
                if(tempLerp<0) {
                    tempLerp = 0;
                    phase = READY_TO_PLAY;
                    sequenceIndex = 0;
                    startTiming();
                    setInputEnabled(true);
                }
                origColor.set(CORNFLOWER_BLUE);
                this.setClearColor(origColor.lerp(Color.BLACK, tempLerp));
                break;
                
            
        }
    }
    
    public void startTiming() {
        clockLeft = 10;
        clockEnabled = true;
    }
    
    public void stopTiming() {
        clockEnabled = false;
    }
    
    @Override
    public void dispose() {
        buttonAtlas.dispose();
    }

    @Override
    public void action() {
        stopTiming();
        game.playSound(game.commonSounds[game.SND_WRONG]);
        flashScreen(true);
        
        phase = 0;
    }
    
}
