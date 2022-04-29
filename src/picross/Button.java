package picross;

import common.Graphics;

import java.awt.*;

import static picross.Main.mainWindow;

@SuppressWarnings({"CanBeFinal", "WeakerAccess"})
class Button {
	private int x1, y1, sizeX, sizeY;
	private int maxFontSize;
	private boolean clicking;
	private boolean isVisible;
	/**
	 * Prevents infinite clicking by forcing one click per mouse click or at least while the mouse stays in bounds.
	 */
	private boolean canClick;
	private boolean isToggled;
	private Color baseColor, coverColor, borderColor = Color.BLACK;
	private String text;
	private Font font;
	private FontMetrics fontInfo;
	private Graphics parentGraphics;

	public Button(Graphics g) {
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		maxFontSize = 50;
		parentGraphics = g;
	}

	public Button(int x, int y, int size_x, int size_y, String text, Graphics g) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		parentGraphics = g;
	}

	@SuppressWarnings("SameParameterValue")
	public Button(int x, int y, int size_x, int size_y, String text, int maxFontSize, Graphics g) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		this.maxFontSize = maxFontSize;
		parentGraphics = g;
	}

	public Button(int x, int y, int size_x, int size_y, String text, Color bColor, Graphics g) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = bColor;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		parentGraphics = g;
	}

	/**
	 * Creates a new Button.
	 *
	 * @param x           x-coordinate of top-left corner, in pixels.
	 * @param y           y-coordinate of top-left corner, in pixels.
	 * @param size_x      Size of the button in the x direction.
	 * @param size_y      Size of the button in the y direction.
	 * @param text        Text to display on the button.
	 * @param bColor      Color of the button (optional, default is white)
	 * @param maxFontSize Maximum size of text (optional but can improve readability of the button)
	 */
	public Button(int x, int y, int size_x, int size_y, String text, Color bColor, int maxFontSize, Graphics g) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = bColor;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		this.maxFontSize = maxFontSize;
		parentGraphics = g;
	}

	private void hover() {
		if (isVisible) {
			coverColor = new Color(0, 0, 0, 32);
		}
	}

	private void click() {
		if (isVisible && canClick) {
			clicking = true;
			coverColor = new Color(0, 0, 0, 128);
		}
	}

	private void unClick() {
		clicking = false;
		canClick = false;
		if (isVisible) {
			coverColor = new Color(0, 0, 0, 32);
		}
	}

	private void unHover() {
		if (isVisible) {
			coverColor = new Color(0, 0, 0, 0);
		}
	}

	public void setVisible(boolean b) {
		isVisible = b;
	}

	/**
	 * Renders the button onto graphics.
	 *
	 * @param graphics2D GameWindow to draw on
	 */
	public void draw(Graphics2D graphics2D) {
		int x = parentGraphics.getFrame().mouseX;
		int y = parentGraphics.getFrame().mouseY;
		if (clicking && !mainWindow.getFrame().clicking()) {
			if (isInBounds(x, y)) {
				try {
					mainWindow.doClickAction(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				unClick();
			} else {
				unClick();
			}
		}
		if (isInBounds(x, y))
			hover();
		else
			unHover();
		if (mainWindow.getFrame().clicking()) {
			if (mainWindow.getFrame().getMouseButton() == 1) {
				if (isInBounds(x, y)) {
					click();
				}
			}
		} else {
			canClick = true;
		}
		font = graphics2D.getFont().deriveFont(sizeY);
		fontInfo = graphics2D.getFontMetrics(font);
		float textSize = getTextSize(graphics2D);
		font = graphics2D.getFont().deriveFont(textSize);
		fontInfo = graphics2D.getFontMetrics(font);
		if (isVisible) {
			graphics2D.setFont(font);
			graphics2D.setColor(baseColor);
			graphics2D.fillRect(x1, y1, sizeX, sizeY);
			graphics2D.setColor(coverColor);
			graphics2D.fillRect(x1, y1, sizeX, sizeY);
			graphics2D.setColor(borderColor);
			graphics2D.drawRect(x1, y1, sizeX, sizeY);
			graphics2D.setColor(Color.black);
			graphics2D.drawString(text, x1 + (sizeX / 2 - fontInfo.stringWidth(text) / 2), y1 + (sizeY / 2 + textSize / 3));
			graphics2D.setFont(graphics2D.getFont().deriveFont(12f));
		}
	}

	/**
	 * @param art GameWindow from which to derive a font
	 * @return Returns the optimal size for button text, from 0 to maxFontSize.
	 */
	private float getTextSize(Graphics2D art) {
		float width = fontInfo.stringWidth(text) * text.length(), height = sizeY;
		while (width > sizeX) {
			height -= 0.1;
			font = art.getFont().deriveFont(height);
			fontInfo = art.getFontMetrics(font);
			width = fontInfo.stringWidth(text);
		}
		if (height > maxFontSize)
			height = maxFontSize;
		return height;
	}

	/**
	 * @param x x-coordinate to test
	 * @param y y-coordinate to test
	 * @return Returns whether the given coordinates are within the Button's bounds.
	 */
	private boolean isInBounds(int x, int y) {
		return x > x1 && y > y1 && x < (x1 + sizeX) && y < (y1 + sizeY);
	}

	public boolean isClicking() {
		return clicking;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setText(String s) {
		text = s;
	}

	public String getText() {
		return text;
	}

	public void setBorderColor(Color c) {
		borderColor = c;
	}

	public void setPos(int x, int y) {
		x1 = x;
		y1 = y;
	}

	public void setX(int x) {
		x1 = x;
	}

	public void setY(int y) {
		y1 = y;
	}

	public void setSize(int x, int y) {
		sizeX = x;
		sizeY = y;
	}

	public void setSizeX(int x) {
		sizeX = x;
	}

	public void setSizeY(int y) {
		sizeY = y;
	}

	public int getX() {
		return x1;
	}

	public int getY() {
		return y1;
	}

	public Dimension getSize() {
		return new Dimension(sizeX, sizeY);
	}

	public void setToggled(boolean toggled) {
		isToggled = toggled;
	}

	public boolean isToggled() {
		return isToggled;
	}

	public void toggle() {
		isToggled = ! isToggled;
	}

	public Color getBaseColor() {
		return baseColor;
	}

	public void setBaseColor(Color baseColor) {
		this.baseColor = baseColor;
	}
}
