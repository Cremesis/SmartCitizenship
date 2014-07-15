package it.cnr.droidpark;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("SimpleDateFormat")
public class QueueMsg implements ApplicationMsg {
	
	private static final long serialVersionUID = -1410878463652251245L;

	private int idGame;
	private Date timestamp;
	private int duration;
	private int numCopies;
	
	public QueueMsg(int idGame, Date timestamp, int duration, int numCopies) {
		super();
		this.idGame = idGame;
		this.timestamp = new Date(timestamp.getTime());
		this.duration = duration;
		this.numCopies = numCopies;
	}
	
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
    	numCopies = in.readInt();
    }
    
    public QueueMsg() {}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(idGame);
		out.writeString(timestamp.toString());
		out.writeInt(duration);
		out.writeInt(numCopies);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public ApplicationMsg duplicate() {
		QueueMsg copy = new QueueMsg(idGame, timestamp, duration, numCopies);
		return copy;
	}
	
	@Override
	public Date getTimestamp() {
		return timestamp;
	}
	@Override
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
	@Override
	public int getNumCopies() {
		return numCopies;
	}
	@Override
	public void setNumCopies(int numCopies) {
		this.numCopies = numCopies;
	}
}
