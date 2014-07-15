package it.cnr.droidpark;

import java.io.Serializable;
import java.util.Date;

import android.os.Parcelable;

public interface ApplicationMsg extends Parcelable, Serializable {
	public int getIdGame();
	public void setIdGame(int IdGame);
	public Date getTimestamp();
	public void setTimestamp(Date timestamp);
	public int getNumCopies();
	public void setNumCopies(int numCopies);
	public ApplicationMsg duplicate();
}
