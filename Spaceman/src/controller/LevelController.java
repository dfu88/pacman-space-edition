package controller;

import model.Level;

import view.LevelVisuals;
import view.Spaceman;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;

public class LevelController {

	private InterfaceController interfaceCtrl;
	private LevelVisuals currentView;
	private Level levelModel;
	//public Spaceman spaceman;

	public LevelController(InterfaceController controller) {
		interfaceCtrl = controller;
		currentView = new LevelVisuals(this);
		levelModel = new Level();

		levelModel.makeMaps();

		currentView.returnScene().setOnKeyPressed(new EventHandler <KeyEvent> () {
			public void handle(KeyEvent input) {
				if (input.getCode() == KeyCode.LEFT) {
					currentView.spaceman.setKeyInput(0);
				} else if(input.getCode() == KeyCode.RIGHT) {
					currentView.spaceman.setKeyInput(2);
				} else if(input.getCode() == KeyCode.UP) {
					currentView.spaceman.setKeyInput(1);
				} else if(input.getCode() == KeyCode.DOWN) {
					currentView.spaceman.setKeyInput(3);
				}
			}
		});
	}
	
	public Level getLevel() {
		return levelModel;
	}
	public void setLevel(int type){
		levelModel.setMap(type);
		currentView.generateMap();
		interfaceCtrl.getMainApp().changeScene(currentView.returnScene()); // possible dont call getmainAPp()
	}																		//create method in intCtrller to change scenes
	
	public int checkMap(int x, int y) {
		return levelModel.getCurrentMap().getData(y, x);
	}
	
	public void updateMap(int dx, int dy,int posX, int posY) {
		levelModel.getCurrentMap().updateData(dx, dy, posX, posY);
		currentView.hideCorrespondingPellet(posX+dx, posY + dy);
	}
}
