package picross;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 4/7/2016 at 11:17 PM.
 */
class ButtonList {
	@SuppressWarnings("CanBeFinal")
	private List<Button> buttons;
	private PicrossWindow name;

	public ButtonList(PicrossWindow name) {
		this.name = name;
		buttons = new ArrayList<>();
	}

	public ButtonList(PicrossWindow name, Button b) {
		this.name = name;
		buttons = new ArrayList<>(Collections.singletonList(b));
	}

	public ButtonList(PicrossWindow name, List<Button> b) {
		this.name = name;
		buttons = b;
	}

	public List<Button> getButtons() {
		return buttons;
	}

	public void addButton(Button b) {
		buttons.add(b);
	}

	public void drawAll(Graphics2D graphics2D) {
		for (Button b : buttons) {
			b.draw(graphics2D);
		}
	}

	public void addButtons(Button[] bs) {
		Collections.addAll(buttons, bs);
	}

	public void addButtons(List<Button> bs) {
		buttons.addAll(bs);
	}

	public Button get(int index) {
		return buttons.get(index);
	}

	public int size() {
		return buttons.size();
	}

	public void setVisible(boolean visible) {
		for (Button button : buttons) {
			button.setVisible(visible);
		}
	}

	public PicrossWindow getName() {
		return name;
	}

	public List<Button> toList() {
		return buttons;
	}

	public void sort() {
		List<String> buttonNames = new ArrayList<>();
		for (Button b : buttons) {
			buttonNames.add(b.getText());
		}
		Collections.sort(buttonNames);
		List<Button> newButtons = new ArrayList<>();
		for (int i = 0; i < buttons.size(); i++) {
			for (Button b : buttons) {
				if (b.getText().equals(buttonNames.get(i))) {
					newButtons.add(b);
				}
			}
		}
		buttons = newButtons;
	}
}
