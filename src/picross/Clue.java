package picross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Clue {
	/**
	 * Type of clue. 0 denotes a clue for a row, 1 for a column.
	 */
	private final int type;
	private final int position;
	@SuppressWarnings("CanBeFinal")
	private List<Integer> values = new ArrayList<>();

	public Clue(int position, int type) {
		this.position = position;
		this.type = type;
	}

	/**
	 * @return Returns an integer list of the clue, or 0 if the clue contains nothing.
	 */
	public List<Integer> getValues() {
		if (values.size() > 0) {
			return values;
		} else {
			return new ArrayList<>(Collections.singletonList(0));
		}
	}

	public void generateClue(Grid grid) {
		int temp = 0, x = 0, length = (type == 0 ? grid.sizeX : grid.sizeY), y = position;
		while (x < length) {
			Box currBox = grid.getBox(type == 0 ? x : y, type == 0 ? y : x);
			if (currBox.getState() == 1)
				temp++;
			else if (temp > 0) {
				values.add(temp);
				temp = 0;
			}
			x++;
		}
		if (temp > 0)
			values.add(temp);
	}

	public String toString() {
		char c = (type == 0 ? ' ' : '\n');
		String out = "";
		for (int i = 0; i < values.size(); i++) {
			out += Integer.toString(values.get(i));
			if (i + 1 < values.size())
				out += c;
		}
		if (out.equals(""))
			out = "0";
		return out;
	}

	/**
	 * @param override If true, will return an easily readable clue. Otherwise returns a string ready for printing in game.
	 * @return Returns the clue represented as a string.
	 */
	@SuppressWarnings("SameParameterValue")
	public String toString(boolean override) {
		char c;
		if (override) {
			c = ' ';
		} else {
			c = (type == 0 ? ' ' : '\n');
		}
		String out = "";
		for (int i = 0; i < values.size(); i++) {
			out += Integer.toString(values.get(i));
			if (i + 1 < values.size())
				out += c;
		}
		if (out.equals(""))
			out = "0";
		return out;
	}
}
