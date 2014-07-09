package cnr.Common;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class CAMEOOffering implements Serializable, Parcelable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private ArrayList<String> observedProperties;
	private ArrayList<String> offeringProcedures;
	
	public CAMEOOffering(String id, String name, ArrayList<String> observedProperties, ArrayList<String> offeringProcedures) {
		this.id=id;
		this.name=name;
		this.observedProperties=observedProperties;
		this.offeringProcedures=offeringProcedures;
	}
	
	@SuppressWarnings("unchecked")
	public CAMEOOffering(Parcel in){
		Object[] array = in.readArray(null);
		this.id = (String) array[0];
		this.name = (String) array[1];
		this.observedProperties = (ArrayList<String>) array[2];
		this.offeringProcedures = (ArrayList<String>) array[3];
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getObservedProperties() {
		return observedProperties;
	}
	
	public ArrayList<String> getOfferingProcedures() {
		return offeringProcedures;
	}

	@Override
	public String toString() {
		String result="Offering ID: "+id+" name: "+name;
		for(String obPro:observedProperties) {
			result=result+"\nobsevedProperty: "+obPro;
		}
		result=result+"\nofferingProcedures: "+offeringProcedures;
		return result;
	}
	
	/**
	 * Variable used to allow the marshalling/unmarshalling of the object CAMEOOffering
	 */
	public static final Parcelable.Creator<CAMEOOffering> CREATOR = new Parcelable.Creator<CAMEOOffering>() {
		public CAMEOOffering createFromParcel(Parcel in) {
			return new CAMEOOffering(in);
		}

		public CAMEOOffering[] newArray(int size) {
			return new CAMEOOffering[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Object[] array = { id, name, observedProperties, offeringProcedures};
		dest.writeArray(array);	
	}
	
}
