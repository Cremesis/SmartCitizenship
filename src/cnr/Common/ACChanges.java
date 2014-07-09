package cnr.Common;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

public class ACChanges implements Serializable, Parcelable {

	private static final long serialVersionUID = 3L;

	private Map<Object, Object> changes;
	private Set<Object> subTableManaged;

	public ACChanges(Map<Object, Object> changes, Set<Object> subTableManaged) {
		this.changes=changes;
		this.subTableManaged=subTableManaged;
	}
	
	@SuppressWarnings("unchecked")
	public ACChanges(Parcel in){
		Object[] array = in.readArray(null);
		this.changes = (Map<Object, Object>) array[0];
		this.subTableManaged = (Set<Object>) array[1];
	}

	public Map<Object, Object> getChanges() {
		return changes;
	}

	public Set<Object> getSubTableManaged() {
		return subTableManaged;
	}

	@Override
	public String toString() {
		return changes.toString();
	}
	
	/**
	 * Variable used to allow the marshalling/unmarshalling of the object ACChanges
	 */
	public static final Parcelable.Creator<ACChanges> CREATOR = new Parcelable.Creator<ACChanges>() {
		public ACChanges createFromParcel(Parcel in) {
			return new ACChanges(in);
		}

		public ACChanges[] newArray(int size) {
			return new ACChanges[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Object[] array = { changes, subTableManaged};
		dest.writeArray(array);	
	}

}
