package picross;

import common.BetterFrame;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static java.awt.Color.*;

/**
 * Created on 4/12/2016 at 9:02 PM.
 */
@SuppressWarnings({"SameParameterValue", "CanBeFinal", "WeakerAccess"})
class CheckBox {
	private final Color TRANSPARENT = new Color(0, 0, 0, 0);
	private final Color HOVERING = new Color(0, 0, 0, 32);
	private final Color CLICKING = new Color(0, 0, 0, 64);
	private Color coverColor = TRANSPARENT;
	private boolean checked, hovering, clicking;
	private int posX, posY, size;

	public CheckBox(int x, int y, int size) {
		checked = false;
		posX = x;
		posY = y;
		this.size = size;
	}

	public CheckBox(int x, int y, int size, boolean isChecked) {
		posX = x;
		posY = y;
		this.size = size;
		checked = isChecked;
	}

	public void draw(int mouseX, int mouseY, Graphics2D graphics2D) {
		BetterFrame frame = Main.mainWindow.getFrame();
		graphics2D.setColor(white);
		graphics2D.fillRect(posX, posY, size, size);
		graphics2D.setColor(black);
		graphics2D.drawRect(posX, posY, size, size);
		graphics2D.setColor(coverColor);
		graphics2D.fillRect(posX, posY, size, size);
		if (mouseX > posX && mouseX < posX + size && mouseY > posY && mouseY < posY + size) {
			hover();
		} else
			unHover();
		if (frame.clicking() && hovering) {
			click();
		} else if (clicking) {
			unClick();
		}
		if (checked) {
			BufferedImage checkMark = null;
			try {
				URL url = this.getClass().getClassLoader().getResource("resources/check.png");
				if (url != null)
					checkMark = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (checkMark != null)
				graphics2D.drawImage(checkMark, posX, posY, size, size, (img, infoflags, x, y, width, height) -> false);
			else {
				int border = size / 6;
				graphics2D.setColor(BLACK);
				graphics2D.fillRect(posX + border, posY + border, size - 2 * border + 1, size - 2 * border + 1);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public void hover() {
		hovering = true;
		coverColor = HOVERING;
	}

	@SuppressWarnings("WeakerAccess")
	public void unHover() {
		hovering = false;
		clicking = false;
		coverColor = TRANSPARENT;
	}

	@SuppressWarnings("WeakerAccess")
	public void click() {
		clicking = true;
		coverColor = CLICKING;
	}

	@SuppressWarnings("WeakerAccess")
	public void unClick() {
		clicking = false;
		checked = ! checked;
		coverColor = hovering ? HOVERING : TRANSPARENT;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setPos(int x, int y) {
		posX = x;
		posY = y;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
