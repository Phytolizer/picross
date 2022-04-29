package picross;

@SuppressWarnings("WeakerAccess")
public class Box {
	private boolean canModify;
	private final int x;
	private final int y;
	public static final int EMPTY = 0;
	public static final int SOLVED = 1;
	public static final int FLAGGED = 2;
	public static final int MISTAKE = 3;
	/**
	 * 0 = empty
	 * 1 = solved
	 * 2 = flagged
	 * 3 = mistake
	 * all boxes start empty
	 * game is not complete until # of solved tiles = number in solution
	 * //x is left->right, y is down->up
	 */
	private int state;


	public Box(int x_, int y_) {
		x = x_;
		y = y_;
		state = EMPTY;
		setCanModify(true);
	}

	public void impossibru() {//toggles flagged state
		if (state == EMPTY)
			setState(FLAGGED);
		else if (state == FLAGGED)
			setState(EMPTY);
	}

	public boolean green(Grid solution) {//checks solution
		if (solution.getBox(x, y).getState() == SOLVED && state == EMPTY) {
			setState(SOLVED);
			setCanModify(false);
			return true;
		} else if (state == FLAGGED) {
			setState(EMPTY);
			setCanModify(false);
			return true;
		} else if (state == MISTAKE) {
			return true;
		} else if (state == EMPTY) {
			setState(MISTAKE);
			return ! canModify();//ONLY returns false if the box can be modified, i.e is deliberately clicked on
		}
		setCanModify(false);
		return true;

	}

	public int[] getPos() {
		return new int[] {x, y};
	}

	public int getState() {
		return state;
	}

	public void setState(int s) {
		if (canModify())
			state = s;
	}

	public boolean canModify() {
		return canModify;
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}
}
