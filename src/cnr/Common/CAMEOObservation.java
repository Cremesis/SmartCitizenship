package cnr.Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CAMEOObservation implements Serializable, Parcelable {

	public static final byte UNKNOWN_TYPE = '0';
	public static final byte POLLUTION_TYPE = 'a';
	public static final byte NOISE_TYPE = 'b';
	public static final byte WEATHER_TYPE = 'c';
	
	private static final long serialVersionUID = 12L;
	private ArrayList<String> phenomenons;
	private String procedureName;
	private String[] time;
	private String[] outputNames;
	private String[] outputDefinitions;
	private String[] outputUnitOfMeasure;
	private Object[] outputTypes;
	private ArrayList<Object[]> values;
	private String url;
	private Double latitude;
	private Double longitude;
	private String locationId;
	private String locationName;
	
	private byte type;
	
	public CAMEOObservation(String url,Double latitude, Double longitude, ArrayList<String> phenomenons, String procedureName,
			String[] time, String locationId, String locationName, String[] outputNames, String[] outputDefinitions, String[] outputUnitOfMeasure, Object[] outputTypes, ArrayList<Object[]> values) {
		this.url=url;
		this.phenomenons=phenomenons;
		this.procedureName=procedureName;
		this.time=time;
		this.outputNames=outputNames;
		this.outputDefinitions=outputDefinitions;
		this.outputUnitOfMeasure=outputUnitOfMeasure;
		this.outputTypes=outputTypes;
		this.values=values;
		this.latitude=latitude;
		this.longitude=longitude;
		this.locationId=locationId;
		this.locationName=locationName;
		this.type=UNKNOWN_TYPE;
	}
	
	public CAMEOObservation(String url,Double latitude, Double longitude, ArrayList<String> phenomenons, String procedureName,
			String[] time, String locationId, String locationName, String[] outputNames, String[] outputDefinitions, String[] outputUnitOfMeasure, Object[] outputTypes, ArrayList<Object[]> values, byte type) {
		this(url,latitude,longitude,phenomenons,procedureName,time,locationId,locationName,
				outputNames,outputDefinitions,outputUnitOfMeasure,outputTypes,values);
		this.type=type;
	}
	
	@SuppressWarnings("unchecked")
	public CAMEOObservation(Parcel in){
		Bundle b=in.readBundle();
		phenomenons=b.getStringArrayList("phenomenons");
		procedureName=b.getString("procedureName");
		url=b.getString("url");
		locationId=b.getString("locationId");
		locationName=b.getString("locationName");
		type=b.getByte("type");
		outputNames=b.getStringArray("outputNames");
		outputDefinitions=b.getStringArray("outputDefinitions");
		outputUnitOfMeasure=b.getStringArray("outputUnitOfMeasure");
		latitude=b.getDouble("latitude");
		longitude=b.getDouble("longitude");
		time=(String[]) b.getStringArray("time");
		outputTypes=(Object[]) b.getSerializable("outputTypes");
		values=(ArrayList<Object[]>) b.getSerializable("values");
	}
	
	public boolean contains(double lat, double lon, double ray) {
		if(latitude==null||longitude==null) {
			return false;
		}
		
		//In degrees
		double minLatitude=latitude;
		double maxLatitude=latitude;
		double minLongitude=longitude;
		double maxLongitude=longitude;

		double R = 6371d; // earth's mean radius in km
		double d = ray/R; //radius given in km
		double lat1 = Math.toRadians(latitude);
		double lon1 = Math.toRadians(longitude);

		int degdelta=360/4;
		for(int x=0; x<360; x+=degdelta) {                      
			double brng = Math.toRadians(x);
			double latitudeRad = Math.asin(Math.sin(lat1)*Math.cos(d) + Math.cos(lat1)*Math.sin(d)*Math.cos(brng));
			double longitudeRad = (lon1 + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat1), Math.cos(d)-Math.sin(lat1)*Math.sin(latitudeRad)));
			double posx=Math.toDegrees(longitudeRad);
			double posy=Math.toDegrees(latitudeRad);
			if(posx<minLongitude)
				minLongitude=posx;
			if(posx>maxLongitude)
				maxLongitude=posx;
			if(posy<minLatitude)
				minLatitude=posy;
			if(posy>maxLatitude)
				maxLatitude=posy;
		} 

		//LAZY, check for a square
		if(lon<minLongitude||lon>maxLongitude||lat<minLatitude||lat>maxLatitude) {
			return false;
		}
		return true;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result
				+ ((locationId == null) ? 0 : locationId.hashCode());
		result = prime * result
				+ ((locationName == null) ? 0 : locationName.hashCode());
		result = prime * result
				+ ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + Arrays.hashCode(outputNames);
		result = prime * result
				+ ((procedureName == null) ? 0 : procedureName.hashCode());
		result = prime * result + Arrays.hashCode(time);
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CAMEOObservation other = (CAMEOObservation) obj;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (locationId == null) {
			if (other.locationId != null)
				return false;
		} else if (!locationId.equals(other.locationId))
			return false;
		if (locationName == null) {
			if (other.locationName != null)
				return false;
		} else if (!locationName.equals(other.locationName))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (!Arrays.equals(outputNames, other.outputNames))
			return false;
		if (procedureName == null) {
			if (other.procedureName != null)
				return false;
		} else if (!procedureName.equals(other.procedureName))
			return false;
		if (!Arrays.equals(time, other.time))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getLocationId() {
		return this.locationId;
	}
	
	public String getLocationName() {
		return this.locationName;
	}
	
	public Double getLatitude() {
		return this.latitude;
	}
	public Double getLongitude() {
		return this.longitude;
	}
	public ArrayList<String> getPhenomenons() {
		return phenomenons;
	}
	public String getProcedureName() {
		return procedureName;
	}
	public String[] getTime() {
		return time;
	}
	public String[] getOutputNames() {
		return outputNames;
	}
	public String[] getOutputDefinitions() {
		return outputDefinitions;
	}
	public String[] getOutputUnitOfMeasure() {
		return outputUnitOfMeasure;
	}
	public Object[] getOutputTypes() {
		return outputTypes;
	}
	public ArrayList<Object[]> getValues() {
		return values;
	}
	public String getUrl() {
		return url;
	}
	public String getProcedureId() {
		return this.procedureName;
	}

	/**
	 * Variable used to allow the marshalling/unmarshalling of the object CAMEOOffering
	 */
	public static final Parcelable.Creator<CAMEOObservation> CREATOR = new Parcelable.Creator<CAMEOObservation>() {
		public CAMEOObservation createFromParcel(Parcel in) {
			return new CAMEOObservation(in);
		}

		public CAMEOObservation[] newArray(int size) {
			return new CAMEOObservation[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b=new Bundle();
		b.putStringArrayList("phenomenons", phenomenons);
		b.putString("procedureName", procedureName);
		b.putString("url", url);
		b.putString("locationId", locationId);
		b.putString("locationName", locationName);
		b.putByte("type", type);
		b.putStringArray("outputNames", outputNames);
		b.putStringArray("outputDefinitions", outputDefinitions);
		b.putStringArray("outputUnitOfMeasure", outputUnitOfMeasure);
		b.putDouble("latitude", latitude);
		b.putDouble("longitude", longitude);
		b.putStringArray("time", time);
		b.putSerializable("outputTypes", outputTypes);
		b.putSerializable("values", values);
		dest.writeBundle(b);
	}
}
