package fr.leblanc.gomoku.engine.model.messaging;

public class PercentCompleted {

	private int index;
	private int percent;
	
	public PercentCompleted(int index, int percent) {
		super();
		this.index = index;
		this.percent = percent;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	
}
