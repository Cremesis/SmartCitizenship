package cnr.Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

public class CAMEOSensor implements Serializable, Parcelable {

	private static final long serialVersionUID = 14L;
	private Double latitude;
	private Double longitude;
	private String uniqueId;
	private String shortName;
	private String longName;
	private String[] outputNames;
	private String[] outputDefinitions;
	private String[] offeringIds;
	private String[] offeringDescriptions;
	private String[] outputUnitOfMeasure;
	private Class<?>[] outputTypes;
	private String url;
	
	public CAMEOSensor(String url, String uniqueId, String shortName, String longName, Double latitude, Double longitude, String[] outputNames, String[] outputDefinitions, String[] offeringIds, String[] offeringDescriptions, Class<?>[] outputTypes) {
		this.url=url;
		this.uniqueId=uniqueId;
		this.shortName=shortName;
		this.longName=longName;
		this.latitude=latitude;
		this.longitude=longitude;
		this.outputNames=outputNames;
		this.outputDefinitions=outputDefinitions;
		this.offeringIds=offeringIds;
		this.offeringDescriptions=offeringDescriptions;
		this.outputTypes=outputTypes;
		outputUnitOfMeasure=new String[outputNames.length];
	}
	
	public void setOutputUnitOfMeasure(String[] outputUnitOfMeasure) {
		this.outputUnitOfMeasure=outputUnitOfMeasure;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public String getLongName() {
		return this.longName;
	}
	
	public void setUniqueId(String uniqueId) {
		this.uniqueId=uniqueId;
	}
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	
	public String[] getOutputNames() {
		return this.outputNames;
	}
	
	public String[] getOutputDefinitions() {
		return this.outputDefinitions;
	}
	
	public String[] getOutputUnitOfMeasure() {
		return this.outputUnitOfMeasure;
	}
	
	public String[] getOfferingIds() {
		return this.offeringIds;
	}
	
	public String[] getOfferingDescriptions() {
		return this.offeringDescriptions;
	}
	
	public Class<?>[] getOutputTypes() {
		return this.outputTypes;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	
	public CAMEOObservation createObservation(Double latitude, Double longitude, ArrayList<String> phenomenons,
			String[] time, String locationId, String locationName, Date[] valuesTime, ArrayList<Object[]> values) {
		
		//controllo sui values?
		CAMEOObservation co=new CAMEOObservation(url,latitude,longitude,phenomenons,uniqueId,time,locationId,locationName,outputNames,outputDefinitions,outputUnitOfMeasure,outputTypes,values);
		return co;
	}
	
	@Override
	public String toString() {
		String result="Server:"+url+"\nuniqueId:"+uniqueId+"\nshortName:"+shortName+"\nlongName:"+longName+
				"\n@lat:"+latitude+" lon:"+longitude;
		result+="\nofferings:";
		for(int i=0;i<offeringIds.length;i++) {
			result+="\n"+i+":"+offeringIds[i]+"-"+offeringDescriptions[i];
		}
		result+="\noutputs:";
		for(int i=0;i<outputNames.length;i++) {
			result+="\n"+i+":"+outputNames[i]+"-"+outputDefinitions[i]+"-"+outputUnitOfMeasure[i]+"-"+outputTypes[i];
		}

		return result;
	}
	
	public CAMEOSensor(Parcel in) {
		latitude=in.readDouble();
		longitude=in.readDouble();
		uniqueId=in.readString();
		shortName=in.readString();
		longName=in.readString();
		outputNames=(String[]) in.readArray(CAMEOSensor.class.getClassLoader());
		outputDefinitions=(String[]) in.readArray(CAMEOSensor.class.getClassLoader());
		offeringIds=(String[]) in.readArray(CAMEOSensor.class.getClassLoader());
		offeringDescriptions=(String[]) in.readArray(CAMEOSensor.class.getClassLoader());
		outputUnitOfMeasure=(String[]) in.readArray(CAMEOSensor.class.getClassLoader());
		outputTypes=(Class<?>[]) in.readArray(CAMEOSensor.class.getClassLoader());
		url=in.readString();
		
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(uniqueId);
		dest.writeString(shortName);
		dest.writeString(longName);
		dest.writeArray(outputNames);
		dest.writeArray(outputDefinitions);
		dest.writeArray(offeringIds);
		dest.writeArray(offeringDescriptions);
		dest.writeArray(outputUnitOfMeasure);
		dest.writeArray(outputTypes);
		dest.writeString(url);
	}
	
	 public static final Parcelable.Creator<CAMEOSensor> CREATOR = new Parcelable.Creator<CAMEOSensor>() {
         public CAMEOSensor createFromParcel(Parcel in) {
             return new CAMEOSensor(in); 
         }

         public CAMEOSensor[] newArray(int size) {
             return new CAMEOSensor[size];
         }
     };
}