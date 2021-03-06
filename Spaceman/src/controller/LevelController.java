package controller;

import model.Level;

import view.Alien;
import view.Leaderboard;
import view.LevelVisuals;
import view.StorySlides;

import java.util.ArrayList;
import javafx.util.Duration;

import javafx.scene.image.Image;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class LevelController {

	private InterfaceController interfaceCtrl;
	private LevelVisuals currentView;
	private StorySlides scenarioDisp;

	private Leaderboard leaderboard;

	private Level levelModel;
	private int currentMode;

	public Timeline timeline;
	public int startTimer = 3;
	public int timeElapsed = 0;
	public int levelWins = 0;

	public boolean paused = false;
	public int exitOption = 0;

	public int levelListIndex =0;
	public ArrayList<LevelVisuals> levelList;
	public ArrayList<Level> modelList;

	public boolean ghostPlayerRed = false;
	public boolean ghostPlayerPink = false;

	private int powerUpTimeOut;


	public LevelController(InterfaceController controller) {
		interfaceCtrl = controller;

		levelList = new ArrayList<LevelVisuals>();
		LevelVisuals primaryLevel = new LevelVisuals(this);
		levelList.add(primaryLevel);
		currentView = primaryLevel;

		scenarioDisp = new StorySlides(this);

		leaderboard = new Leaderboard(this);
		leaderboard.generateLeaderboard();
		
		modelList = new ArrayList<Level>();
		Level primaryModel = new Level();
		modelList.add(primaryModel);
		levelModel = primaryModel;

		timeline = makeTimeline();

	}

	//This timeline ticks every second and controls anything that relies ton time.
	private Timeline makeTimeline() {
		timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				//If the game is not over, plays countdown sound and countdowns from 'initial message'
				//to 'Start!'. (5 ticks total: initial, 3,2,1,Start!)
				if (levelModel.getTimeLimit() != timeElapsed) {
					if (startTimer == 3) {
						currentView.playCountdown();
					}

					//While still in CountDown State
					if (startTimer >= -1) {
						currentView.updateMessage(startTimer);
						if ((startTimer == -1)) {

							currentView.spaceman.start();
							currentView.red.start();
							currentView.pink.start();	//maybe func here
							currentView.blue.start();
							currentView.orange.start();

						}
						startTimer--;

						//In game timer that counts every second
					} else if (levelModel.getTimeLimit() != timeElapsed) {
						currentView.spaceman.start();
						timeElapsed++;
						currentView.updateTime(levelModel.getTimeLimit());
						currentView.updateLives(levelModel.lives);
						//						respawnCollectables();

					}

				//When time runs out, ends the game.
				} else {
					currentView.updateMessage(-1);
					timeline.stop();
					currentView.pauseCountdown();
					currentView.playGameOver();
					currentView.stopAliens();
					currentView.spaceman.stop();
					currentView.gameOverPopUp.setVisible(true);
				}

				//For limited duration powerups eg. time stop
				if (timeElapsed == powerUpTimeOut & timeElapsed != 0) {
					currentView.startAliens();
				}
			}

		});
		timeline.getKeyFrames().add(keyFrame);

		return timeline;
	}

	/* This function initialises the visuals display of a particular mode and map
	 * Inputs: type; Defines type of map and game mode to be read/displayed
	 */
	public void setLevel(int type){
		levelModel.initLevel(type, levelWins);
		currentView.generateMap();
		
		if (currentMode == 3) {
			currentView.updateTime(-1);
		} else if (currentMode == 1) {
			levelModel.initLevel(type+levelWins+1, levelWins);
			currentView.generateMap();
		}
		
		//Setup maps for warp mode
		if ((type == 4) & (levelList.size() == 1)) {
			LevelVisuals secondMap = new LevelVisuals(this);
			Level secondModel = new Level();
			secondModel.initLevel(5, levelWins);
			levelModel = secondModel;
			secondMap.generateMap();
			secondMap.updateMessage(-1);
			modelList.add(secondModel);
			levelList.add(secondMap);

			LevelVisuals thirdMap = new LevelVisuals(this);
			Level thirdModel = new Level();
			thirdModel.initLevel(3,levelWins);
			levelModel = thirdModel;
			modelList.add(thirdModel);
			thirdMap.generateMap();
			thirdMap.updateMessage(-1);
			levelList.add(thirdMap);

			currentView = levelList.get(levelListIndex);
			levelModel = modelList.get(levelListIndex);
		}
		interfaceCtrl.changeScene(currentView.returnScene()); 
	}
	
	
	/*This function is for warp mode where the scene will change when the player walks through a tunnel.
	 * Input: direction, the direction the player is moving (+ve for right, -ve for left)
	 */
	public void changeMap(int direction) {
		
		//When the player moves through the left tunnel
		if (direction < 0) {
			if (levelListIndex == 0) {
				levelListIndex = 2;
			} else {
				levelListIndex--;
			}
			
			
		//When the player moves through the right tunnel
		} else if (direction > 0) {
			if (levelListIndex == 2) {
				levelListIndex = 0;
			} else {
				levelListIndex++;
			}

		}
		currentView.spaceman.setKeyInput(direction); 
		
		//Retains parameters from previous map
		int prevLives = levelModel.getLives();
		int prevScore = levelModel.getScore();
		boolean prevShieldStat = currentView.spaceman.shieldStatus;

		currentView = levelList.get(levelListIndex);
		levelModel = modelList.get(levelListIndex);

		levelModel.setScore(prevScore);
		currentView.updateScore(prevScore);

		levelModel.setLives(prevLives);
		currentView.updateLives(prevLives);
		
		currentView.updateTime(levelModel.getTimeLimit());

		currentView.spaceman.shieldStatus = prevShieldStat;

		interfaceCtrl.changeScene(currentView.returnScene());

		currentView.spaceman.setNewPosition(direction);
		currentView.startAllChars();
		timeline.play();
	}




	/*	Updates the visual display based on where spaceman if going
	 *  Inputs: dx: x direction of spaceman; 1 for right, -1 for left.
	 *  		dx: y direction of spaceman; 1 for up, -1 for down.
	 *  		posX: The current x position on the array of the spaceman
	 *  		posY: The current y position on the array of the spaceman
	 */
	public void updateMap(int dx, int dy,int posX, int posY) {
		//The current tile in front of spaceman
		int checkedTile = levelModel.getCurrentMap().getData(posY+dy, posX+dx);

		//Attempts to hide the pellets if valid
		if (checkedTile == 2) {
			if (currentView.hideCorrespondingPellet(posX+dx, posY + dy)) {
				levelModel.addPoints(10);
				currentView.updateScore(levelModel.getScore());
			}

			//Attempts to hide the powerup if valid
		} else if (checkedTile == 10 || checkedTile == 11 || checkedTile ==12 || checkedTile ==13|| checkedTile ==14) {
			if (currentView.hideCorrespondingPowerUp(posX+dx, posY + dy)) {

				//Logic for consuming star
				if (checkedTile == 10) {
					currentView.playGenericPU();
					currentView.red.changeToFrightMode();
					currentView.pink.changeToFrightMode();
					currentView.blue.changeToFrightMode();
					currentView.orange.changeToFrightMode();
					levelModel.addPoints(50);
					currentView.updateScore(levelModel.getScore());


					//Logic for consuming a heart
				} else if (checkedTile == 11) {
					levelModel.addLives(1);
					currentView.playLifeUp(); 
					currentView.updateLives(levelModel.lives);

					//Logic for consuming cherry
				} else if (checkedTile == 12) {
					currentView.playGenericPU();
					levelModel.addPoints(500);
					currentView.updateScore(levelModel.getScore());

					//Logic for consuming a shield
				} else if (checkedTile == 13) {
					currentView.playShieldSound();
					currentView.spaceman.updateShieldStatus();

					//logic for consuming magic stopwatch
				}  else if (checkedTile == 14) {
					currentView.playStopWatch();
					currentView.stopAliens();
					powerUpTimeOut = timeElapsed + 5;
				}
			}
		}
	}

	private boolean ifSpacemanMetAlien (Alien alien) {
		double alienX = alien.getGraphicalX();
		double spacemanX = currentView.spaceman.getGraphicalX();
		double alienY = alien.getGraphicalY();
		double spacemanY = currentView.spaceman.getGraphicalY();
		
		double dx = Math.abs(spacemanX-alienX);
		double dy = Math.abs(spacemanY-alienY);
		
		if (dx >= 20 || dy >= 20) {
			return false;
		}
		
		if ((dx*dx + dy*dy) <= 20*20) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Checks if spaceman postion is within 20 pixels of a ghost
	 * If so, either consume alien or lose and restart position 
	 */
	public void checkSpacemanAndAliens () {

		Alien[] aliens = currentView.aliens;
		for (Alien i : aliens) {

			if (ifSpacemanMetAlien(i)) {
				//When star powerup is active, alien is consumed
				if (i.frightenedFlag) {
					currentView.playKillSound();
					consumeAlien(i);

					//When shield powerup is active, consumes shield and alien
				} else if (currentView.spaceman.shieldStatus) {
					currentView.spaceman.updateShieldStatus();
					currentView.playKillSound();
					consumeAlien(i);

					//spaceman will be consumed otherwise
				} else {
					currentView.playDeathSound();
					levelModel.minusLives(1);
					currentView.updateLives(levelModel.lives);

					//Resets position and continues if there are still lives
					if (levelModel.lives > 0) {
						currentView.stopAllChars();

						currentView.playCycleSound();
						timeline.pause();

						currentView.spaceman.resetSpaceman();
						currentView.resetAliens();

						startTimer = 3;
						currentView.resetCountdown();
						currentView.countDownView.setVisible(true);
					}

					//Shows gameover screen otherwise
					else if (levelModel.lives == 0) {
						currentView.playGameOver();
						currentView.stopAllChars();
						timeline.pause();

						currentView.spaceman.resetSpaceman();
						currentView.resetAliens();
						currentView.gameFinishedPopUp.setVisible(true);
					}
				}
			}
		}
	}
	
	private void consumeAlien(Alien alien) {
		levelModel.addPoints(200);
		alien.stop();
		alien.resetAlien(false);
		alien.start();
	}

	public int getTimeElapsed() {
		return timeElapsed;
	}
	
	public int getTimeLimit() {
		return levelModel.getTimeLimit();
	}
	
	public int getCountdown() {
		return startTimer;
	}

	public int getMode() {
		return currentMode;
	}
	
	public void respawnCollectables() {
		currentView.respawnPellet();
	}
	
	//Resets the level to start state
	public void resetToStartState() {
		startTimer = 3;
		exitOption = 0;
		timeElapsed = 0;
		
		levelListIndex=0;
		
		ghostPlayerRed = false;
		ghostPlayerPink = false;
		currentView.resetCountdown();
		interfaceCtrl.showHome();
		paused = false;
	}

	public void playStory(int levelWins) {
		scenarioDisp.generateScenario(levelWins);
		interfaceCtrl.changeScene(scenarioDisp.returnScene());
	}

	public void setMode(int mode) {
		currentMode = mode;
	}

	public void setBgView(Image image) {
		currentView.setBg(image);
	}
	
	public void showLeaderboard(int gameMode) {
		leaderboard.generateLeaderboard();
		interfaceCtrl.changeScene(leaderboard.returnScene());
	}

	public LevelVisuals getCurrentView() {
		return currentView;
	}

	public int checkMap(int x, int y) {
		return levelModel.getCurrentMap().getData(y, x);
	}

	public Level getLevel() {
		return levelModel;
	}
	
	//Removes warp maps so they can be re generated
	public void resetWarp() {
		currentView = levelList.get(0);
		if (currentMode == 4) {
			for (int index = 2; index > 0; index--) {
				levelList.remove(index);
				modelList.remove(index);
			}
		}
	}
	
	public Leaderboard getLeaderboard() {
		return leaderboard;
	}
}
