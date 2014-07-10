package it.cnr.entertainment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("SimpleDateFormat")
public class QueueMsg implements Parcelable {
	private int idGame;
	private Date timestamp;
	private int duration;
	
    public static final Parcelable.Creator<QueueMsg> CREATOR = new Parcelable.Creator<QueueMsg>() {
        public QueueMsg createFromParcel(Parcel in) {
            return new QueueMsg(in);
        }

        public QueueMsg[] newArray(int size) {
            return new QueueMsg[size];
        }
    };
    
    private QueueMsg(Parcel in) {
    	DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    	
    	idGame = in.readInt();
    	try {
    		timestamp = formatter.parse(in.readString());
		} catch (Exception e) {
			timestamp = new Date();
			timestamp.setTime(0);
			e.printStackTrace();
		}
    	duration = in.readInt();
    }
    
    public QueueMsg() {}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(idGame);
		out.writeString(timestamp.toString());
		out.writeInt(duration);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getIdGame() {
		return idGame;
	}
	public void setIdGame(int idGame) {
		this.idGame = idGame;
	}
}
