package it.cnr.droidpark;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Application;
import android.util.Log;

public class ApplicationDroidPark extends Application {
	
	// TODO: persistence of the data missing
	
    private static final String TAG = "ApplicationDroidPark";
	public final Integer NUMBER_OF_GAMES = 4;
	public final Integer NUMBER_OF_COPIES = 50;
	
	private Hashtable<Integer, QueueMsg> queueList; // < IDgioco, QueueMsg > 
	private Hashtable<Integer, Map<Integer, RatingMsg>> ratingList; // < IDgioco, <IDutente,RatingMsg> >
	private Hashtable<Integer, Map<Integer, Opinion>> opinionList; // < IDgioco, <IDutente,Opinion> >
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		queueList = new Hashtable<Integer, QueueMsg>(NUMBER_OF_GAMES);
		ratingList = new Hashtable<Integer, Map<Integer, RatingMsg>>(NUMBER_OF_GAMES);
		opinionList = new Hashtable<Integer, Map<Integer, Opinion>>(NUMBER_OF_GAMES);
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
	
	public Map<Integer, Opinion> getAllGameOpinions(Integer gameID) {
	    	return opinionList.get(gameID);
	}
	
	/**
	 * Insert the opinion. If there is an opinion of the same user about the
	 * same game, keep the most recent one only
	 * 
	 * @param gameID
	 * @param userID
	 * @param opinion
	 * @return true if the opinion was inserted (because more recent or new),
	 *         false otherwise
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
		
		Log.d(TAG, "inserted/updated opinion");
		
		// Insert new opinion
		Map<Integer, Opinion> userOpinion = new Hashtable<Integer, Opinion>();
		userOpinion.put(userID, opinion);
		opinionList.put(gameID, userOpinion);
		return true;
	}
	
	/**
	 * Insert the rating. If there is a rating of the same user about the same
	 * game, keep the most recent one only. If the user, game and timestamp are
	 * the same, sums the copies
	 * 
	 * @param gameID
	 * @param userID
	 * @param rating
	 * @return true if the rating was inserted (because number of copies are not
	 *         0, or because it is more recent or new), false otherwise
	 */
	public boolean insertRating(Integer gameID, Integer userID, RatingMsg rating) {
		Map<Integer, RatingMsg> gameRatings = ratingList.get(gameID);
		if(gameRatings != null) {
			RatingMsg currentUserRating = gameRatings.get(userID);
			if(currentUserRating != null) {
				int compare = currentUserRating.getTimestamp().compareTo(rating.getTimestamp());
				if(compare > 0) { // The local rating is newer than the "new" one. Don't do anything
					Log.d(TAG, "\"new\" rating is older than present");
					return false;
				} if(compare == 0) { // The local rating is the same of the "new" one. Sum the copies.
					Log.d(TAG, "added copies in rating");
					currentUserRating.setNumCopies(currentUserRating.getNumCopies() + rating.getNumCopies());
					return true;
				}
			}
		}
		
		Log.d(TAG, "inserted/updated rating");
		
		// Decrement numCopies by 1
		int ratingNumCopies = rating.getNumCopies();
		if(ratingNumCopies > 0) rating.setNumCopies(ratingNumCopies-1);
		
		// Insert new rating
		Map<Integer, RatingMsg> userRating = ratingList.get(gameID);
		if(userRating == null) userRating = new Hashtable<Integer, RatingMsg>();
		userRating.put(userID, rating);
		ratingList.put(gameID, userRating);
		return true;
	}
	
	/**
	 * Insert the queue. If there is a queue of the same game, keep the most
	 * recent one only. If the game and timestamp are the same, sums the
	 * copies
	 * 
	 * @param gameID
	 * @param queue
	 * @return true if the queue was inserted (because number of copies are not
	 *         0, or because it is more recent or new), false otherwise
	 */
	public boolean insertQueue(Integer gameID, QueueMsg queue) {
		QueueMsg gameQueue = queueList.get(gameID);
		if(gameQueue != null) {
			int compare = gameQueue.getTimestamp().compareTo(queue.getTimestamp());
			if(compare > 0) { // The local queue is newer than the "new" one. Don't do anything
				Log.d(TAG, "\"new\" queue is older than present");
				return false;
			} if(compare == 0) { // The local queue is the same of the "new" one. Sum the copies.
				Log.d(TAG, "added copies in queue");
				gameQueue.setNumCopies(gameQueue.getNumCopies() + queue.getNumCopies());
				return true;
			}
		}
		
		Log.d(TAG, "inserted/updated queue");
		
		// Decrement numCopies by 1
		int queueNumCopies = queue.getNumCopies();
		if(queueNumCopies > 0) queue.setNumCopies(queueNumCopies-1);
		
		// Insert new queue
		queueList.put(gameID, queue);
		return true;
	}
}
