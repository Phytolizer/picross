package picross;

import common.BetterFrame;

import java.awt.*;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

/**
 * Created on 4/7/2016 at 11:47 PM.
 */
@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
class Slider {
	private final Color TRANSPARENT = new Color(0, 0, 0, 0);
	private final Color HOVERING = new Color(0, 0, 0, 32);
	private final Color CLICKING = new Color(0, 0, 0, 64);
	private int x, y, sizeX, knobRadius, thickness;
	private double position; //Between 0 and 1
	private Color coverColor = TRANSPARENT;
	private boolean isVisible, hovering, clicking;

	@SuppressWarnings("SameParameterValue")
	public Slider(int x1, int y1, int size_x) {
		x = x1;
		y = y1;
		sizeX = size_x;
		position = 0.5;
		knobRadius = 10;
		thickness = 7;
	}

	public Slider(int x1, int y1, int size_x, double pos) {
		x = x1;
		y = y1;
		sizeX = size_x;
		assert 0 <= pos && pos <= 1;
		position = pos;
		knobRadius = 10;
		thickness = 7;
	}

	public void draw(int mouseX, int mouseY, Graphics2D art) {
		if (isVisible) {
			int knobPos = x + (int) ((double) sizeX * position);
			BetterFrame frame = Main.mainWindow.getFrame();
			if (mouseX > x && mouseX < x + sizeX && mouseY > y - knobRadius && mouseY < y + knobRadius) {
				hover();
			} else if (hovering && ! clicking) {
				unHover();
			}
			if (hovering && frame.clicking()) {
				click();
			} else if (clicking) {
				unClick();
			}
			moveKnob(mouseX);
			art.setColor(WHITE);
			art.fillOval(x, y - thickness, thickness * 2, thickness * 2);
			art.fillOval(x + sizeX - thickness * 2, y - thickness, thickness * 2, thickness * 2);
			art.setColor(BLACK);
			art.drawOval(x, y - thickness, thickness * 2, thickness * 2);
			art.drawOval(x + sizeX - thickness * 2, y - thickness, thickness * 2, thickness * 2);
			art.setColor(Color.WHITE);
			art.fillRect(x + thickness, y - thickness, sizeX - thickness * 2, thickness * 2);
			art.setColor(BLACK);
			art.drawLine(x + thickness, y - thickness, x + sizeX - thickness, y - thickness);
			art.drawLine(x + thickness, y + thickness, x + sizeX - thickness, y + thickness);
			art.setColor(WHITE);
			art.fillOval(knobPos - knobRadius, y - knobRadius, knobRadius * 2, knobRadius * 2);
			art.setColor(BLACK);
			art.drawOval(knobPos - knobRadius, y - knobRadius, knobRadius * 2, knobRadius * 2);
			art.setColor(coverColor);
			art.fillOval(knobPos - knobRadius, y - knobRadius, knobRadius * 2, knobRadius * 2);
		}
	}

	private void hover() {
		if (isVisible) {
			hovering = true;
			coverColor = HOVERING;
		}
	}

	private void click() {
		if (isVisible) {
			clicking = true;
			coverColor = CLICKING;
		}
	}

	private void unHover() {
		if (isVisible) {
			hovering = false;
			coverColor = TRANSPARENT;
		}
	}

	private void unClick() {
		if (isVisible) {
			clicking = false;
			if (hovering) {
				coverColor = HOVERING;
			} else {
				coverColor = TRANSPARENT;
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	public void setVisible(boolean v) {
		isVisible = v;
	}

	public void setScreenPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public double getPos() {
		return position;
	}

	public Dimension getPosition() {
		return new Dimension(x, y);
	}

	private void moveKnob(int mouseX) {
		if (clicking) {
			position = ((double) mouseX - (double) x) / (double) sizeX;
			if (position < 0.0)
				position = 0.0;
			if (position > 1.0)
				position = 1.0;
			Main.mainWindow.doSlideAction(this);
		}
	}
}
