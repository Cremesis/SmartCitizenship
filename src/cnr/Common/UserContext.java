package cnr.Common;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class UserContext implements Parcelable {
	
	static final public String UUID = "UUID";
	static final public String NAME = "NAME";
	static final public String PHOTO = "PHOTO";
	static final public String EMAIL = "EMAIL";
	static final public String GENDER = "GENDER";
	static final public String AGE = "AGE";
	static final public String COMMUNITY = "COMMUNITY";
	
	static final public byte MALE = 'm';
	static final public byte FEMALE = 'f';
	
	private Map<Object,Object> context;
	
	public UserContext(String uuid, String name, String community) {
		context=new HashMap<Object, Object>();
		context.put(UUID, uuid);
		context.put(NAME, name);
		context.put(COMMUNITY, community);
	}
	
	public UserContext(Parcel in) {
		context=new HashMap<Object, Object>();
		in.readMap(context, UserContext.class.getClassLoader());
	}
	
	public UserContext(Map<Object, Object> usrContext) {
		this.context=usrContext;
	}
	
	public boolean isEmpty() {
		return context.isEmpty();
	}
	
	public String getUUID() {
		return (String) context.get(UUID);
	}
	
	public String getName() {
		return (String) context.get(NAME);
	}
	
	public String getCommunity() {
		return (String) context.get(COMMUNITY);
	}
	
	public byte[] getPhoto() {
		return (byte[]) context.get(PHOTO);
	}
	
	public String getEmail() {
		return (String) context.get(EMAIL);
	}
	
	public Byte getGender() {
		return (Byte) context.get(GENDER);
	}
	
	public Integer getAge() {
		return (Integer) context.get(AGE);
	}
	
	public Map<Object,Object> getMap() {
		return context;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeMap(context);
	}
	
	 public static final Parcelable.Creator<UserContext> CREATOR = new Parcelable.Creator<UserContext>() {
         public UserContext createFromParcel(Parcel in) {
             return new UserContext(in); 
         }

         public UserContext[] newArray(int size) {
             return new UserContext[size];
         }
     };
	
}
