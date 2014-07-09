package cnr.Common;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContentEvaluator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -928168198376484966L;

	public final static String TAG="EVALUATOR";
	
	private List<Evaluator> evaluators;
	
	private Object contentID;
	
	public ContentEvaluator() {
		evaluators=new ArrayList<Evaluator>();
	}
	
	public void addEvaluationCriteria(Object propertyTag, Object preferenceTag, double weight) {
		if (weight<0 || weight>1 || propertyTag==null || preferenceTag==null)
			throw new IllegalArgumentException("addEvaluationCriteria(): Wrong parameter!");
		evaluators.add(new Evaluator(propertyTag, preferenceTag, weight));
	}
	
	public void addEvaluationCriteria(Object propertyTag, Object preferenceTag) {
		this.addEvaluationCriteria(propertyTag, preferenceTag, 1);
	}
	
	public Iterator<Evaluator> getIterator() {
		return evaluators.iterator();
	}
	
	public void setContentID(Object contentID) {
		this.contentID = contentID;
	}

	public Object getContentID() {
		return contentID;
	}

	public class Evaluator implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7422024805645405712L;
		private Object propertyTag;
		private Object preferenceTag;
		private double weight;
		
		public Evaluator(Object pt, Object prt, double w) {
			propertyTag=pt;
			preferenceTag=prt;
			weight=w;
		}

		public Object getPropertyTag() {
			return propertyTag;
		}

		public Object getPreferenceTag() {
			return preferenceTag;
		}

		public double getWeight() {
			return weight;
		}
	}
}
