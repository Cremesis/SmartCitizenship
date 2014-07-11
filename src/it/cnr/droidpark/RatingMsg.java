package it.cnr.droidpark;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("SimpleDateFormat")
public class RatingMsg implements ApplicationMsg {
	
	private static final long serialVersionUID = -8179313354743538606L;
	
	private int idGame;
	private int idUser;
	private Date timestamp;
	private float eval;
	private int numCopies;
	
	public RatingMsg(int idGame, int idUser, Date timestamp, float eval, int numCopies) {
		super();
		this.idGame = idGame;
		this.idUser = idUser;
		this.timestamp = timestamp;
		this.eval = eval;
		this.numCopies = numCopies;
	}
	
	public static final Parcelable.Creator<RatingMsg> CREATOR = new Parcelable.Creator<RatingMsg>() {
        public RatingMsg createFromParcel(Parcel in) {
            return new RatingMsg(in);
        }

        public RatingMsg[] newArray(int size) {
            return new RatingMsg[size];
        }
    };
    
    private RatingMsg(Parcel in) {
    	DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    	
    	idGame = in.readInt();
    	idUser = in.readInt();
    	try {
    		timestamp = formatter.parse(in.readString());
		} catch (Exception e) {
			timestamp = new Date();
			timestamp.setTime(0);
			e.printStackTrace();
		}
    	eval = in.readFloat();
    	numCopies = in.readInt();
    }
    
    public RatingMsg() {}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(idGame);
		out.writeInt(idUser);
		out.writeString(timestamp.toString());
		out.writeFloat(eval);
		out.writeInt(numCopies);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
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
	@Override
	public Date getTimestamp() {
		return timestamp;
	}
	@Override
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public float getEval() {
		return eval;
	}
	public void setEval(int eval) {
		this.eval = eval;
	}
	@Override
	public int getNumCopies() {
		return numCopies;
	}
	@Override
	public void setNumCopies(int numCopies) {
		this.numCopies = numCopies;
	}
}
