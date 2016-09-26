/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.oracledragon.game.screens.BaseScreen;

/**
 * Represents a GUI button
 */
public class Button {
    
    public interface ButtonHandler {
        void buttonTouch(Button sender);
        void buttonHold(Button sender);
        void buttonRelease(Button sender);
    }
    
    public int id=0;
    private TextureRegion textureIdle, texturePressed;
    private Rectangle trigger;
    private ButtonHandler handler = null;
    private boolean pressed = false;
    
    public void setHandler(ButtonHandler handler) {
        this.handler = handler;
    }
    
    public Rectangle getDimensions() {
        return trigger;        
    }
    
    public Button(float x, float y, float width, float height, 
            TextureRegion texIdle, TextureRegion texPressed) {
        textureIdle = texIdle;
        texturePressed = texPressed;
        
        trigger = new Rectangle(x,y,width,height);
    }
    
    /**
     * Draws the button. Make sure SpriteBatch.begin() has been called
     */
    public void draw(SpriteBatch batch) {
        if(pressed) {
            float hw = trigger.width/2 - trigger.width*0.45f;
            float hh = trigger.height/2 - trigger.height*0.45f;
            batch.draw(texturePressed, trigger.x + hw, trigger.y + hh, 
                    trigger.width*0.9f,trigger.height*0.9f);
        } else {
            batch.draw(textureIdle, trigger.x, trigger.y, trigger.width,
                    trigger.height);
        }
    }
    
    public void baseScreenOnTouch(float x, float y, BaseScreen parent) {
        if(trigger.contains(x,y)) {
            if(handler!=null) {
                handler.buttonTouch(this);
                if(parent!=null)
                    parent.eventHandled = true;
            }
            pressed = true;
        }
    }
    
    public void baseScreenOnHold(float x, float y, BaseScreen parent) {
        if(pressed) {
            if(trigger.contains(x,y)) {
                if(handler!=null) {
                    handler.buttonHold(this);
                    if(parent!=null)
                        parent.eventHandled = true;
                }
            } else
                pressed = false;
        }
    }
    
    public void baseScreenOnRelease(BaseScreen parent) {
        if(pressed && handler!=null) {
            handler.buttonRelease(this);
        }
        pressed = false;
    }
}
