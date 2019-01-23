package picross;

import common.KeyInterface;

import java.awt.event.KeyEvent;

import static picross.Main.mainWindow;

/**
 * Created by kyle on 3/3/17.
 */
public class PicrossKeyHandler implements KeyInterface {
	private String keyAssigning;
	private final int keyCancelAssignment = KeyEvent.VK_BACK_SPACE;

	public PicrossKeyHandler() {

	}

	@Override
	public void pressKey(KeyEvent e) {
		char keyChar = e.getKeyChar();
		int keyCode = e.getKeyCode();
		if (keyAssigning == null) {
			if (keyCode == KeyEvent.VK_SHIFT) {
				mainWindow.modifier = true;
			} else if (keyCode == mainWindow.keyPauseGame) {
				try {
					mainWindow.doClickAction(mainWindow.bPause);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else if (keyCode == mainWindow.keyUp || keyCode == mainWindow.keyDown || keyCode == mainWindow.keyLeft || keyCode == mainWindow.keyRight) {
				//enter keyboard mode
				if (mainWindow.controlMode == ControlMode.MOUSE) {
					mainWindow.savedMouseX = mainWindow.mouseX;
					mainWindow.savedMouseY = mainWindow.mouseY;
					mainWindow.kbX = mainWindow.currBox == null ? 0 : mainWindow.currBox.getPos()[0];
					mainWindow.kbY = mainWindow.currBox == null ? 0 : mainWindow.currBox.getPos()[1];
					mainWindow.controlMode = ControlMode.KEYBOARD;
					System.out.println("Entering keyboard control mode");
				} else {
					if (keyCode == mainWindow.keyUp && mainWindow.kbY > 0) {
						mainWindow.kbY--;
					}
					if (keyCode == mainWindow.keyDown && mainWindow.kbY < mainWindow.sizeY - 1) {
						mainWindow.kbY++;
					}
					if (keyCode == mainWindow.keyLeft && mainWindow.kbX > 0) {
						mainWindow.kbX--;
					}
					if (keyCode == mainWindow.keyRight && mainWindow.kbX < mainWindow.sizeX - 1) {
						mainWindow.kbX++;
					}
//				System.out.println("Keyboard X pos: " + kbX + ", Y pos: " + kbY);
				}
			} else if (mainWindow.controlMode == ControlMode.KEYBOARD && (keyCode == mainWindow.keyResolve1 || keyCode == mainWindow.keyResolve2)) {
				mainWindow.pushingSolveKey = true;
			} else if (keyCode == mainWindow.keyGamba) {
				try {
					mainWindow.doClickAction(mainWindow.bGamba);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} else if (keyCode != keyCancelAssignment) {
			switch (keyAssigning) {
				case "pauseGame":
					mainWindow.keyPauseGame = keyCode;
					break;
				case "up":
					mainWindow.keyUp = keyCode;
					break;
				case "left":
					mainWindow.keyLeft = keyCode;
					break;
				case "down":
					mainWindow.keyDown = keyCode;
					break;
				case "right":
					mainWindow.keyRight = keyCode;
					break;
				case "resolve1":
					mainWindow.keyResolve1 = keyCode;
					break;
				case "resolve2":
					mainWindow.keyResolve2 = keyCode;
					break;
				case "gamba":
					mainWindow.keyGamba = keyCode;
					break;
			}
			mainWindow.updateButtons("controls");
			keyAssigning = null;
		} else {
			keyAssigning = null;
			mainWindow.updateButtons("controls");
		}
		if (keyChar == 'd') {
			mainWindow.debugging = true;
		}
		if (keyCode == KeyEvent.VK_ENTER) {
			mainWindow.submitScore();
		}
	}

	@Override
	public void releaseKey(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_SHIFT) {
			mainWindow.modifier = false;
		} else if (key == KeyEvent.VK_D) {
			mainWindow.debugging = false;
		} else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
			mainWindow.pushingSolveKey = false;
		}
	}

	public void setKeyAssigning(String keyAssigning) {
		this.keyAssigning = keyAssigning;
	}

	public String getKeyAssigning() {
		return keyAssigning;
	}
}
