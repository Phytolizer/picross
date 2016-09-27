package picross;//TODO create interactive tutorial
//TODO redesign main menu, similar to original but new color scheme

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static java.awt.Color.*;
import static picross.Main.*;


public class Graphics implements Runnable, KeyListener, WindowListener {
	public static final String VERSION = "v1.3.0.2";
	static int bSize;
	private static int numFrames = 0;
	private final int MIN_BSIZE = 14;
	//base components
	private Grid gameGrid;
	private Grid solutionGrid;
	private Box currBox;
	private int x;
	private int y;
	private int numMistakes;
	private int numFadeFrames = 0;//counts frames for fading effect
	private int fadeAlpha;
	private int cWidth;
	private int sizeX;
	private int sizeY;
	private int fps = 0;
	private int scrollIndex = 0;
	private Scanner s;
	private String currWindow;
	private Stack<String> windows;
	private String status;
	//flags
	private boolean isRunning;
	private boolean isDone;
	private boolean playable;
	private boolean faded = false;
	private boolean modifier = false;
	private boolean debugging = false;
	//graphics
	@SuppressWarnings ("CanBeFinal")
	private FancyFrame frame;
	private Image imgBuffer;
	private Color bgColor = new Color(128, 128, 255);
	static int[] clueLen;
	private Font f;
	//all buttons
	private AllButtons allButtons;
	//button categories
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
	//buttons
	private Button bPause;
	private Button bResume;
	private Button bNewPuzzle;
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
	private Button bOptions;
	private Button bControlsMenu;
	private Button bCreator;
	private Button bRandomPuzzle;
	private Button bLoadPuzzle;

	public Graphics() {
		FPSCounter.begin();
		//initialize frame & basic flags
		Dimension SIZE = new Dimension(800, 600);
		frame = new FancyFrame("Loading...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		//basic window flags
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(frame.getWidth(), frame.getHeight());
		//important flags to determine what is displayed on screen
		playable = false;
		status = "menu";
		currWindow = "menu";
		windows = new Stack<String>();
		windows.push(currWindow);
		//grab size from file
		if(prefs.get("size").equals("0,0")) {
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
		displayStatus("Setting up graphics...");
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		while(true) {
			if(isDone)
				System.exit(0);
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		writePrefs();
		frame.setVisible(false);
		isRunning = false;
		frame.dispose();
		isDone = true;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		doClickAction(bPause);
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		char keyChar = e.getKeyChar();
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_SHIFT) {
			modifier = true;
		}
		if(keyChar == 'd') {
			debugging = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if(key == KeyEvent.VK_SHIFT) {
			modifier = false;
		}
		if(key == KeyEvent.VK_D) {
			debugging = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void run() {
		while(isRunning) {
			switch(currWindow) {
				case "game":
					if(playable)
						bPause.setVisible(true);
					else {
						bBegin.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50);
						bResume.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
						bMainMenu.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
						bMainMenu2.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
						bRegenPuzzle.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
						bCreator.setPos(frame.getWidth() / 2 - 100, 400);
					}
					//get size of each box for optimal display size, takes into account clueLen and mistakes box
					bSize = getBoxSize();

					frame.setMinimumSize(new Dimension(
							245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25 >
									clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25 ?
									245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25 :
									clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25, clueLen[1] + MIN_BSIZE * gameGrid.sizeY + 50
					));
					//check for completeness
					boolean temp = true;
					for(int i = 0; i < gameGrid.sizeX; i++) {
						for(int j = 0; j < gameGrid.sizeY; j++) {
							if(gameGrid.getBox(i, j).getState() != 1 && solutionGrid.getBox(i, j).getState() == 1)
								temp = false;
						}
					}
					if(temp) {
						status = "solved";
						allButtons.setWindow("game end");
						playable = false;
						if(Main.timer != null)
							Main.timer.pause();
					}
					//maximum mistakes
					if(numMistakes == 5) {
						status = "failed";
						allButtons.setWindow("game end");
						playable = false;
						Main.timer.pause();
					}
					//maximum time
					if(Main.timer.getHours() > 9) {
						status = "failed";
						playable = false;
						Main.timer.pause();
					}
					if(status.equals("paused") && !Main.animator.isRunning()) {
						Main.animator.begin();
					} else if(!status.equals("paused") && Main.animator.isRunning()) {
						Main.animator.reset();
					}
					break;
				case "gamemode":
					gameChoiceButtons.setVisible(true);
					bRandomPuzzle.setPos(frame.getWidth() / 2 - 100, bRandomPuzzle.getY());
					bLoadPuzzle.setPos(frame.getWidth() / 2 - 100, bLoadPuzzle.getY());
					break;
				case "size picker":
					frame.setMinimumSize(new Dimension(
							getStrLen("SIZE PICKER", 50f) + bBack.getSize().width * 2 + 25,
							100 + 50 + 10 + 50 + 60 + 250
					));
					int freeSpace = frame.getHeight() - 100 - bStart.getSize().height - 150;
					bXUp.setVisible(true);
					bXUp.setPos(frame.getWidth() / 2 - 200, freeSpace / 2 - 55 + 160);
					bXDown.setVisible(true);
					bXDown.setPos(bXUp.getX(), bXUp.getY() + 110);
					bYUp.setVisible(true);
					bYUp.setPos(frame.getWidth() / 2 + 100, freeSpace / 2 - 55 + 160);
					bYDown.setVisible(true);
					bYDown.setPos(bYUp.getX(), bYUp.getY() + 110);
					bBack.setVisible(true);
					bStart.setVisible(true);
					bStart.setPos(frame.getWidth() / 2 - 50, frame.getHeight() - 100);
					if(sizeX > 25) {
						sizeX = 25;
					}
					if(sizeX < 1) {
						sizeX = 1;
					}
					if(sizeY > 25) {
						sizeY = 25;
					}
					if(sizeY < 1) {
						sizeY = 1;
					}
					break;
				case "menu":
					mainMenuButtons.setVisible(true);
					if(f != null) {
						frame.setMinimumSize(new Dimension(getStrLen("MAIN MENU", 50f) + 25, 550));
					}
					bNewPuzzle.setPos(frame.getWidth() / 2 - 125, bNewPuzzle.getY());
					bOptions.setPos(frame.getWidth() / 2 + 25, bOptions.getY());
					bQuitGame.setPos(frame.getWidth() / 2 + 25, bQuitGame.getY());
					bControlsMenu.setPos(frame.getWidth() / 2 - 125, bControlsMenu.getY());
					bCreator.setPos(frame.getWidth() / 2 - 100, bCreator.getY());
					break;
				case "options":
					bBack.setVisible(true);
					break;
				case "controls":

					break;
				case "load":
					puzzleButtons.setVisible(false);
					for(int i = scrollIndex; i < scrollIndex + (puzzleButtons.size() >= 5 ? 5 : puzzleButtons.size()); i++) {
						Button b = puzzleButtons.get(i);
						b.setX(50);
						int workingHeight = frame.getHeight() - 150;
						int topBuffer = 100;
						b.setY(workingHeight * (i - scrollIndex) / 5 + topBuffer + 25);
						b.setSizeX(frame.getWidth() - 100);
						b.setSizeY(workingHeight / 5 - 25);
						b.setVisible(true);
					}
					break;
			}
			x = frame.mouseX;
			y = frame.mouseY;
			mouseActions();
			draw();
			try {
				Thread.sleep(10);
				numFrames++;
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Renders an image of the game.
	 */
	private void draw() {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		art.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		f = art.getFont();
		switch(currWindow) {
			case "menu":
				frame.setTitle("Main Menu | Picross");
				art.setColor(bgColor);
				//art.setColor(getRandomColor());
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "MAIN MENU", 100, art);
				art = setFont(20f, art);
				art.drawString(VERSION, 15, frame.getHeight() - 15);
				break;
			case "options":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "OPTIONS", 100, art);
				break;
			case "gamemode":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "CHOOSE GAMEMODE", 100, art);
				break;
			case "size picker":
				frame.setTitle("Size Picker | Picross");
				if(sizeX == 25 || (modifier && sizeX + 5 > 25)) {
					bXUp.setVisible(false);
				} else if(sizeX == 1 || (modifier && sizeX - 5 < 1)) {
					bXDown.setVisible(false);
				} else {
					bXUp.setVisible(true);
					bXDown.setVisible(true);
				}
				if(sizeY == 25 || (modifier && sizeY + 5 > 25)) {
					bYUp.setVisible(false);
				} else if(sizeY == 1 || (modifier && sizeY - 5 < 1)) {
					bYDown.setVisible(false);
				} else {
					bYUp.setVisible(true);
					bYDown.setVisible(true);
				}

				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "SIZE PICKER", 100, art);
				drawCenteredText(f, "X", bXUp.getX() + bXUp.getSize().width / 2, bXUp.getY() - 10, art);
				drawCenteredText(f, "Y", bYUp.getX() + bYUp.getSize().width / 2, bYUp.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeX), bXUp.getX() + bXUp.getSize().width / 2, bXDown.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeY), bYUp.getX() + bYUp.getSize().width / 2, bYDown.getY() - 10, art);
				art = setFont(20f, art);
				break;
			case "game":
				frame.setTitle("" + Main.timer.toString(true) + " | Picross");
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				if(playable) {
					art.setColor(fadeOff(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				}
				art = setFont(12f, art);//12x7 pixels

				art.setColor(BLACK);
				cWidth = frame.getWidth() / 2 - (gameGrid.sizeX * bSize / 2) - clueLen[0];
				if(cWidth < 0) {
					cWidth = 0;
				}
				for(int i = 0; i < (gameGrid.sizeX); i++) {
					for(int j = 0; j < (gameGrid.sizeY); j++) {
						gameGrid.drawGrid(i, j, art, cWidth);
						art.setColor(BLACK);
						if(playable && i == (x - clueLen[0] - cWidth) / bSize && j == (y - clueLen[1]) / bSize && x > clueLen[0] + cWidth && y > clueLen[1]) {
							art.setColor(new Color(0, 0, 0, 64));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						} else if(playable && ((i == (x - clueLen[0] - cWidth) / bSize && x > clueLen[0] + cWidth) || (j == (y - clueLen[1]) / bSize && y > clueLen[1]))) {
							art.setColor(new Color(0, 0, 0, 32));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						}
						art.setColor(BLACK);
						art.drawRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
					}
				}
				if(!status.equals("paused") && !status.equals("get ready")) {
					for(int i = 0; i < gameGrid.sizeX; i++) {
						Clue cTemp = new Clue(i, 1);
						cTemp.generateClue(gameGrid);
						if(cTemp.getValues().equals(gameGrid.cluesY[i].getValues())) {
							art.setColor(new Color(0, 0, 0, 128));
							for(int box = 0; box < gameGrid.sizeY; box++) {
								Box b = gameGrid.getBox(i, box);
								if(b.getState() == 0)
									b.impossibru();
							}
						} else
							art.setColor(BLACK);
						gameGrid.drawClues(i, 1, art, cWidth);
					}
					for(int j = 0; j < gameGrid.sizeY; j++) {
						Clue cTemp = new Clue(j, 0);
						cTemp.generateClue(gameGrid);
						if(cTemp.getValues().equals(gameGrid.cluesX[j].getValues())) {
							art.setColor(new Color(0, 0, 0, 128));
							for(int box = 0; box < gameGrid.sizeX; box++) {
								Box b = gameGrid.getBox(box, j);
								if(b.getState() == 0)
									b.impossibru();
							}
						} else
							art.setColor(BLACK);
						gameGrid.drawClues(j, 0, art, cWidth);
					}
				}
				for(int i = 5; i < gameGrid.sizeX; i += 5) {
					art.drawLine(clueLen[0] + i * bSize + 1 + cWidth, clueLen[1], clueLen[0] + i * bSize + 1 + cWidth, clueLen[1] + gameGrid.sizeY * bSize);
				}
				for(int i = 5; i < gameGrid.sizeY; i += 5) {
					art.drawLine(clueLen[0] + cWidth, clueLen[1] + i * bSize + 1, clueLen[0] + gameGrid.sizeX * bSize + cWidth, clueLen[1] + i * bSize + 1);
				}
				art = setFont(20f, art);
				art.drawString("MISTAKES: ", 10, frame.getHeight() - 15);
				int xRendered = 0, mistakesTemp = numMistakes;
				art.drawRect(120, frame.getHeight() - 35, 125, 25);
				art.setColor(RED);
				while(mistakesTemp > 0 && xRendered < 5) {
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					mistakesTemp--;
					xRendered++;
				}
				while(xRendered < 5) {
					art.setColor(new Color(0, 0, 0, 64));
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					xRendered++;
				}
				if(!playable) {
					bPause.setVisible(false);
					if(bBegin.isVisible()) {
						faded = true;
					}
					art.setColor(fadeOn(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
					art.setColor(WHITE);
					art.fillRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					art.setColor(BLACK);
					art.drawRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					String showText = "";

					art = setFont(30f, art);
					switch(status) {
						case "solved":
							art.setColor(GREEN);
							showText = "SOLVED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "failed":
							art.setColor(RED);
							showText = "FAILED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "paused":
							if(Main.animator.getMS() % 1000 <= 500) {
								drawCenteredText(f, "PAUSED", frame.getHeight() / 2 - 10, art);
							}
							bResume.draw(x, y, art);
							bMainMenu.draw(x, y, art);
							break;
					}
					if(status.equals("get ready"))
						bBegin.draw(x, y, art);
					drawCenteredText(f, showText, frame.getHeight() / 2 - 10, art);
					art.setColor(BLACK);
					//if(!status.equals("get ready") && !status.equals("paused"))
					//art.drawString("TIME:" + Main.timer.toString(), frame.getWidth() / 2 - 45, frame.getHeight() / 2 - 12);
				}
				//render mistakes/timer
				art = setFont(20f, art);
				art.setColor(BLACK);
				drawRightText(f, "TIME: " + Main.timer.toString(false), frame.getHeight() - 15, art);
				break;
			case "controls":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(black);
				art = setFont(50f, art);
				drawCenteredText(f, "CONTROLS", 100, art);
				art.drawRect(100, 150, frame.getWidth() - 200, frame.getHeight() - 200);
				List<String> text = new ArrayList<>();
				//CONTROLS TEXT
				text.add("Esc -> Pause");//TODO implement
				text.add("Q -> Quit current game");//TODO implement
				text.add("R -> Reset puzzle");//TODO implement
				text.add("M (hold) -> Show currently playing song");
				text.add("N -> Skip current song");
				text.add("Scroll on size picker -> quickly change size");
				text.add("Shift (hold) -> size picker changes by 5s instead of 1s");
				//END CONTROLS TEXT
				frame.setMinimumSize(new Dimension(130 + getMaxStrLen(text, 25f) + 130, 210 + (35 * text.size()) + 100));
				art = setFont(25f, art);
				for(int i = 0; i < text.size(); i++) {
					art.drawString(text.get(i), 130, 180 + i * 35);
				}
				break;
			case "load":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "LOAD A PUZZLE", 100, art);
				loadMenuButtons.drawAll(x, y, art);
				puzzleButtons.drawAll(x, y, art);
				break;
		}
		allButtons.drawButtons(x, y, art);
		if(Main.FPSCounter.getMS() > 1000) {
			Main.FPSCounter.begin();
			fps = numFrames;
			numFrames = 0;
		}
		art.setColor(black);
		art = setFont(12f, art);
		if(debugging) {
			art.drawString("" + fps + " FPS", 20, 50);
		}
		art = (Graphics2D) frame.getGraphics();
		if(art != null) {
			imgBuffer = Resizer.PROGRESSIVE_BILINEAR.resize((BufferedImage) imgBuffer, frame.getWidth(), frame.getHeight());
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}

	/**
	 * Creates a Grid with random states of size sizeX, sizeY.
	 */
	private void getSolution() {
		//x = Integer.parseInt(s.nextLine());
		//y = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for(int i = 0; i < sizeX; i++) {
			for(int j = 0; j < sizeY; j++) {
				//int b = s.nextInt();
				Random random = new Random();
				int b = random.nextInt(2);
				if(b == 1) {
					solutionGrid.getBox(i, j).setState(1);
				}
			}
		}
	}

	/**
	 * @return Returns the side length of a box in pixels based on graphics elements in game and frame size
	 */
	private int getBoxSize() {
		int temp = (frame.getWidth() - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (gameGrid.sizeX + 1);
		if(temp * gameGrid.sizeY + clueLen[1] + 50 > frame.getHeight()) {
			return (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY > MIN_BSIZE ? (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY : MIN_BSIZE;
		} else {
			return temp > MIN_BSIZE ? temp : MIN_BSIZE;
		}
	}

	/**
	 * Writes generated clues of a random puzzle to a file, to be read by the solver program.
	 */
	private void writeClues() {
		try {
			FileWriter writer = new FileWriter("clues.nin");
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for(Clue c : gameGrid.cluesX) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			for(Clue c : gameGrid.cluesY) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs any actions regarding mouse clicks that are not handled by the Button class. Includes gameplay and scrolling on the size picker.
	 */
	private void mouseActions() {
		if(frame.hasClicked()) {
			frame.setHasClicked(false);
		}
		switch(currWindow) {
			case "game":
				//bound checking to prevent instant toggling of a flag
				if(currBox != null && (x - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1] && currBox != gameGrid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize)) {
					currBox.setCanModify(true);
				}
				//get box only if mouse is within game grid, otherwise it is null
				if((x - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1]) {
					currBox = gameGrid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize);
				} else {
					currBox = null;
				}
				if(currBox != null) {
					if(frame.isClicking()) {
						//only disables boxes as the player attempts to modify them
						if(!playable)
							currBox.setCanModify(false);
						//left click = reveal
						if(frame.getMouseButton() == 3) {
							currBox.impossibru();
							currBox.setCanModify(false);
						} else if(frame.getMouseButton() == 1) {
							//click buttons
							//if the box is not part of the solution, you made a mistake
							if(!currBox.green(solutionGrid)) {
								numMistakes++;
								currBox.setCanModify(false);
							}
							currBox.setCanModify(false);
						}
						//right click = flag, is not checked with the solution to prevent cheating

					} else {
						currBox.setCanModify(true);
					}
				}
				break;
			case "menu":
				//no special actions for menu
				break;
			case "size picker":
				if(frame.scrollAmt != 0) {
					if(isInBounds(bXUp.getX(), bXUp.getY() + bXUp.getSize().height, bXUp.getX() + bXUp.getSize().width, bXUp.getY() + bXUp.getSize().height + 60)) {
						if(modifier) {
							sizeX -= sizeX - (5 * frame.scrollAmt) > 0 && sizeX - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeX -= sizeX - frame.scrollAmt > 0 && sizeX - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					} else if(isInBounds(bYUp.getX(), bYUp.getY() + bYUp.getSize().height, bYUp.getX() + bYUp.getSize().width, bYUp.getY() + bYUp.getSize().height + 60)) {
						if(modifier) {
							sizeY -= sizeY - (5 * frame.scrollAmt) > 0 && sizeY - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeY -= sizeY - frame.scrollAmt > 0 && sizeY - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					}
					frame.scrollAmt = 0;
				}
				break;
			case "load":
				if(frame.scrollAmt != 0) {
					scrollIndex += (scrollIndex + frame.scrollAmt >= 0 && scrollIndex + frame.scrollAmt <= puzzleButtons.size() - 5 ? frame.scrollAmt : 0);

					frame.scrollAmt = 0;
				}
				scrollIndex = scrollIndex;
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
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			} while(output.size() < 5);
			int solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
			numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
			System.out.println(output.get(solutionsLine));
			//String difficulty = "";
			//int diffLine = Strings.findLineWith(output, "Decisions : ", true);
			//difficulty = output.get(diffLine).substring(12, output.get(diffLine).length());
			//Integer.parseInt(difficulty);
		} while(numSolutions > 1);
		System.out.println("Generated puzzle in " + numTries + " " + (numTries == 1 ? "try." : "tries."));
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for(int i = 0; i < gameGrid.sizeY; i++) {
			if(gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if(clueLen[0] < 100)
			clueLen[0] = 100;
		for(int i = 0; i < gameGrid.sizeX; i++) {
			if(gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if(clueLen[1] < 130) {
			clueLen[1] = 130;
		}
		try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("clues.nin"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void loadPuzzle(String name) {
		frame.setTitle("LOADING...");
		displayStatus("Loading custom puzzle...");
		windows.push(currWindow);
		currWindow = "game";
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
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(s.hasNext()) {
			String size = s.nextLine();
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(' ')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(' ') + 1, size.length()));
			gameGrid = new Grid(sizeX, sizeY);
		}
		runSolver("." + slashCharacter + "saves" + slashCharacter + name + ".nin");
		List<String> prevOutput = new ArrayList<>();
		do {
			prevOutput = output;
			output = LogStreamReader.output;
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		} while(!output.equals(prevOutput));
		List<String> puzzle = new ArrayList<>();
		int startIndex = 0;
		for(int i = 0; i < output.size(); i++) {
			puzzle.add(output.get(i));
			if(output.get(i).contains("hash misses")) {
				startIndex = i + 2;
			}
		}
		puzzle.remove(startIndex + sizeY);
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for(int i = 0; i < sizeY; i++) {
			System.out.println(puzzle.get(i + startIndex));
			for(int j = 0; j < sizeX; j++) {
				char currCheck = puzzle.get(i + startIndex).charAt(j);
				solutionGrid.getBox(j, i).setState(currCheck == '#' ? 1 : 0);
			}
		}
		gameGrid.generateClues(solutionGrid);
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for(int i = 0; i < gameGrid.sizeY; i++) {
			if(gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if(clueLen[0] < 100)
			clueLen[0] = 100;
		for(int i = 0; i < gameGrid.sizeX; i++) {
			if(gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if(clueLen[1] < 130) {
			clueLen[1] = 130;
		}
	}

	/**
	 * Prints a string in the center of the frame.
	 *
	 * @param f   font, analyzed to center text exactly
	 * @param s   string to print
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	private void drawCenteredText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() / 2 - len / 2, y);
	}

	/**
	 * Prints a string centered at (x, y).
	 *
	 * @param f   font, analyzed to center text exactly
	 * @param s   string to print
	 * @param x   x-value of the center in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	void drawCenteredText(Font f, String s, int x, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, x - len / 2, y);
	}

	/**
	 * Prints a string aligned to the right of the frame.
	 *
	 * @param f   font, analyzed to find leftmost pixel of printed text
	 * @param s   string to print
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	private void drawRightText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() - len - 10, y);
	}

	/**
	 * Prints a string right-aligned to the point (x, y).
	 *
	 * @param f   font, analyzed to find leftmost pixel of printed text
	 * @param s   string to print
	 * @param x   x-value of the string end in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	void drawRightText(Font f, String s, int x, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, x - len - 10, y);
	}

	/**
	 * Performs a predetermined action based on the button passed.
	 *
	 * @param b button to be compared with known buttons
	 */
	void doClickAction(Button b) {
		if(b != bXUp && b != bXDown && b != bYUp && b != bYDown && b != bBegin && b != bPause)
		displayStatusNoBG("Loading...");
		if(b == bNewPuzzle) {
			windows.push(currWindow);
			currWindow = "gamemode";
			allButtons.setWindow(currWindow);
			//get size from settings file

		} else if(b == bRandomPuzzle) {
			windows.push(currWindow);
			currWindow = "size picker";
			allButtons.setWindow(currWindow);
			String size = prefs.get("size");
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(',')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(',') + 1));
			if(sizeX == 0)
				sizeX = 10;
			if(sizeY == 0)
				sizeY = 10;
		} else if(b == bOptions) {
			windows.push(currWindow);
			currWindow = "options";
			allButtons.setWindow(currWindow);
		} else if(b == bResume) {
			status = "";
			allButtons.setWindow("game");
			bResume.setVisible(false);
			bMainMenu2.setVisible(false);
			bPause.setVisible(true);
			Main.timer.resume();
			playable = true;
			faded = false;
		} else if(b == bPause) {
			if(status.equals("")) {
				allButtons.setWindow("pause");
				status = "paused";
				bPause.setVisible(false);
				bResume.setVisible(true);
				bMainMenu2.setVisible(true);
				Main.timer.pause();
				playable = false;
				faded = false;
			}
		} else if(b == bXUp) {
			if(modifier) {
				sizeX += 5;
			} else {
				sizeX++;
			}
		} else if(b == bXDown) {
			if(modifier) {
				sizeX -= 5;
			} else {
				sizeX--;
			}
		} else if(b == bYUp) {
			if(modifier) {
				sizeY += 5;
			} else {
				sizeY++;
			}
		} else if(b == bYDown) {
			if(modifier) {
				sizeY -= 5;
			} else {
				sizeY--;
			}
		} else if(b == bBack) {
			currWindow = windows.pop();
			allButtons.setWindow(currWindow);
		} else if(b == bStart || b == bRegenPuzzle) {
			frame.setTitle("GENERATING...");
			displayStatus("Generating random puzzle...");
			b.setVisible(false);
			windows.push(currWindow);
			currWindow = "game";
			allButtons.setWindow(currWindow);
			status = "get ready";
			bBegin.setVisible(true);
			bPause.setVisible(false);
			numMistakes = 0;
			bRegenPuzzle.setVisible(false);
			bMainMenu.setVisible(false);
			playable = false;
			generatePuzzle();
			Main.timer.reset();
		} else if(b == bMainMenu || b == bMainMenu2) {
			windows = new Stack<>();
			frame.setTitle("Main Menu | Picross");
			currWindow = "menu";
			windows.push(currWindow);
			allButtons.setWindow(currWindow);
			status = "menu";
			numMistakes = 0;
			playable = false;
		} else if(b == bQuitGame) {
			frame.setTitle("Quitting...");
			writePrefs();
			frame.setVisible(false);
			isRunning = false;
			frame.dispose();
			isDone = true;
			System.exit(0);
		} else if(b == bBegin) {
			b.setVisible(false);
			status = "";
			Main.timer.begin();
			playable = true;
			faded = false;
		} else if(b == bControlsMenu) {
			windows.push(currWindow);
			currWindow = "controls";
			allButtons.setWindow(currWindow);
		} else if(b == bCreator) {
			runCreator();
		} else if(b == bLoadPuzzle) {
			windows.push(currWindow);
			currWindow = "load";
			allButtons.setWindow(currWindow);
			loadMenuButtons.setVisible(true);
			//get all puzzles
			List<String> puzzleNames = getPuzzleNames();
			scrollIndex = 0;
			Button[] pButtons = new Button[getNumPuzzles()];
			for(int i = 0; i < getNumPuzzles(); i++) {
				pButtons[i] = new Button();
				pButtons[i].setText(puzzleNames.get(i).substring(0, puzzleNames.get(i).length() - 4));
			}
			puzzleButtons.addButtons(pButtons);
			puzzleButtons.sort();
			puzzleButtons.setVisible(true);
		} else {
			for(int i = 0; i < puzzleButtons.size(); i++) {
				if(b == puzzleButtons.get(i)) {
					loadPuzzle(b.getText());
				}
			}
		}
	}

	void doSlideAction(Slider s) {

	}

	/**
	 * Returns a color that slowly darkens to amt.
	 *
	 * @param amt      Amount to darken, from 0-255
	 * @param duration Time in frames to darken
	 * @return Color to cover frame with for a fading effect
	 */
	@SuppressWarnings ("SameParameterValue")
	private Color fadeOn(int amt, int duration) {
		duration /= 10;
		if(numFadeFrames == duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = numFadeFrames * amt / duration;
		Color out = faded ? new Color(0, 0, 0, amt) : new Color(0, 0, 0, fadeAlpha);
		if(!faded) {
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
	@SuppressWarnings ("SameParameterValue")
	private Color fadeOff(int amtInit, int duration) {
		duration /= 10;
		if(numFadeFrames > duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = amtInit - (numFadeFrames * amtInit / duration);
		Color out = faded ? new Color(0, 0, 0, 0) : new Color(0, 0, 0, fadeAlpha);
		if(!faded) {
			numFadeFrames++;
		}
		return out;
	}

	/**
	 * @param x1 x-coordinate of left bound of rectangle
	 * @param y1 y-coordinate of left bound of rectangle
	 * @param x2 x-coordinate of right bound of rectangle
	 * @param y2 y-coordinate of right bound of rectangle
	 * @return Returns whether the mouse's current positions falls within the defined bounds.
	 */
	//@Contract (pure = true)
	private boolean isInBounds(int x1, int y1, int x2, int y2) {
		return (x > x1) && (x < x2) && (y > y1) && (y < y2);
	}

	private int getStrLen(String s, float fontHeight) {
		FontMetrics fm = frame.getGraphics().getFontMetrics(f.deriveFont(fontHeight));
		return fm.stringWidth(s);
	}

	private int getMaxStrLen(List<String> strings, float fontHeight) {
		int max = 0;
		for(String s : strings) {
			if(getStrLen(s, fontHeight) > max) {
				max = getStrLen(s, fontHeight);
			}
		}
		return max;
	}

	FancyFrame getFrame() {
		return frame;
	}


	private Graphics2D setFont(float size, Graphics2D art) {
		art.setFont(f.deriveFont(size));
		f = f.deriveFont(size);
		return art;
	}

	private void initButtons() {
		allButtons = new AllButtons();

		mainMenuButtons = new ButtonList("menu");
		bNewPuzzle = new Button(frame.getWidth() / 2 - 125, 125, 100, 100, "Start Game", GREEN, 20);
		bNewPuzzle.setVisible(true);
		bOptions = new Button(frame.getWidth() / 2 + 25, 125, 100, 100, "Options", YELLOW, 20);
		bOptions.setVisible(true);
		bQuitGame = new Button(frame.getWidth() / 2 + 25, 275, 100, 100, "Quit Game", RED, 20);
		bQuitGame.setVisible(true);
		bControlsMenu = new Button(frame.getWidth() / 2 - 125, 275, 100, 100, "Controls", BLUE, 20);
		bControlsMenu.setVisible(true);
		bCreator = new Button(frame.getWidth() / 2 - 100, 400, 200, 100, "Creator", YELLOW, 20);
		bCreator.setVisible(true);
		mainMenuButtons.addButtons(new Button[] {bNewPuzzle, bQuitGame, bOptions, bControlsMenu, bCreator});

		gameChoiceButtons = new ButtonList("gamemode");
		bBack = new Button(10, 55, 50, 50, "<", RED, 30);
		bRandomPuzzle = new Button(frame.getWidth() / 2 - 100, 150, 200, 100, "Random Puzzle", GREEN, 20);
		bLoadPuzzle = new Button(frame.getWidth() / 2 - 100, 275, 200, 100, "Load Puzzle", YELLOW, 20);
		gameChoiceButtons.addButtons(new Button[] {bRandomPuzzle, bLoadPuzzle, bBack});

		loadMenuButtons = new ButtonList("load");
		loadMenuButtons.addButtons(new Button[] {bBack});

		puzzleButtons = new ButtonList("puzzles");

		sizePickerButtons = new ButtonList("size picker");
		bXUp = new Button(300, 400, 100, 50, "Λ", 30);
		bXDown = new Button(300, 510, 100, 50, "V", 30);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30);
		bYDown = new Button(600, 510, 100, 50, "V", 30);
		bStart = new Button(frame.getWidth() / 2 - 50, frame.getHeight() - 100, 100, 75, "GENERATE", GREEN, 30);
		sizePickerButtons.addButtons(new Button[] {bXUp, bXDown, bYUp, bYDown, bBack, bStart});

		optionsMenuButtons = new ButtonList("options");
		optionsMenuButtons.addButtons(new Button[] {bBack});

		gameButtons = new ButtonList("game");
		bPause = new Button(20, 50, 60, 60, "Pause", YELLOW, 17);
		gameButtons.addButtons(new Button[] {bPause});

		pauseMenuButtons = new ButtonList("pause");
		bResume = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Resume", GREEN, 17);
		bMainMenu = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", bgColor, 17);
		bMainMenu2 = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", bgColor, 17);
		bRegenPuzzle = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "New Puzzle", GREEN, 17);
		bBegin = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100, "BEGIN", GREEN, 20);
		pauseMenuButtons.addButtons(new Button[] {bResume/*, bMainMenu*/, bMainMenu2/*, bRegenPuzzle*//*, bBegin*/});

		gameEndButtons = new ButtonList("game end");
		gameEndButtons.addButtons(new Button[] {bMainMenu, bRegenPuzzle});

		controlsMenuButtons = new ButtonList("controls");
		controlsMenuButtons.addButtons(new Button[] {bBack});

		allButtons.addButtonLists(new ButtonList[] {mainMenuButtons, gameChoiceButtons, loadMenuButtons, sizePickerButtons, optionsMenuButtons, gameButtons, gameEndButtons, pauseMenuButtons, controlsMenuButtons});
		allButtons.setWindow("menu");
	}

	private void writePrefs() {
		prefs.put("size", "" + sizeX + ',' + sizeY);
		try {
			prefs.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void displayStatus(String message) {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHints(rh);
		f = art.getFont();
		art.setColor(bgColor);
		art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		art.setColor(black);
		art = setFont(50f, art);
		drawCenteredText(f, message, frame.getHeight() / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if(art != null) {
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
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
		drawCenteredText(f, message, frame.getHeight() / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if(art != null) {
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}
}
