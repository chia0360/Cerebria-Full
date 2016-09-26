/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game.screens;

import com.oracledragon.game.Cerebria;

/**
 */
public class SettingsScreen extends BaseScreen {
    
    public SettingsScreen(Cerebria game) {
        super(game);
        
        setQuitButton(new MainMenuScreen(game));
    }
    
    public void update(float delta) {
        // -- any updates go here
    }
    
    public void draw(float delta) {
        batch.begin();
        
        // -- any drawing that uses SpriteBatch goes here
        
        game.scoreFont.draw(batch, "SETTINGS", 10, 470);
        
        batch.end();
    }
    
    public void onTouch(float x,float y) {
        // -- handle single touch on the screen at (x,y)
        // (x,y) is the world-coordinate, after transformation
        // from screen coord
    }
    
    public void onRelease() {
        // -- handle the release of the single touch
        
    }
    
    public void dispose() {
        // -- dispose your game assets here
    }
    
}
