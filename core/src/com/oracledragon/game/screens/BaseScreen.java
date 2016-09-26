/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.oracledragon.game.Button;
import com.oracledragon.game.Cerebria;
import java.util.ArrayList;

/**
 * Represents a common screen
 */
public abstract class BaseScreen implements Screen, Button.ButtonHandler{
    protected final OrthographicCamera camera;
    protected final Cerebria game;
    protected final SpriteBatch batch;
    protected Color clearColor;
    
    // single touch
    private boolean touched = false;
    
    // will the update routine be called?
    protected boolean canUpdate = true;
    
    // fading transition
    private float fadeAlpha = 0;
    protected int fadeState = 0;
    private BaseScreen fadeTargetScreen = null;
    private boolean fadeOverrideDraw = false;
    private Color fadeColor;
    
    // screen parameters
    protected boolean clockEnabled = false;  // Countdown timer associated with
                                             // this screen?
    protected ClockInvocable clockHandler = null;
    protected float clockLeft = 0;
    
    protected boolean drawCommonGui = false;  // draw the common minigame gui?
    
    // top right corner buttons
    protected Button[] cornerButtons = {null, null, null};
    protected boolean[] buttonEnabled = {true, true, true};
    protected boolean isRightButtonACross = false;
    protected BaseScreen quitTargetScreen;
    public static final int LEFT_BUTTON = 0;
    public static final int CENTER_BUTTON = 1;
    public static final int RIGHT_BUTTON = 2;
    
    public boolean eventHandled = false;
    
    protected ArrayList<Button> buttons;
    
    /**
     * Add a Button to be handled internally by this screen. All events on this button
     * will be routed to the onButton* functions.  Be sure the buttons id is bigger than 2
     * @param button 
     */
    public void registerButton(Button button) {
        for(int i=0;i<buttons.size();i++) 
        {
            if(buttons.get(i).id == button.id)
            {
                System.out.println("ERROR - Trying to register button with an existing ID.");
                return;
            }
        }
        button.setHandler(this);
        buttons.add(button);
    }
    
    public void unregisterButton(int id) {
        for(int i=0;i<buttons.size();i++) 
        {
            if(buttons.get(i).id == id)
            {
                buttons.remove(i);
                return;
            }
        }
        System.out.println("WARNING - No such button ID to unregister");
    }
    
    public void setClearColor(Color color) {
        clearColor = color;
    }
    
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    public Cerebria getGame() {
        return game;
    }
    
    public BaseScreen(Cerebria game) {
        this.game=game;
        clearColor = new Color(0x6495EDFF);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Cerebria.WIDTH, Cerebria.HEIGHT);
        batch = game.getSpriteBatch();
        
        fadeColor = Color.BLACK.cpy();
        
        buttons = new ArrayList();
    }
    
    /**
     * Sets one of the three corner buttons to have the given texture. Set texture to null 
     * to remove button
     */
    public void setCornerButton(int buttonIndex, TextureRegion texture) {
        if(texture == null)
            cornerButtons[buttonIndex] = null; 
        else {
            cornerButtons[buttonIndex] = new Button(688+35*buttonIndex, 438, 32,32, 
                    texture, texture);
            cornerButtons[buttonIndex].setHandler(this);
            cornerButtons[buttonIndex].id = buttonIndex;
        }
    }
    
    /**
     * Uses the rightmost corner button as the default quit button
     * @param targetScreen which screen to exit to
     */
    public void setQuitButton(BaseScreen targetScreen) { 
        setCornerButton(RIGHT_BUTTON, game.guiCross);
        isRightButtonACross = true;
        quitTargetScreen = targetScreen;
    }
    
    public void setButtonEnabled(int buttonIndex, boolean enabled) {
        buttonEnabled[buttonIndex] = enabled;
    }
    
    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(clearColor.r,clearColor.g,clearColor.b,clearColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        this._update(delta);
        if(canUpdate)
            this.update(delta);
        
        batch.setProjectionMatrix(camera.combined);
        game.getShapeRenderer().setProjectionMatrix(camera.combined);
        if(!this.fadeOverrideDraw) {
            this.draw(delta);
            batch.begin();
            
            if(drawCommonGui) {
                if(clockEnabled) 
                    game.drawCommonMiniGameGui((int)Math.ceil(clockLeft));
                else 
                    game.drawCommonMiniGameGui();
            }
            
            // draw buttons
            for(int i=0;i<buttons.size();i++)
                buttons.get(i).draw(batch);
            
            // draw corner buttons
            for(int i=0;i<3;i++) {
                if(cornerButtons[i] != null) {
                    batch.setColor(1,1,1, buttonEnabled[i] ? 1 : 0.2f);
                    cornerButtons[i].draw(batch);
                }
               
            }
            batch.setColor(1,1,1,1);
            batch.end();
        }
        
        if(fadeAlpha>0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            ShapeRenderer rend = game.getShapeRenderer();
            
            rend.begin(ShapeType.Filled);
            rend.setColor(fadeColor);
            rend.rect(0,0,Cerebria.WIDTH, Cerebria.HEIGHT);
            rend.end();
             Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    @Override
    public void resize(int w, int h) { }

    @Override
    public void pause() {  }

    private void _update(float delta) {
        camera.update(); 
        Vector3 v;
        int i;
        
        if(!touched && Gdx.input.isTouched()) {
            touched = true;
            v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            v = camera.unproject(v);

            eventHandled = false;
            
            // corner buttons
            for( i=0;i<3;i++) {
                if(buttonEnabled[i] && cornerButtons[i] != null)
                    cornerButtons[i].baseScreenOnTouch(v.x,v.y, this);
            }
            
            if(!eventHandled) {
                for(i=0;i<buttons.size();i++)
                    buttons.get(i).baseScreenOnTouch(v.x,v.y,null);
                onTouch(v.x, v.y);  
            }
            
        } else if(touched && !Gdx.input.isTouched()) {
            touched = false;

            // corner buttons
            for(i=0;i<3;i++) {
                if(buttonEnabled[i] && cornerButtons[i] != null)
                    cornerButtons[i].baseScreenOnRelease(this);
            }
            if(!eventHandled) {
                for(i=0;i<buttons.size();i++)
                    buttons.get(i).baseScreenOnRelease(null);
                 onRelease();
            }
               
        }
        
        if(touched) {
            v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            v = camera.unproject(v);

            // corner buttons
            for( i=0;i<3;i++) {
                if(buttonEnabled[i] && cornerButtons[i] != null)
                    cornerButtons[i].baseScreenOnHold(v.x,v.y, this);
            }
            if(!eventHandled) {
                for(i=0;i<buttons.size();i++)
                    buttons.get(i).baseScreenOnHold(v.x,v.y,null);
                onHold(v.x,v.y);
            }
                
        }
        
        // any other updates here
        
        
        // Fade transition
        switch(fadeState) {
            case 1: // Fade In
                fadeAlpha -= delta*0.8f;
                if(fadeAlpha<=0) {
                    fadeAlpha=0;
                    fadeState=0;
                }
                break;
                
            case -1: // Fade Out
                fadeAlpha += delta*0.8f;
                if(fadeAlpha>=1) {
                    fadeAlpha=1;
                    fadeState=0;
                    
                    this.dispose();
                    
                    // now that things have been disposed
                    // don't draw anything
                    this.fadeOverrideDraw = true;
                    fadeTargetScreen.fadeAlpha = 1.0f;
                    fadeTargetScreen.fadeState = 1;
                    game.setScreen(fadeTargetScreen);
                }
                break;
        }
        fadeColor.a = fadeAlpha;
        
        // clock countdown
        if(clockEnabled && fadeState == 0) {
            if(clockLeft>0) {
                clockLeft -= delta;
                if(clockLeft < 0) {
                    clockLeft = 0;
                    if(clockHandler != null) 
                        clockHandler.action();
                }
            }
        }
        
        
    }
    
    // ========================================================
    // OVERRIDE THESE FUNCTIONS IN DERIVED CLASSES IF NEED TO
    public void onTouch(float x, float y) {  }
    public void onHold(float x, float y) {}
    public void onRelease() { }
    public void onButtonTouch(int buttonIndex) { }
    public void onButtonHold(int buttonIndex) { }
    public void onButtonRelease(int buttonIndex) { }
    public void draw(float delta)  {}
    public void update(float delta) {  }
    // ========================================================

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
    
    /**
     * Fade transition to next screen, disposing this current one
     * @param nextScreen 
     */
    public void fadeToScreen(BaseScreen nextScreen) {
        if(fadeState == 0) {
            this.fadeAlpha = 0;
            this.fadeState = -1; // fade out
        }
        fadeTargetScreen = nextScreen;
    }
    
    @Override
    public void buttonTouch(Button sender) {
        game.playSound(game.commonSounds[0]);
        onButtonTouch(sender.id);
    }
    
    @Override
    public void buttonHold(Button sender) {
        onButtonHold(sender.id);
    }
    
    @Override
    public void buttonRelease(Button sender) {
        if(sender.id == RIGHT_BUTTON && isRightButtonACross) {
            fadeToScreen(quitTargetScreen);
        }
        onButtonRelease(sender.id);
    }
    
}
