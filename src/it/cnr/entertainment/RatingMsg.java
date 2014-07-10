package it.cnr.entertainment;

import java.util.Date;

public class RatingMsg {
	
	public RatingMsg(int idGame, int idUser, Date timestamp, float eval) {
		super();
		this.idGame = idGame;
		this.idUser = idUser;
		this.timestamp = timestamp;
		this.eval = eval;
	}
	
	
	private int idGame;
	private int idUser;
	private Date timestamp;
	private float eval;
	
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
	public float getEval() {
		return eval;
	}
	public void setEval(int eval) {
		this.eval = eval;
	}
}
