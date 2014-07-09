package it.cnr.entertainment;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Application;

public class ApplicationEntertainment extends Application {
	
	// TODO: persistence of the data missing
	
	public final Integer NUMBEROFGAMES = 4;
	
	private Hashtable<Integer, QueueMsg> queueList; // < IDgioco, QueueMsg > 
	private Hashtable<Integer, Map<Integer, RatingMsg>> ratingList; // < IDgioco, <IDutente,RatingMsg> >
	private Hashtable<Integer, Map<Integer, Opinion>> opinionList; // < IDgioco, <IDutente,Opinion> >
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		queueList = new Hashtable<Integer, QueueMsg>(NUMBEROFGAMES);
		ratingList = new Hashtable<Integer, Map<Integer, RatingMsg>>(NUMBEROFGAMES);
		opinionList = new Hashtable<Integer, Map<Integer, Opinion>>(NUMBEROFGAMES);
	}
	
	public float getRatingAverage(Integer gameID) {
		float average = 0f;
		int numGameRatings = 0;
		
		Set<Entry<Integer, RatingMsg>> currentGameRatings = ratingList.get(gameID).entrySet();
		for(Entry<Integer, RatingMsg> entry : currentGameRatings) {
			average += entry.getValue().getEval();
			numGameRatings++;
		}
		
		return average / numGameRatings;
	}
	
	public Map<Integer, RatingMsg> getRatingMsg(Integer gameID) {
		return ratingList.get(gameID);
	}
	
	public QueueMsg getQueueMsg(Integer gameID) {
		return queueList.get(gameID);
	}
	
	public Opinion getGameOpinion(Integer gameID, Integer userID) {
		return opinionList.get(gameID).get(userID);
	}
	
	/**
	 * Insert the opinion. If there is an opinion of the same user about the
	 * same game, keep the most recent one only
	 * 
	 * @param gameID
	 * @param userID
	 * @param opinion
	 * @return true if the opinion was inserted (because more recent or new), false otherwise
	 */
	public boolean insertOpinion(Integer gameID, Integer userID, Opinion opinion) {
		Map<Integer, Opinion> gameOpinions = opinionList.get(gameID);
		if(gameOpinions != null) {
			Opinion currentUserOpinion = gameOpinions.get(userID);
			if(currentUserOpinion != null) {
				if(currentUserOpinion.getTimestamp().compareTo(opinion.getTimestamp()) > 0)
					// The local opinion is newer than the "new" one. Don't do anything
					return false;
			}
		}
		
		Map<Integer, Opinion> userOpinion = new Hashtable<Integer, Opinion>();
		userOpinion.put(userID, opinion);
		opinionList.put(gameID, userOpinion);
		return true;
	}
	
	/**
	 * Insert the rating. If there is a rating of the same user about the
	 * same game, keep the most recent one only
	 * 
	 * @param gameID
	 * @param userID
	 * @param rating
	 * @return true if the rating was inserted (because more recent or new), false otherwise
	 */
	public boolean insertRating(Integer gameID, Integer userID, RatingMsg rating) {
		Map<Integer, RatingMsg> gameRatings = ratingList.get(gameID);
		if(gameRatings != null) {
			RatingMsg currentUserRating = gameRatings.get(userID);
			if(currentUserRating != null) {
				if(currentUserRating.getTimestamp().compareTo(rating.getTimestamp()) > 0)
					// The local rating is newer than the "new" one. Don't do anything
					return false;
			}
		}
		
		Map<Integer, RatingMsg> userRating = new Hashtable<Integer, RatingMsg>();
		userRating.put(userID, rating);
		ratingList.put(gameID, userRating);
		return true;
	}
	
	/**
	 * Insert the queue. If there is a queue of the same game, keep the most recent one only
	 * 
	 * @param gameID
	 * @param queue
	 * @return
	 */
	public boolean InsertQueue(Integer gameID, QueueMsg queue) {
		QueueMsg gameQueue = queueList.get(gameID);
		if(gameQueue != null) {
			if(gameQueue.getTimestamp().compareTo(queue.getTimestamp()) > 0)
				// The local queue is newer than the "new" one. Don't do anything
				return false;
		}
		
		queueList.put(gameID, queue);
		return true;
	}
}
