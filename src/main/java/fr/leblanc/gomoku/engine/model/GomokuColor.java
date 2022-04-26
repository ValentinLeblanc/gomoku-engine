package fr.leblanc.gomoku.engine.model;

public enum GomokuColor {

	BLACK(0), WHITE(1), NONE(-1);

	public int toNumber() {
		return number;
	}

	private int number;

	GomokuColor(int number) {
		this.number = number;
	}

}
