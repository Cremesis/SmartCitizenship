package cnr.Common;

import java.io.Serializable;

public class Query implements Serializable {
	
	private static final long serialVersionUID = 6779091629483322130L;
	
	private String query=null;
	private String sensorQuery=null;
	private String phenomenonQuery=null;
	private String positionQuery=null;
	private String timeQuery=null;
	
	public Query(String sensorName, String phenomenon, Double longitude, Double latitude, Float positionAccuracy, Long minTimestamp, Long maxTimestamp) {
		if(sensorName!=null) {
			sensorQuery="SENSORNAME='"+sensorName+"'";
			query=sensorQuery;
		}
		if(phenomenon!=null) {
			phenomenonQuery="PHENOMENON='"+phenomenon+"'";
			if(query!=null) {
				query=query+" AND "+phenomenonQuery;
			} else {
				query=phenomenonQuery;
			}
		}
		if(longitude!=null&&latitude!=null&&positionAccuracy!=null) {
			double minLongitude=longitude-positionAccuracy;
			double maxLongitude=longitude+positionAccuracy;
			double minLatitude=latitude-positionAccuracy;
			double maxLatitude=latitude+positionAccuracy;
			positionQuery="LONGITUDE>="+minLongitude+" AND LONGITUDE<="+maxLongitude+" AND LATITUDE>="+minLatitude+" AND LATITUDE<="+maxLatitude;
			if(query!=null) {
				query=query+" AND "+positionQuery;
			} else {
				query=positionQuery;
			}
		}
		if(minTimestamp!=null&&maxTimestamp!=null) {
			timeQuery="TIMESTAMP<="+maxTimestamp+" AND TIMESTAMP>="+minTimestamp;
			if(query!=null) {
				query=query+" AND "+timeQuery;
			} else {
				query=timeQuery;
			}
		}
	}
	
	public String getQuery() {
		return query;
	}
	
}
