package view;

import java.util.ArrayList;

import controller.LevelController;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;

//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;


public class LevelVisuals {
	
	private double SCENE_WIDTH = 1440;
	private double SCENE_HEIGHT = 900;
	private Scene scene;
	private Group root;
	private ArrayList<Pellet> pelletsRendered;
	
	public LevelVisuals () {
		pelletsRendered = new ArrayList<Pellet>();
		
		//Setup Scene for game visuals
		root = new Group(); 
		scene = new Scene(root,SCENE_WIDTH,SCENE_HEIGHT);
		scene.setFill(Color.LIGHTBLUE);
	}

	public Scene returnScene() {
		return scene;
	}
	
	public void generateMap(LevelController controller) {
		
		//NOTE MAKE CONST FOR NOW UNLESS TILE SIZE CHANGES BASED ON MAPARRAY SIZE
		double tileWidth = 40;
		double tileHeight = 40;
		
		//NOTE: CHANGE MAGIC NUMBER (21) TO var or constant
		double mapOffsetY = (SCENE_HEIGHT-tileHeight*21)*0.5; //(WindowH - MapH)/2 (centers it) = 30
		double mapOffsetX = (SCENE_WIDTH - tileWidth*21)*0.5; //WIndowW - MapW)/2 = 300
		
		root.getChildren().clear();
		
		for (int row = 0; row < 21; row++) {
			for (int col = 0; col < 21; col++) {
				
				int currentElement = controller.currentLevel.currentMap.getData(row, col);
				//Walls
				if (currentElement == 1) {
					Rectangle wall = new Rectangle(mapOffsetX+tileWidth*col, mapOffsetY+tileHeight*row, tileWidth, tileHeight);
					wall.setFill(Color.INDIANRED); //fill
					wall.setStroke(Color.INDIANRED);//outline
					root.getChildren().add(wall);
					
				//Pellets	
				} else if (currentElement == 2) {
					Pellet pellet = new Pellet(mapOffsetX+tileWidth*(col+0.5), mapOffsetY+tileHeight*(0.5+row), tileWidth*0.125);
					//we can have a class 'Theme' to have a combination of preset colours to use
					pellet.returnPellet().setFill(Color.BLUEVIOLET); 
					root.getChildren().add(pellet.returnPellet());
					pelletsRendered.add(pellet);
				
				//Magic Pellet	
				} else if (currentElement == 3) {
					Circle powerup = new Circle(mapOffsetX+tileWidth*col+tileWidth*0.5, mapOffsetY+tileHeight*row+tileHeight*0.5, tileWidth*0.35);
					powerup.setFill(Color.CRIMSON);
					root.getChildren().add(powerup);
				}
			}
		}
		root.getChildren().add(controller.currentLevel.spaceman);
		controller.currentLevel.spaceman.start();
		
		
		//add other level objects
		Text lives = new Text();
		lives.setText("Lives");
		lives.setFont(Font.font("Comic Sans MS", FontWeight.BOLD,50));
		lives.setX((mapOffsetX-lives.getLayoutBounds().getWidth())*0.5);
		lives.setY(100.0);
		root.getChildren().add(lives);
		
		
		Text timeLabel = new Text();
		timeLabel.setText("Time:");
		timeLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD,50));
		timeLabel.setX(SCENE_WIDTH-mapOffsetX + ((mapOffsetX-timeLabel.getLayoutBounds().getWidth())*0.5));
		timeLabel.setY(100.0);
		root.getChildren().add(timeLabel);
		
		Text time = new Text();
		time.setText(Integer.toString(controller.currentLevel.timeRemaining));
		time.setFont(Font.font("Comic Sans MS",50));
		time.setX(SCENE_WIDTH-mapOffsetX + ((mapOffsetX-time.getLayoutBounds().getWidth())*0.5));
		time.setY(100+timeLabel.getLayoutBounds().getHeight()+10);
		root.getChildren().add(time);
		
	}
	

}
