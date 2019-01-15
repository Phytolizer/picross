package picross;

import common.Background;
import common.DrawingTools;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.Color.*;
import static picross.Main.*;
import static picross.PicrossWindow.*;

public class GameWindow extends common.Graphics {

	private static final String VERSION = "1.5.1";
	static int bSize;
	private static int numFrames = 0;
	private final int MIN_BSIZE = 14;
	//base components
	private Grid gameGrid;
	private Grid solutionGrid;
	public Box currBox;
	public int mouseX;
	public int mouseY;
	public int savedMouseX;
	public int savedMouseY;
	public int kbX;
	public int kbY;
	public ControlMode controlMode = ControlMode.MOUSE;
	private int numMistakes;
	private int numFadeFrames = 0;//counts frames for fading effect
	private int fadeAlpha;
	private int cWidth;
	public int sizeX;
	public int sizeY;
	private int fps = 0;
	private int scrollIndex = 0;
	private Scanner s;
	private PicrossWindow currWindow;
	private Stack<PicrossWindow> windows;
	private String status;
	private String gameName = "Picross";
	//region flags
	private boolean playable;
	private boolean faded = false;
	public boolean modifier = false;
	public boolean debugging = false;
	public boolean pushingSolveKey = false;
	private boolean scoreSubmitted = false;
	private boolean competitiveMode = true;
	private boolean showingPausePrompt = false;
	private boolean loginSuccessful = false;
	//endregion
	//graphics
	private Color bgColor = new Color(128, 128, 255);
	static int[] clueLen;
	private Font f;
	//region controls menu elements
	private List<Button> controlsButtons;
	private List<String> controlsDescriptions;
	//endregion
	//region key binds
	public int keyPauseGame;
	public int keyUp;
	public int keyLeft;
	public int keyDown;
	public int keyRight;
	public int keyResolve1;
	public int keyResolve2;
	public int keyGamba;
	//endregion
	private AllButtons allButtons;
	//region button categories
	private ButtonList mainMenuButtons;
	private ButtonList gameChoiceButtons;
	private ButtonList loadMenuButtons;
	private ButtonList sizePickerButtons;
	private ButtonList gameButtons;
	private ButtonList pauseMenuButtons;
	private ButtonList optionsMenuButtons;
	private ButtonList controlsMenuButtons;
	private ButtonList gameEndButtons;
	private ButtonList puzzleButtons;//load screen entries (puzzles to choose from)
	//endregion
	//region buttons
	public Button bPause;
	private Button bResume;
	private Button bNewPuzzle;
	private Button bLeaderboard;
	private Button bXUp;
	private Button bXDown;
	private Button bYUp;
	private Button bYDown;
	private Button bBack;
	private Button bStart;
	private Button bMainMenu;
	private Button bMainMenu2;
	private Button bQuitGame;
	private Button bBegin;
	private Button bRegenPuzzle;
	private Button bControlsMenu;
	private Button bCreator;
	private Button bRandomPuzzle;
	private Button bLoadPuzzle;
	private Button bRestoreControls;
	public Button bGamba;
	//endregion
	//region check boxes
	private CheckBox competitiveModeToggle;
	//endregion

	public GameWindow() {
		super("Loading...");
		if (Math.random() < 0.05) {
			gameName = "Pillsbury";
		}
		FPSCounter.begin();
		//initialize frame & basic flags
		frame.setSize(800, 600);
		PicrossKeyHandler keyHandler = new PicrossKeyHandler();
		frame.setKeyHandler(keyHandler);
		//basic window flags
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("resources/icon.png"));
		setVisible(true);
		//important flags to determine what is displayed on screen
		playable = false;
		status = "menu";
		currWindow = MENU;
		windows = new Stack<>();
		windows.push(currWindow);
		//grab size from file
		if (prefs.get("size").equals("0,0") || prefs.get("size").equals("null")) {
			prefs.put("size", "10,10");
		}
		String size = prefs.get("size");
		sizeX = Integer.parseInt(size.substring(0, size.indexOf(',')));
		sizeY = Integer.parseInt(size.substring(size.indexOf(',') + 1));
		//initializes currBox so the game doesn't freak out
		currBox = null;
		//buttons, sliders, and checkboxes
		displayStatus("Creating buttons...");
		initButtons();
		initControls();
		competitiveModeToggle = new CheckBox(width - 35, height - 35, 25, false);
		displayStatus("Setting up graphics...");
	}

	//region unused
	@Override
	public void windowActivated(WindowEvent arg0) {

	}
	//endregion

	@Override
	public void windowClosing(WindowEvent arg0) {
		writePrefs();
		frame.setVisible(false);
		running = false;
		frame.dispose();
		done = true;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		try {
			if (!competitiveMode) {
				doClickAction(bPause);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//region unused
	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}
	//endregion

	public void submitScore() {
		String username = Main.lb.username;
		String password = Main.lb.password;
		double time = (double) (Main.timer.getMS()) / 1000;
		URL url;
		System.out.println("Sending score to server...");
		try {
			url = new URL("https://westonreed.com/picross/addscore.php?username=" + username + "&password=" + password + "&time=" + time + "&size=" + sizeX + "x" + sizeY + "&version=" + VERSION);
			url.openStream();
		} catch (IOException e) {
			//Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, e);
			try {
				//If the first try fails, try again over http.
				System.out.println("Trying again over HTTP...");
				url = new URL("http://westonreed.com/picross/addscore.php?username=" + username + "&password=" + password + "&time=" + time + "&size=" + sizeX + "x" + sizeY + "&version=" + VERSION);
				url.openStream();
			} catch (IOException ex) {
				Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
		scoreSubmitted = true;
	}

	@Override
	public void runActions() {
		Background.updateColor();
		bgColor = Background.getCurrentColor();
		updateSize();
		int i;
		if (currWindow == GAME) {
			if (playable /*&& !competitiveMode*/) {
				bPause.setVisible(true);

			} else {
				bBegin.setPos(width / 2 - 100, height / 2 - 50);
				bResume.setPos(width / 2 - 100, height / 2 + 7);
				bMainMenu.setPos(width / 2 - 100, height / 2 + 7);
				bMainMenu2.setPos(width / 2, height / 2 + 7);
				bRegenPuzzle.setPos(width / 2, height / 2 + 7);
				bCreator.setPos(width / 2 - 100, bCreator.getY());
			}
			//get size of each box for optimal display size, takes into account clueLen and mistakes box
			bSize = getBoxSize();

			frame.setMinimumSize(new Dimension(
					245 + getStrLen("TIME: " + timer.toString(false), 20f) + 25
					> clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25
							? 245 + getStrLen("TIME: " + timer.toString(false), 20f) + 25
							: clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25,
					clueLen[1] + MIN_BSIZE * gameGrid.sizeY + 50
			));
			//check for completeness
			boolean temp = true;
			for (i = 0; i < gameGrid.sizeX; i++) {
				for (int j = 0; j < gameGrid.sizeY; j++) {
					if (gameGrid.getBox(i, j).getState() != 1
						&& solutionGrid.getBox(i, j).getState() == 1) {
						temp = false;
					}
				}
			}
			if (temp) {
				status = "solved";
				allButtons.setWindow(GAME_END);
				playable = false;
				if (timer != null) {
					timer.pause();
				}
				if (!scoreSubmitted && competitiveMode) {
					submitScore();
				}
			}
			//maximum mistakes
			if (numMistakes == 5) {
				status = "failed";
				allButtons.setWindow(GAME_END);
				playable = false;
				timer.pause();
			}
			//maximum time
			if (timer.getHours() > 9) {
				status = "failed";
				playable = false;
				timer.pause();
			}
			if (status.equals("paused") && !animator.isRunning()) {
				animator.begin();
			} else if (!status.equals("paused") && animator.isRunning()) {
				animator.reset();
			}
			if (controlMode == ControlMode.KEYBOARD) {
				int mouseDiffX = Math.abs(mouseX - savedMouseX);
				int mouseDiffY = Math.abs(mouseY - savedMouseY);
				if (mouseDiffX > 10 || mouseDiffY > 10) {
					controlMode = ControlMode.MOUSE;
					System.out.println("Entering mouse control mode");
				}
			}
			int promptDelay = 5;
			if (showingPausePrompt && promptTimer.getSeconds() >= promptDelay) {
				showingPausePrompt = false;
				promptTimer.reset();
			}
		} else if (currWindow == GAMEMODE) {
			gameChoiceButtons.setVisible(true);
			if (competitiveMode) {
				bLoadPuzzle.setVisible(false);
			}
			bRandomPuzzle.setPos(width / 2 - 100, bRandomPuzzle.getY());
			bLoadPuzzle.setPos(width / 2 - 100, bLoadPuzzle.getY());
		} else if (currWindow == SIZE_PICKER) {
			frame.setMinimumSize(new Dimension(
					getStrLen("SIZE PICKER", 50f) + bBack.getSize().width * 2 + 25,
					100 + 50 + 10 + 50 + 60 + 250
			));
			int freeSpace = height - 100 - bStart.getSize().height - 150;
			bXUp.setVisible(true);
			bXUp.setPos(width / 2 - 200, freeSpace / 2 - 55 + 160);
			bXDown.setVisible(true);
			bXDown.setPos(bXUp.getX(), bXUp.getY() + 110);
			bYUp.setVisible(true);
			bYUp.setPos(width / 2 + 100, freeSpace / 2 - 55 + 160);
			bYDown.setVisible(true);
			bYDown.setPos(bYUp.getX(), bYUp.getY() + 110);
			bBack.setVisible(true);
			bStart.setVisible(true);
			bStart.setPos(width / 2 - 50, height - 100);
			if (sizeX > 25) {
				sizeX = 25;
			}
			if (sizeX < 1) {
				sizeX = 1;
			}
			if (competitiveMode && sizeX < 5) {
				sizeX = 5;
			}
			if (sizeY > 25) {
				sizeY = 25;
			}
			if (sizeY < 1) {
				sizeY = 1;
			}
			if (competitiveMode && sizeY < 5) {
				sizeY = 5;
			}
		} else if (currWindow == MENU) {
			mainMenuButtons.setVisible(true);
			if (f != null) {
				frame.setMinimumSize(new Dimension(getStrLen("MAIN MENU", 50f) + 25, 550));
			}
			bNewPuzzle.setPos(width / 2 - 100, bNewPuzzle.getY());
			bLeaderboard.setPos(width / 2 - 100, bLeaderboard.getY());
			bQuitGame.setPos(width / 2 - 100, bQuitGame.getY());
			bControlsMenu.setPos(width / 2 - 100, bControlsMenu.getY());
			bCreator.setPos(width / 2 - 100, bCreator.getY());
			competitiveModeToggle.setPos(width - 35, height - 35);
			if (competitiveMode != competitiveModeToggle.isChecked()) {
				competitiveMode = competitiveModeToggle.isChecked();
			}
		} else if (currWindow == OPTIONS) {
			bBack.setVisible(true);
		} else if (currWindow == CONTROLS) {
			i = 0;
			for (Button b : controlsMenuButtons.toList()) {
				if (b instanceof ControlsButton) {
					b.setPos(100, 150 + (50 * i));
					i++;
				}
			}
			bRestoreControls.setPos(width - 150 - 10, bRestoreControls.getY());
		} else if (currWindow == LOAD) {
			puzzleButtons.setVisible(false);
			for (i = scrollIndex;
				 i < scrollIndex + (puzzleButtons.size() >= 5 ? 5 : puzzleButtons.size()); i++) {
				Button b = puzzleButtons.get(i);
				b.setX(50);
				int workingHeight = height - 150;
				int topBuffer     = 100;
				b.setY(workingHeight * (i - scrollIndex) / 5 + topBuffer + 25);
				b.setSizeX(width - 100);
				b.setSizeY(workingHeight / 5 - 25);
				b.setVisible(true);
			}
		}
		if (!loginSuccessful) {
			if (Main.lb.username != null) {
				loginSuccessful = true;
				competitiveModeToggle.setChecked(true);
			}
			if (competitiveModeToggle.isChecked()) {
				competitiveModeToggle.setChecked(false);
			}
		}
		mouseX = frame.mouseX;
		mouseY = frame.mouseY;
		mouseActions();
		draw();
		try {
			Thread.sleep(10);
			numFrames++;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Renders an image of the game.
	 */
	private void draw() {
		startDraw();
		switch (currWindow) {
			case MENU:
				frame.setTitle("Main Menu  | " + gameName);
				graphics2D.setColor(bgColor);
				//graphics2D.setColor(getRandomColor());
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(BLACK);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "MAIN MENU", width / 2, 100, graphics2D);
				graphics2D = setFont(20f, graphics2D);
				graphics2D.drawString("v" + VERSION, 15, height - 15);
				competitiveModeToggle.draw(mouseX, mouseY, graphics2D);
				graphics2D.setColor(black);
				DrawingTools.drawRightText(f, "Competitive Mode", width - 35, height - 15, graphics2D);
				break;
			case OPTIONS:
				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(BLACK);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "OPTIONS", width / 2, 100, graphics2D);
				break;
			case GAMEMODE:
				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(BLACK);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "CHOOSE GAMEMODE", width / 2, 100, graphics2D);
				break;
			case SIZE_PICKER:
				frame.setTitle("Size Picker  | " + gameName);
				//noinspection Duplicates
				if (sizeX == 25 || (modifier && sizeX + 5 > 25)) {
					bXUp.setVisible(false);
				} else if (sizeX == 1 || (modifier && sizeX - 5 < 1) || (competitiveMode && (sizeX <= 5 || modifier && sizeX - 5 < 5))) {
					bXDown.setVisible(false);
				} else {
					bXUp.setVisible(true);
					bXDown.setVisible(true);
				}
				//noinspection Duplicates
				if (sizeY == 25 || (modifier && sizeY + 5 > 25)) {
					bYUp.setVisible(false);
				} else if (sizeY == 1 || (modifier && sizeY - 5 < 1) || (competitiveMode && (sizeY <= 5 || modifier && sizeY - 5 < 5))) {
					bYDown.setVisible(false);
				} else {
					bYUp.setVisible(true);
					bYDown.setVisible(true);
				}

				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(BLACK);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "SIZE PICKER", width / 2, 100, graphics2D);
				DrawingTools.drawCenteredText(f, "X", bXUp.getX() + bXUp.getSize().width / 2, bXUp.getY() - 10, graphics2D);
				DrawingTools.drawCenteredText(f, "Y", bYUp.getX() + bYUp.getSize().width / 2, bYUp.getY() - 10, graphics2D);
				DrawingTools.drawCenteredText(f, Integer.toString(sizeX), bXUp.getX() + bXUp.getSize().width / 2, bXDown.getY() - 10, graphics2D);
				DrawingTools.drawCenteredText(f, Integer.toString(sizeY), bYUp.getX() + bYUp.getSize().width / 2, bYDown.getY() - 10, graphics2D);
				graphics2D = setFont(20f, graphics2D);
				break;
			case GAME:
				frame.setTitle("" + Main.timer.toString(true) + "  | " + gameName);
				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				if (playable) {
					graphics2D.setColor(fadeOff(64, 100));
					graphics2D.fillRect(0, 0, width, height);
				}
				graphics2D = setFont(12f, graphics2D);//12x7 pixels

				graphics2D.setColor(BLACK);
				cWidth = width / 2 - (gameGrid.sizeX * bSize / 2) - clueLen[0];
				if (cWidth < 0) {
					cWidth = 0;
				}
				for (int i = 0; i < (gameGrid.sizeX); i++) {
					for (int j = 0; j < (gameGrid.sizeY); j++) {
						gameGrid.drawGrid(i, j, graphics2D, cWidth);
						graphics2D.setColor(BLACK);
						//if(playable && i == (mouseX - clueLen[0] - cWidth) / bSize && j == (mouseY - clueLen[1]) / bSize && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1]) {
						if (playable && currBox != null && i == currBox.getPos()[0] && j == currBox.getPos()[1]) {
							graphics2D.setColor(new Color(0, 0, 0, 64));
							graphics2D.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
							//} else if(playable && ((i == (mouseX - clueLen[0] - cWidth) / bSize && mouseX > clueLen[0] + cWidth) || (j == (mouseY - clueLen[1]) / bSize && mouseY > clueLen[1]))) {
						} else if (controlMode == ControlMode.KEYBOARD) {
							if (playable && currBox != null && (i == currBox.getPos()[0] || j == currBox.getPos()[1])) {
								graphics2D.setColor(new Color(0, 0, 0, 32));
								graphics2D.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
							}
						} else if (playable && ((i == (mouseX - clueLen[0] - cWidth) / bSize && mouseX > clueLen[0] + cWidth) || (j == (mouseY - clueLen[1]) / bSize && mouseY > clueLen[1]))) {
							graphics2D.setColor(new Color(0, 0, 0, 32));
							graphics2D.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						}
						graphics2D.setColor(BLACK);
						graphics2D.drawRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
					}
				}
				if (!status.equals("paused") && !status.equals("get ready")) {
					for (int i = 0; i < gameGrid.sizeX; i++) {
						Clue cTemp = new Clue(i, 1);
						cTemp.generateClue(gameGrid);
						if (cTemp.getValues().equals(gameGrid.cluesY[i].getValues())) {
							graphics2D.setColor(new Color(0, 0, 0, 128));
							for (int box = 0; box < gameGrid.sizeY; box++) {
								Box b = gameGrid.getBox(i, box);
								if (b.getState() == 0) {
									b.impossibru();
								}
							}
						} else {
							graphics2D.setColor(BLACK);
						}
						gameGrid.drawClues(i, 1, graphics2D, cWidth);
					}
					for (int j = 0; j < gameGrid.sizeY; j++) {
						Clue cTemp = new Clue(j, 0);
						cTemp.generateClue(gameGrid);
						if (cTemp.getValues().equals(gameGrid.cluesX[j].getValues())) {
							graphics2D.setColor(new Color(0, 0, 0, 128));
							for (int box = 0; box < gameGrid.sizeX; box++) {
								Box b = gameGrid.getBox(box, j);
								if (b.getState() == 0) {
									b.impossibru();
								}
							}
						} else {
							graphics2D.setColor(BLACK);
						}
						gameGrid.drawClues(j, 0, graphics2D, cWidth);
					}
				}
				for (int i = 5; i < gameGrid.sizeX; i += 5) {
					graphics2D.drawLine(clueLen[0] + i * bSize + 1 + cWidth, clueLen[1], clueLen[0] + i * bSize + 1 + cWidth, clueLen[1] + gameGrid.sizeY * bSize);
				}
				for (int i = 5; i < gameGrid.sizeY; i += 5) {
					graphics2D.drawLine(clueLen[0] + cWidth, clueLen[1] + i * bSize + 1, clueLen[0] + gameGrid.sizeX * bSize + cWidth, clueLen[1] + i * bSize + 1);
				}
				graphics2D = setFont(20f, graphics2D);
				graphics2D.drawString("MISTAKES: ", 10, height - 15);
				int xRendered = 0,
						mistakesTemp = numMistakes;
				graphics2D.drawRect(120, height - 35, 125, 25);
				graphics2D.setColor(RED);
				while (mistakesTemp > 0 && xRendered < 5) {
					graphics2D.drawString("X", xRendered * 25 + 125, height - 15);
					mistakesTemp--;
					xRendered++;
				}
				while (xRendered < 5) {
					graphics2D.setColor(new Color(0, 0, 0, 64));
					graphics2D.drawString("X", xRendered * 25 + 125, height - 15);
					xRendered++;
				}
				if (!playable) {
					bPause.setVisible(false);
					if (bBegin.isVisible()) {
						faded = true;
					}
					graphics2D.setColor(fadeOn(64, 100));
					graphics2D.fillRect(0, 0, width, height);
					graphics2D.setColor(WHITE);
					graphics2D.fillRect(width / 2 - 100, height / 2 - 50, 200, 100);
					graphics2D.setColor(BLACK);
					graphics2D.drawRect(width / 2 - 100, height / 2 - 50, 200, 100);
					String showText = "";
					graphics2D = setFont(30f, graphics2D);
					switch (status) {
						case "solved":
							graphics2D.setColor(Color.black);
							graphics2D = setFont(20f, graphics2D);
							graphics2D.setColor(GREEN);
							showText = "SOLVED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "failed":
							graphics2D.setColor(RED);
							showText = "FAILED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "paused":
							if (Main.animator.getMS() % 1000 <= 500) {
								DrawingTools.drawCenteredText(f, "PAUSED", width / 2, height / 2 - 10, graphics2D);
							}
							bResume.draw(graphics2D);
							bMainMenu.draw(graphics2D);
							break;
					}
					if (status.equals("get ready")) {
						bBegin.draw(graphics2D);
					}
					graphics2D = setFont(30f, graphics2D);
					DrawingTools.drawCenteredText(f, showText, width / 2, height / 2 - 10, graphics2D);
					graphics2D.setColor(BLACK);
					//if(!status.equals("get ready") && !status.equals("paused"))
					//graphics2D.drawString("TIME:" + Main.timer.toString(), width / 2 - 45, height / 2 - 12);
				}
				//render mistakes/timer
				graphics2D = setFont(20f, graphics2D);
				graphics2D.setColor(BLACK);
				DrawingTools.drawRightText(f, "TIME: " + Main.timer.toString(false), width, height - 15, graphics2D);
				if (showingPausePrompt) {
					graphics2D = setFont(20f, graphics2D);
					graphics2D.drawString("Press the pause button again to confirm disabling Competitive Mode", bPause.getX() + bPause.getSize().width + 15, bPause.getY() + bPause.getSize().height / 2 + 10);
				}
				break;
			case CONTROLS:
				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(black);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "CONTROLS", width / 2, 100, graphics2D);
				graphics2D.drawRect(100, 150, width - 200, height - 250);
				frame.setMinimumSize(new Dimension(100 + 100 + 10 + getMaxStrLen(controlsDescriptions, 25f) + 100, 150 + (50 * (controlsMenuButtons.size() - 2)) + 100));
				graphics2D = setFont(25f, graphics2D);
				for (int i = 0; i < controlsDescriptions.size(); i++) {
					graphics2D.drawString(controlsDescriptions.get(i), 210, 150 + 50 * (i + 1) - 15);
				}
				break;
			case LOAD:
				graphics2D.setColor(bgColor);
				graphics2D.fillRect(0, 0, width, height);
				graphics2D.setColor(BLACK);
				graphics2D = setFont(50f, graphics2D);
				DrawingTools.drawCenteredText(f, "LOAD A PUZZLE", width / 2, 100, graphics2D);
				loadMenuButtons.drawAll(graphics2D);
				puzzleButtons.drawAll(graphics2D);
				break;
		}
		allButtons.drawButtons(graphics2D);
		if (Main.FPSCounter.getMS() > 1000) {
			Main.FPSCounter.begin();
			fps = numFrames;
			numFrames = 0;
		}
		graphics2D.setColor(black);
		graphics2D = setFont(12f, graphics2D);
		if (debugging) {
			graphics2D.drawString("" + fps + " FPS", 20, 50);
		}
		endDraw();
	}

	/**
	 * Creates a Grid with random states of size sizeX, sizeY.
	 */
	private void getSolution() {
		//mouseX = Integer.parseInt(s.nextLine());
		//mouseY = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				//int b = s.nextInt();
				Random random = new Random();
				int b = random.nextInt(2);
				if (b == 1) {
					solutionGrid.getBox(i, j).setState(1);
				}
			}
		}
	}

	/**
	 * @return Returns the side length of a box in pixels based on graphics
	 * elements in game and frame size
	 */
	private int getBoxSize() {
		int temp = (width - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (gameGrid.sizeX + 1);
		if (temp * gameGrid.sizeY + clueLen[1] + 50 > height) {
			return (height - 50 - clueLen[1]) / gameGrid.sizeY > MIN_BSIZE ? (height - 50 - clueLen[1]) / gameGrid.sizeY : MIN_BSIZE;
		} else {
			return temp > MIN_BSIZE ? temp : MIN_BSIZE;
		}
	}

	/**
	 * Writes generated clues of a random puzzle to a file, to be read by the
	 * solver program.
	 */
	private void writeClues() {
		try {
			FileWriter writer = new FileWriter("clues.nin");
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for (Clue c : gameGrid.cluesX) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			for (Clue c : gameGrid.cluesY) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs any actions regarding mouse clicks that are not handled by the
	 * Button class. Includes gameplay and scrolling on the size picker.
	 */
	private void mouseActions() {
		if (frame.hasClicked()) {
			frame.setHasClicked(false);
		}
		switch (currWindow) {
			case GAME:
				if (controlMode == ControlMode.MOUSE) {
					//bound checking to prevent instant toggling of a flag
					if (currBox != null && (mouseX - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (mouseY - clueLen[1]) / bSize < gameGrid.sizeY && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1] && currBox != gameGrid.getBox((mouseX - clueLen[0] - cWidth) / bSize, (mouseY - clueLen[1]) / bSize)) {
						currBox.setCanModify(true);
					}
					//get box only if mouse is within game grid, otherwise it is null
					if ((mouseX - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (mouseY - clueLen[1]) / bSize < gameGrid.sizeY && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1]) {
						currBox = gameGrid.getBox((mouseX - clueLen[0] - cWidth) / bSize, (mouseY - clueLen[1]) / bSize);
					} else {
						currBox = null;
					}
					if (currBox != null) {
						if (frame.clicking()) {
							//only disables boxes as the player attempts to modify them
							if (!playable) {
								currBox.setCanModify(false);
							}
							//left click = reveal
							if (frame.getMouseButton() == 3) {
								currBox.impossibru();
								currBox.setCanModify(false);
							} else if (frame.getMouseButton() == 1) {
								//click buttons
								//if the box is not part of the solution, you made a mistake
								if (!currBox.green(solutionGrid)) {
									numMistakes++;
									Main.timer.addSeconds(10 * numMistakes);
									currBox.setCanModify(false);
								}
								currBox.setCanModify(false);
							}
							//right click = flag, is not checked with the solution to prevent cheating

						} else {
							currBox.setCanModify(true);
						}
					}
				} else if (controlMode == ControlMode.KEYBOARD) {
					currBox = gameGrid.getBox(kbX, kbY);
					if (pushingSolveKey && currBox.canModify()) {
						if (!modifier && !currBox.green(solutionGrid)) {
							numMistakes++;
							Main.timer.addSeconds(10 * numMistakes);
						} else if (modifier) {
							currBox.impossibru();
						}
						currBox.setCanModify(false);
					}
				}
				break;
			case MENU:
				//no special actions for menu
				break;
			case SIZE_PICKER:
				if (frame.scrollAmt != 0) {
					if (isInBounds(bXUp.getX(), bXUp.getY() + bXUp.getSize().height, bXUp.getX() + bXUp.getSize().width, bXUp.getY() + bXUp.getSize().height + 60)) {
						int min = competitiveMode ? 4 : 0;
						if (modifier) {
							sizeX -= sizeX - (5 * frame.scrollAmt) > min && sizeX - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeX -= sizeX - frame.scrollAmt > min && sizeX - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					} else if (isInBounds(bYUp.getX(), bYUp.getY() + bYUp.getSize().height, bYUp.getX() + bYUp.getSize().width, bYUp.getY() + bYUp.getSize().height + 60)) {
						int min = competitiveMode ? 4 : 0;
						if (modifier) {
							sizeY -= sizeY - (5 * frame.scrollAmt) > min && sizeY - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeY -= sizeY - frame.scrollAmt > min && sizeY - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					}
					frame.scrollAmt = 0;
				}
				break;
			case LOAD:
				if (frame.scrollAmt != 0) {
					scrollIndex += (scrollIndex + frame.scrollAmt >= 0 && scrollIndex + frame.scrollAmt <= puzzleButtons.size() - 5 ? frame.scrollAmt : 0);

					frame.scrollAmt = 0;
				}
				break;
		}
	}

	private void generatePuzzle() {
		List<String> output;
		int numSolutions;
		int numTries = 0;
		do {
			numTries++;
			getSolution();
			gameGrid.generateClues(solutionGrid);
			writeClues();
			runSolver("clues.nin");
			do {
				output = LogStreamReader.output;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (output.size() < 5);
			int solutionsLine;
			do {
				solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
			} while (Strings.findLineWith(output, "Solutions : ", true) == -1);
			numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
			System.out.println(output.get(solutionsLine));
			//String difficulty = "";
			//int diffLine = Strings.findLineWith(output, "Decisions : ", true);
			//difficulty = output.get(diffLine).substring(12, output.get(diffLine).length());
			//Integer.parseInt(difficulty);
		} while (numSolutions > 1);
		System.out.println("Generated puzzle in " + numTries + " " + (numTries == 1 ? "try." : "tries."));
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for (int i = 0; i < gameGrid.sizeY; i++) {
			if (gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if (clueLen[0] < 100) {
			clueLen[0] = 100;
		}
		for (int i = 0; i < gameGrid.sizeX; i++) {
			if (gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if (clueLen[1] < 130) {
			clueLen[1] = 130;
		}
		try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("clues.nin"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadPuzzle(String name) {
		frame.setTitle("LOADING...");
		displayStatus("Loading custom puzzle...");
		windows.push(currWindow);
		currWindow = GAME;
		allButtons.setWindow(currWindow);
		status = "get ready";
		bBegin.setVisible(true);
		bPause.setVisible(false);
		numMistakes = 0;
		bRegenPuzzle.setVisible(false);
		bMainMenu.setVisible(false);
		playable = false;
		List<String> output = new ArrayList<>();
		File puzzleFile = new File("." + slashCharacter + "saves" + slashCharacter + name + ".nin");
		try {
			s = new Scanner(puzzleFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (s.hasNext()) {
			String size = s.nextLine();
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(' ')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(' ') + 1, size.length()));
			gameGrid = new Grid(sizeX, sizeY);
		}
		runSolver("." + slashCharacter + "saves" + slashCharacter + name + ".nin");
		List<String> prevOutput;
		do {
			prevOutput = output;
			output = LogStreamReader.output;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!output.equals(prevOutput));
		List<String> puzzle = new ArrayList<>();
		int startIndex = 0;
		for (int i = 0; i < output.size(); i++) {
			puzzle.add(output.get(i));
			if (output.get(i).contains("hash misses")) {
				startIndex = i + 2;
			}
		}
		puzzle.remove(startIndex + sizeY);
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for (int i = 0; i < sizeY; i++) {
			System.out.println(puzzle.get(i + startIndex));
			for (int j = 0; j < sizeX; j++) {
				char currCheck = puzzle.get(i + startIndex).charAt(j);
				solutionGrid.getBox(j, i).setState(currCheck == '#' ? 1 : 0);
			}
		}
		gameGrid.generateClues(solutionGrid);
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for (int i = 0; i < gameGrid.sizeY; i++) {
			if (gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if (clueLen[0] < 100) {
			clueLen[0] = 100;
		}
		for (int i = 0; i < gameGrid.sizeX; i++) {
			if (gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if (clueLen[1] < 130) {
			clueLen[1] = 130;
		}
	}

	/**
	 * Performs a predetermined action based on the button passed.
	 *
	 * @param b button to be compared with known buttons
	 */
	void doClickAction(Button b) {
		if (b != bXUp && b != bLeaderboard && b != bXDown && b != bYUp && b != bYDown && b != bBegin && b != bPause && !(b instanceof ControlsButton)) {
			displayStatusNoBG("Loading...");
		}
		if (b == bNewPuzzle) {
			windows.push(currWindow);
			if (competitiveMode) {
				currWindow = SIZE_PICKER;
			} else {
				currWindow = GAMEMODE;
			}
			allButtons.setWindow(currWindow);
			//get size from settings file

		} else if (b == bLeaderboard) {
			displayStatusNoBG("Opening Browser...");
			try {
				Desktop.getDesktop().browse(new URL("https://westonreed.com/picross/leaderboard.php").toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (b == bRandomPuzzle) {
			windows.push(currWindow);
			currWindow = SIZE_PICKER;
			allButtons.setWindow(currWindow);
			String size = prefs.get("size");
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(',')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(',') + 1));
			if (sizeX == 0) {
				sizeX = 10;
			}
			if (sizeY == 0) {
				sizeY = 10;
			}
		} else if (b == bResume) {
			status = "";
			allButtons.setWindow(GAME);
			bResume.setVisible(false);
			bMainMenu2.setVisible(false);
			bPause.setVisible(true);
			Main.timer.resume();
			playable = true;
			faded = false;
		} else if (b == bPause) {
			if (status.equals("") && !competitiveMode) {
				allButtons.setWindow(PAUSE);
				status = "paused";
				bPause.setVisible(false);
				bResume.setVisible(true);
				bMainMenu2.setVisible(true);
				Main.timer.pause();
				playable = false;
				faded = false;
			} else if (competitiveMode) {
				if (showingPausePrompt) {
					showingPausePrompt = false;
					competitiveMode = false;
					competitiveModeToggle.setChecked(false);
					doClickAction(b);
				} else {
					showingPausePrompt = true;
					Main.promptTimer.begin();
				}
			}
		} else if (b == bXUp) {
			if (modifier) {
				sizeX += 5;
			} else {
				sizeX++;
			}
		} else if (b == bXDown) {
			if (modifier) {
				sizeX -= 5;
			} else {
				sizeX--;
			}
		} else if (b == bYUp) {
			if (modifier) {
				sizeY += 5;
			} else {
				sizeY++;
			}
		} else if (b == bYDown) {
			if (modifier) {
				sizeY -= 5;
			} else {
				sizeY--;
			}
		} else if (b == bBack) {
			currWindow = windows.pop();
			allButtons.setWindow(currWindow);
		} else if (b == bStart || b == bRegenPuzzle) {
			frame.setTitle("GENERATING...");
			displayStatus("Generating random puzzle...");
			b.setVisible(false);
			windows.push(currWindow);
			currWindow = GAME;
			allButtons.setWindow(currWindow);
			status = "get ready";
			bBegin.setVisible(true);
			bPause.setVisible(false);
			numMistakes = 0;
			bRegenPuzzle.setVisible(false);
			bMainMenu.setVisible(false);
			playable = false;
			kbX = 0;
			kbY = 0;
			generatePuzzle();
			Main.timer.reset();
			scoreSubmitted = false;
		} else if (b == bMainMenu || b == bMainMenu2) {
			windows = new Stack<>();
			frame.setTitle("Main Menu  | " + gameName);
			currWindow = MENU;
			windows.push(currWindow);
			allButtons.setWindow(currWindow);
			status = "menu";
			numMistakes = 0;
			playable = false;
			scoreSubmitted = false;
		} else if (b == bQuitGame) {
			frame.setTitle("Quitting...");
			writePrefs();
			frame.setVisible(false);
			running = false;
			frame.dispose();
			done = true;
			System.exit(0);
		} else if (b == bBegin) {
			b.setVisible(false);
			status = "";
			Main.timer.begin();
			playable = true;
			faded = false;
		} else if (b == bControlsMenu) {
			windows.push(currWindow);
			currWindow = CONTROLS;
			allButtons.setWindow(currWindow);
		} else if (b == bCreator) {
			runCreator();
		} else if (b == bLoadPuzzle) {
			windows.push(currWindow);
			currWindow = LOAD;
			allButtons.setWindow(currWindow);
			loadMenuButtons.setVisible(true);
			//get all puzzles
			List<String> puzzleNames = getPuzzleNames();
			scrollIndex = 0;
			Button[] pButtons = new Button[getNumPuzzles()];
			for (int i = 0; i < getNumPuzzles(); i++) {
				pButtons[i] = new Button(this);
				pButtons[i].setText(puzzleNames.get(i).substring(0, puzzleNames.get(i).length() - 4));
			}
			puzzleButtons = new ButtonList(LOAD);
			puzzleButtons.addButtons(pButtons);
			puzzleButtons.sort();
			puzzleButtons.setVisible(true);
		} else if (b == bRestoreControls) {
			keyPauseGame = KeyEvent.VK_ESCAPE;
			keyUp = KeyEvent.VK_UP;
			keyLeft = KeyEvent.VK_LEFT;
			keyDown = KeyEvent.VK_DOWN;
			keyRight = KeyEvent.VK_RIGHT;
			keyResolve1 = KeyEvent.VK_SPACE;
			keyResolve2 = KeyEvent.VK_ENTER;
			keyGamba = KeyEvent.VK_G;
			updateButtons("controls");
			writePrefs();
		} else if (b instanceof ControlsButton) {
			PicrossKeyHandler pkh = (PicrossKeyHandler) frame.getKeyHandler();
			HashMap<String, Button> controlsButtons = new HashMap<>();
			for (Button b1 : controlsMenuButtons.toList()) {
				if (b1 instanceof ControlsButton) {
					controlsButtons.put(((ControlsButton) b1).getLabel(), b1);
				}
			}
			if (pkh.getKeyAssigning() != null) {
				//return previously assigning button's key to normal
				updateButtons("controls");
			}
			pkh.setKeyAssigning(((ControlsButton) b).getLabel());
			b.setText("Press a key");
			System.out.println("Assigning a key to label " + pkh.getKeyAssigning());
		} else {
			for (int i = 0; i < puzzleButtons.size(); i++) {
				if (b == puzzleButtons.get(i)) {
					loadPuzzle(b.getText());
				}
			}
		}
	}

	private int[] findGoodBadSquaresRemaining() {
		int[] numGoodBad = {0, 0};
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				if (gameGrid.getBox(i, j).getState() == Box.EMPTY) {
					if (solutionGrid.getBox(i, j).getState() == Box.SOLVED) {
						numGoodBad[0]++;
					} else {
						numGoodBad[1]++;
					}
				}
			}
		}
		return numGoodBad;
	}

	public void updateButtons(String window) {
		switch (window) {
			case "controls":
				for (Button b : controlsMenuButtons.toList()) {
					if (b instanceof ControlsButton) {
						switch (((ControlsButton) b).getLabel()) {
							case "pauseGame":
								b.setText(KeyEvent.getKeyText(keyPauseGame));
								break;
							case "up":
								b.setText(KeyEvent.getKeyText(keyUp));
								break;
							case "left":
								b.setText(KeyEvent.getKeyText(keyLeft));
								break;
							case "down":
								b.setText(KeyEvent.getKeyText(keyDown));
								break;
							case "right":
								b.setText(KeyEvent.getKeyText(keyRight));
								break;
							case "resolve1":
								b.setText(KeyEvent.getKeyText(keyResolve1));
								break;
							case "resolve2":
								b.setText(KeyEvent.getKeyText(keyResolve2));
								break;
						}
					}
				}
				break;
		}
	}

	//region unused
	void doSlideAction(@SuppressWarnings("unused") Slider s) {

	}
	//endregion

	/**
	 * Returns a color that slowly darkens to amt.
	 *
	 * @param amt      Amount to darken, from 0-255
	 * @param duration Time in frames to darken
	 * @return Color to cover frame with for a fading effect
	 */
	@SuppressWarnings("SameParameterValue")
	private Color fadeOn(int amt, int duration) {
		duration /= 10;
		if (numFadeFrames >= duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = numFadeFrames * amt / duration;
		Color out = faded ? new Color(0, 0, 0, amt) : new Color(0, 0, 0, fadeAlpha);
		if (!faded) {
			numFadeFrames++;
		}
		return out;
	}

	/**
	 * Returns a color that slowly lightens from amt to 0.
	 *
	 * @param amtInit  Initial darkness, will slowly approach 0
	 * @param duration Time in frames to lighten
	 * @return Color to cover frame with for a fading effect
	 */
	@SuppressWarnings("SameParameterValue")
	private Color fadeOff(int amtInit, int duration) {
		duration /= 10;
		if (numFadeFrames >= duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = amtInit - (numFadeFrames * amtInit / duration);
		Color out = faded ? new Color(0, 0, 0, 0) : new Color(0, 0, 0, fadeAlpha);
		if (!faded) {
			numFadeFrames++;
		}
		return out;
	}

	/**
	 * @param x1 mouseX-coordinate of left bound of rectangle
	 * @param y1 mouseY-coordinate of left bound of rectangle
	 * @param x2 mouseX-coordinate of right bound of rectangle
	 * @param y2 mouseY-coordinate of right bound of rectangle
	 * @return Returns whether the mouse's current positions falls within the
	 * defined bounds.
	 */
	private boolean isInBounds(int x1, int y1, int x2, int y2) {
		return (mouseX > x1) && (mouseX < x2) && (mouseY > y1) && (mouseY < y2);
	}

	private int getStrLen(String s, float fontHeight) {
		FontMetrics fm = frame.getGraphics().getFontMetrics(f.deriveFont(fontHeight));
		return fm.stringWidth(s);
	}

	private int getMaxStrLen(List<String> strings, float fontHeight) {
		int max = 0;
		for (String s : strings) {
			if (getStrLen(s, fontHeight) > max) {
				max = getStrLen(s, fontHeight);
			}
		}
		return max;
	}

	private Graphics2D setFont(float size, Graphics2D art) {
		art.setFont(f.deriveFont(size));
		f = f.deriveFont(size);
		return art;
	}

	private void initButtons() {
		allButtons = new AllButtons();

		mainMenuButtons = new ButtonList(MENU);
		bNewPuzzle = new Button(width / 2 - 100, 150, 200, 65, "Start Game", GREEN, 20, this);
		bNewPuzzle.setVisible(true);
		bLeaderboard = new Button(width / 2 - 100, 225, 200, 65, "Leaderboard", YELLOW, 20, this);
		bLeaderboard.setVisible(true);
		bCreator = new Button(width / 2 - 100, 300, 200, 65, "Creator", new Color(255, 140, 0), 20, this);
		bCreator.setVisible(true);
		bControlsMenu = new Button(width / 2 - 100, 375, 200, 65, "Controls", new Color(0, 50, 255), 20, this);
		bControlsMenu.setVisible(true);
		bQuitGame = new Button(width / 2 - 100, 450, 200, 65, "Quit Game", RED, 20, this);
		bQuitGame.setVisible(true);
		mainMenuButtons.addButtons(new Button[]{bNewPuzzle, bLeaderboard, bCreator, bControlsMenu, bQuitGame});

		gameChoiceButtons = new ButtonList(GAMEMODE);
		bBack = new Button(10, 55, 50, 50, "<", RED, 30, this);
		bRandomPuzzle = new Button(width / 2 - 100, 150, 200, 100, "Random Puzzle", GREEN, 20, this);
		bLoadPuzzle = new Button(width / 2 - 100, 275, 200, 100, "Load Puzzle", YELLOW, 20, this);
		gameChoiceButtons.addButtons(new Button[]{bRandomPuzzle, bLoadPuzzle, bBack});

		loadMenuButtons = new ButtonList(LOAD);
		loadMenuButtons.addButtons(new Button[]{bBack});

		puzzleButtons = new ButtonList(LOAD);

		sizePickerButtons = new ButtonList(SIZE_PICKER);
		bXUp = new Button(300, 400, 100, 50, "Λ", 30, this);
		bXDown = new Button(300, 510, 100, 50, "V", 30, this);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30, this);
		bYDown = new Button(600, 510, 100, 50, "V", 30, this);
		bStart = new Button(width / 2 - 50, height - 100, 100, 75, "GENERATE", GREEN, 30, this);
		sizePickerButtons.addButtons(new Button[]{bXUp, bXDown, bYUp, bYDown, bBack, bStart});

		optionsMenuButtons = new ButtonList(OPTIONS);
		optionsMenuButtons.addButtons(new Button[]{bBack});

		gameButtons = new ButtonList(GAME);
		bPause = new Button(20, 50, 60, 60, "Pause", YELLOW, 17, this);
		gameButtons.addButton(bPause);

		pauseMenuButtons = new ButtonList(PAUSE);
		bResume = new Button(width / 2 - 100, height / 2 + 7, 100, 43, "Resume", GREEN, 17, this);
		bMainMenu = new Button(width / 2 - 100, height / 2 + 7, 100, 43, "Main Menu", bgColor, 17, this);
		bMainMenu2 = new Button(width / 2, height / 2 + 7, 100, 43, "Main Menu", bgColor, 17, this);
		bRegenPuzzle = new Button(width / 2, height / 2 + 7, 100, 43, "New Puzzle", GREEN, 17, this);
		bBegin = new Button(width / 2 - 100, height / 2 - 50, 200, 100, "BEGIN", GREEN, 20, this);
		pauseMenuButtons.addButtons(new Button[]{bResume/*, bMainMenu*/, bMainMenu2/*, bRegenPuzzle*//*, bBegin*/});

		gameEndButtons = new ButtonList(GAME_END);
		gameEndButtons.addButtons(new Button[]{bMainMenu, bRegenPuzzle});

		controlsMenuButtons = new ButtonList(CONTROLS);
		bRestoreControls = new Button(width - 150 - 10, 55, 150, 50, "Restore Defaults", YELLOW, 20, this);
		controlsMenuButtons.addButtons(new Button[]{bBack, bRestoreControls});

		allButtons.addButtonLists(new ButtonList[]{mainMenuButtons, gameChoiceButtons, loadMenuButtons, sizePickerButtons, optionsMenuButtons, gameButtons, gameEndButtons, pauseMenuButtons, controlsMenuButtons});
		allButtons.setWindow(MENU);
	}

	private void initControls() {
		int buttonWidth = 100;
		int buttonHeight = 50;
		controlsButtons = new ArrayList<>();
		controlsDescriptions = new ArrayList<>();
		boolean newControls = false;
		//catch-all for if prefs is not properly initialized
		if (!(prefs.has("pauseGame") && prefs.has("up") && prefs.has("left") && prefs.has("down") && prefs.has("right") && prefs.has("resolve1") && prefs.has("resolve2"))){
			try {
				newControls = true;
				System.out.println("Prefs not initialized! Restoring default controls: ");
				doClickAction(bRestoreControls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//ESC pauses the game
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyPauseGame : Integer.parseInt(prefs.get("pauseGame"))), "pauseGame", 20, this));
		controlsDescriptions.add("Pause game");
		if (!newControls) {
			keyPauseGame = Integer.parseInt(prefs.get("pauseGame"));
		}
		//Up Arrow Key moves the cursor up 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyUp : Integer.parseInt(prefs.get("up"))), "up", 20, this));
		controlsDescriptions.add("Move in-game cursor up");
		if (!newControls) {
			keyUp = Integer.parseInt(prefs.get("up"));
		}
		//Left Arrow Key moves the cursor left 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyLeft : Integer.parseInt(prefs.get("left"))), "left", 20, this));
		controlsDescriptions.add("Move in-game cursor left");
		if (!newControls) {
			keyLeft = Integer.parseInt(prefs.get("left"));
		}
		//Down Arrow Key moves the cursor down 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyDown : Integer.parseInt(prefs.get("down"))), "down", 20, this));
		controlsDescriptions.add("Move in-game cursor down");
		if (!newControls) {
			keyDown = Integer.parseInt(prefs.get("down"));
		}
		//Right Arrow Key moves the cursor right 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyRight : Integer.parseInt(prefs.get("right"))), "right", 20, this));
		controlsDescriptions.add("Move in-game cursor right");
		if (!newControls) {
			keyRight = Integer.parseInt(prefs.get("right"));
		}
		//Enter key marks a block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyResolve1 : Integer.parseInt(prefs.get("resolve1"))), "resolve1", 20, this));
		controlsDescriptions.add("Resolves the current tile");
		if (!newControls) {
			keyResolve1 = Integer.parseInt(prefs.get("resolve1"));
		}
		//Space also marks a block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(newControls ? keyResolve2 : Integer.parseInt(prefs.get("resolve2"))), "resolve2", 20, this));
		controlsDescriptions.add("Secondary key to resolve the current tile");
		if (!newControls) {
			keyResolve2 = Integer.parseInt(prefs.get("resolve2"));
		}

		controlsMenuButtons.addButtons(controlsButtons);
	}

	private void writePrefs() {
		prefs.put("size", "" + sizeX + ',' + sizeY);
		prefs.put("pauseGame", Integer.toString(keyPauseGame));
		prefs.put("up", Integer.toString(keyUp));
		prefs.put("left", Integer.toString(keyLeft));
		prefs.put("down", Integer.toString(keyDown));
		prefs.put("right", Integer.toString(keyRight));
		prefs.put("resolve1", Integer.toString(keyResolve1));
		prefs.put("resolve2", Integer.toString(keyResolve2));
		try {
			prefs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayStatus(String message) {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHints(rh);
		f = art.getFont();
		art.setColor(bgColor);
		art.fillRect(0, 0, width, height);
		art.setColor(black);
		art = setFont(50f, art);
		DrawingTools.drawCenteredText(f, message, width / 2, height / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if (art != null) {
			art.drawImage(imgBuffer, 0, 0, width, height, 0, 0, width, height, null);
			art.dispose();
		}
	}

	private void displayStatusNoBG(String message) {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHints(rh);
		f = art.getFont();
		art.setColor(black);
		art = setFont(50f, art);
		DrawingTools.drawCenteredText(f, message, width / 2, height / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if (art != null) {
			art.drawImage(imgBuffer, 0, 0, width, height, 0, 0, width, height, null);
			art.dispose();
		}
	}
}
