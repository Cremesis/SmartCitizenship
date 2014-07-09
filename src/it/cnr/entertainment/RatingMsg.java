package it.cnr.entertainment;

import java.util.Date;

public class RatingMsg {
	private int idGame;
	private int idUser;
	private Date timestamp;
	private int eval;
	
	public int getIdGame() {
		return idGame;
	}
	public void setIdGame(int idGame) {
		this.idGame = idGame;
	}
	public int getIdUser() {
		return idUser;
	}
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getEval() {
		return eval;
	}
	public void setEval(int eval) {
		this.eval = eval;
	}
}
