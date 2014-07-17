package it.cnr.droidpark;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("SimpleDateFormat")
public class Opinion implements Parcelable, Serializable {
	
	private static final long serialVersionUID = -4024860112791177737L;
	
	private int idGame;
	private int idUser;
	private Date timestamp;
	private String msg;
	
	public Opinion(int idGame, int idUser, Date timestamp, String msg) {
		super();
		this.idGame = idGame;
		this.idUser = idUser;
		this.timestamp = timestamp;
		this.msg = msg;
	}
	
	public static final Parcelable.Creator<Opinion> CREATOR = new Parcelable.Creator<Opinion>() {
        public Opinion createFromParcel(Parcel in) {
            return new Opinion(in);
        }

        public Opinion[] newArray(int size) {
            return new Opinion[size];
        }
    };
    
    private Opinion(Parcel in) {
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
    	msg = in.readString();
    }
    
    public Opinion() {}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(idGame);
		out.writeInt(idUser);
		out.writeString(timestamp.toString());
		out.writeString(msg);
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
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timeStamp) {
		this.timestamp = timeStamp;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
