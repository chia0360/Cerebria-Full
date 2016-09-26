/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracledragon.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.oracledragon.game.Button;
import com.oracledragon.game.Cerebria;

/**
 *
 * @author Billy
 */
public class MainMenuScreen extends BaseScreen {
    
    private TextureAtlas atlas;
    private TextureRegion[] regions;
    private Button.ButtonHandler buttonHandler;
    
    public MainMenuScreen(Cerebria game) {
        super(game);
        Color color = new Color(0x6495EDFF);
        this.setClearColor(color);
        
        this.setCornerButton(RIGHT_BUTTON, game.guiGear);
        this.setCornerButton(CENTER_BUTTON, game.guiHelp);
        
        atlas = new TextureAtlas(Gdx.files.internal("mainmenu.atlas"));
        regions = new TextureRegion[3];
        regions[0] = atlas.findRegion("startgame");
        regions[1] = atlas.findRegion("choosegame");
        regions[2] = atlas.findRegion("leaderboard");
        
        
        // create the buttons
        Button button;
        for(int i=0;i<3;i++) {
            button = new Button(400-128, 200 - 51*i, 256,48, regions[i], regions[i]);
            button.id = i+3;
            this.registerButton(button);
        }
    }
    
    @Override
    public void draw(float delta) {
        batch.begin();
        game.scoreFont.draw(batch, "MAIN_MENU", 10, 470);
        
        batch.end();
    }
    
    @Override
    public void dispose() {
        
        atlas.dispose();
    }
    
    @Override
    public void onButtonRelease(int index) {
        switch(index) {
            case RIGHT_BUTTON:
                fadeToScreen(new SettingsScreen(game));
                break;
            case CENTER_BUTTON:
                fadeToScreen(new HelpScreen(game));
                break;
            case 3:
                fadeToScreen(new ButtonsScreen(game));
                break;

            case 4:
                fadeToScreen(new ChooseGameScreen(game));
                break;

            case 5:
                fadeToScreen(new LeaderboardScreen(game));
                break;
                
        }
    }
}
