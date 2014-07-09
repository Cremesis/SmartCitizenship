package cnr.Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class CAMEOObservationRequest implements Serializable, Parcelable {

	private static final long serialVersionUID = 5L;
	private String offeringId;
	private ArrayList<String> observedProperties;
	private Double northeastLatitude;
	private Double northeastLongitude;
	private Double southwestLatitude;
	private Double southwestLongitude;
	private Date startTime;
	private Date endTime;
	private long timeout;

	public CAMEOObservationRequest(String offeringId, ArrayList<String> observedProperties, Double northeastLatitude, Double northeastLongitude, Double southwestLatitude, Double southwestLongitude, Date startTime, Date endTime, long timeout) {
		this.offeringId=offeringId;
		this.observedProperties=observedProperties;
		this.northeastLatitude=northeastLatitude;
		this.northeastLongitude=northeastLongitude;
		this.southwestLatitude=southwestLatitude;
		this.southwestLongitude=southwestLongitude;
		this.startTime=startTime;
		this.endTime=endTime;
		this.timeout=timeout;
	}

	public CAMEOObservationRequest( String offeringId, ArrayList<String> observedProperties, Date startTime, Date endTime, long timeout) {
		this(offeringId,observedProperties,null,null,null,null,startTime,endTime,timeout);
	}

	@SuppressWarnings("unchecked")
	public CAMEOObservationRequest(Parcel in){
		Object[] array = in.readArray(null);
		this.offeringId = (String) array[0];
		this.observedProperties = (ArrayList<String>) array[1];
		this.northeastLatitude = (Double) array[2];
		this.northeastLongitude = (Double) array[3];
		this.southwestLatitude = (Double) array[4];
		this.southwestLongitude = (Double) array[5];
		this.startTime = (Date) array[6];
		this.endTime = (Date) array[7];
		this.timeout = (Long) array[8];
	}

	public long getTimeout() {
		return timeout;
	}

	public String getOfferingId() {
		return offeringId;
	}
	public ArrayList<String> getObserverProperties() {
		return observedProperties;
	}
	public Date getStartTime() {
		return startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public Double getNorthEastLatitude() {
		return northeastLatitude;
	}
	public Double getNorthEastLongitude() {
		return northeastLongitude;
	}
	public Double getSouthWestLatitude() {
		return southwestLatitude;
	}
	public Double getSouthWestLongitude() {
		return southwestLongitude;
	}

	/**
	 * Variable used to allow the marshalling/unmarshalling of the object
	 */
	public static final Parcelable.Creator<CAMEOObservationRequest> CREATOR = new Parcelable.Creator<CAMEOObservationRequest>() {
		public CAMEOObservationRequest createFromParcel(Parcel in) {
			return new CAMEOObservationRequest(in);
		}

		public CAMEOObservationRequest[] newArray(int size) {
			return new CAMEOObservationRequest[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Object[] array = {offeringId, observedProperties, northeastLatitude, northeastLongitude, southwestLatitude, southwestLongitude, startTime, endTime, timeout};
		dest.writeArray(array);
	}
}
