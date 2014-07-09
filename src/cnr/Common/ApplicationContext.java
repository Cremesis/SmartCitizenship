package cnr.Common;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cnr.Common.PlatformInterface;
import android.os.RemoteException;

/**
 * 
 * 
 * This class represents the context informations defined by an application.
 * Since the internal implementation of the contexts is not visible outside CAMEO,
 * ApplicationContext acts as a container allowing registered applications to manage 
 * their own context informations inside the middleware.
 * The context informations are represented as a set of (name, value) pairs
 * 
 * @author Claudio Scandura (cl.scandura@gmail.com)
 * 
 */

public class ApplicationContext implements Serializable {

	private static final long serialVersionUID = 6L;

	/**
	 * The hash map where the context informations are stored as (name, value) pairs
	 */
	private Map<Object, Object> valuePairs;
	private Set<Object> subTableManaged;
	private Map<Object, Object> changes;
	
	/**
	 * Instantiates a new empty ApplicationContext
	 */
	public ApplicationContext() {
		valuePairs=(Map<Object, Object>) Collections.synchronizedMap(new HashMap<Object, Object>());
		subTableManaged=(Set<Object>) Collections.synchronizedSet(new HashSet<Object>());
		changes=(Map<Object, Object>) Collections.synchronizedMap(new HashMap<Object, Object>());
//		nullReferences=new ArrayList<Object>();
//		comparator=new InternalComparator();
	}
	
	/**
	 * Gets one object from the context if a mapping exists
	 * 
	 * @param key The name that should map to the desired object 
	 * 
	 * @return the object corresponding to the specified key if a mapping exists
	 * <p> null if no mapping exists
	 */
	public Object getValue(Object key) {
		return valuePairs.get(key);
	}
	
	public Object getSubValue(Object tableKey, Object key) {
		if(subTableManaged.contains(tableKey)) {
			Object mapObject=valuePairs.get(tableKey);
			if(mapObject!=null && mapObject instanceof HashMap) {
				HashMap<Object, Object> map=(HashMap<Object, Object>)mapObject;
				return map.get(key);
			}
		}
		return null;
	}
	
	/**
	 * Adds a (name, value) pair to this context
	 * 
	 * @param key The name of the pair
	 * @param value The object to associate to the key
	 */
	public void addValue(Object key, Object value) {
		if (value==null) {
			removeValue(key);
		}
		else {
			valuePairs.put(key, value);
			changes.put(key, value);
		}
	}
	
	public void addSubValue(Object tableKey, Object key, Object value) {
		if(subTableManaged.contains(tableKey)) {
			Object mapObject=valuePairs.get(tableKey);
			if(mapObject!=null && mapObject instanceof HashMap) {
				if(value==null) {
					removeSubValue(tableKey,key);
				} else {
					HashMap<Object, Object> map=(HashMap<Object, Object>)mapObject;
					map.put(key, value);
					HashMap<Object, Object> subTable=(HashMap<Object, Object>) changes.get(tableKey);
					if(subTable==null) {
						subTable=new HashMap<Object, Object>();
					}
					subTable.put(key, value);
					changes.put(tableKey, subTable);
				}
			}
		}
	}
	
	public boolean declareSubTable(Object tableKey) {
		if(!subTableManaged.contains(tableKey)) {
			subTableManaged.add(tableKey);
			valuePairs.put(tableKey, new HashMap<Object, Object>());
			changes.put(tableKey, new HashMap<Object, Object>());
			return true;
		}
		return false;
	}
	
	public Set<Object> getSubTablesKeys() {
		return subTableManaged;
	}
	
	/**
	 * Adds a set of (name, value) pairs to this context
	 * 
	 * @param s The set of pairs to add
	 */
	public void addValues(Set<Entry<Object, Object>> s) {
		if (s!=null&&!s.isEmpty())
			for (Entry<Object, Object> e: s)
				addValue(e.getKey(), e.getValue());
	}
	
	public void addSubValues(Object tableKey, Set<Entry<Object, Object>> s) {
		if(subTableManaged.contains(tableKey)&&s!=null&&!s.isEmpty()) {
			for (Entry<Object, Object> e: s)
				addSubValue(tableKey, e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Removes an object from this context
	 * 
	 * @param key The name that should map to that object
	 */
	public void removeValue(Object key) {
		valuePairs.put(key, null);
		changes.put(key, null);
		//nullReferences.add(key);
	}

	public void removeSubValue(Object tableKey, Object key) {
		if(subTableManaged.contains(tableKey)) {
			Object mapObject=valuePairs.get(tableKey);
			if(mapObject!=null && mapObject instanceof HashMap) {
				HashMap<Object, Object> map=(HashMap<Object, Object>)mapObject;
				map.put(key, null);
				HashMap<Object, Object> subTable=(HashMap<Object, Object>) changes.get(tableKey);
				if(subTable==null) {
					subTable=new HashMap<Object, Object>();
				}
				subTable.put(key, null);
				changes.put(tableKey, subTable);
			}
		}
	}
	
	/**
	 * Removes a set of objects from this context
	 * 
	 * @param s The set of keys that should map to those objects
	 */
	public void removeValues(Set<Entry<Object, Object>> s) {
		if (s!=null&&!s.isEmpty())
			for (Entry<Object, Object> e: s)
				removeValue(e.getKey());
	}
	
	public void removeSubValues(Object tableKey, Set<Entry<Object, Object>> s) {
		if(subTableManaged.contains(tableKey)&&s!=null&&!s.isEmpty()) {
			for (Entry<Object, Object> e: s)
				removeSubValue(tableKey, e.getKey());
		}
	}
	
	/**
	 * Checks if this context is empty
	 * 
	 * @return true if there are no (name, value) pairs inside the context
	 * <p> false if there is at least a (name, value) pair inside the context
	 */
	public boolean isEmpty() {
		return valuePairs.isEmpty();
	}
	
	/**
	 * Update the state of this context inside the CAMEO platform. A registered application calls
	 * this method when it wants to modify its context inside CAMEO through the AIDL interface
	 * 
	 * @param i The CAMEO AIDL interface
	 * @param appKey The key the application received from CAMEO during the registration procedure
	 * @throws RemoteException
	 */
	public synchronized void update(PlatformInterface i, long appKey) throws RemoteException {
		i.updateApplicationContext(new ACChanges(changes, subTableManaged), appKey);
		changes.clear();
	}
	
	/**
	 * Gets all the (name, value) pairs contained in this context
	 * 
	 * @return The set of (name, value) pairs representing this context
	 */
	public Set<Map.Entry<Object, Object>> getValues() {
		return valuePairs.entrySet();
	}
	
	public Set<Map.Entry<Object, Object>> getSubValues(Object tableKey) {
		if(subTableManaged.contains(tableKey)) {
			Object mapObject=valuePairs.get(tableKey);
			if(mapObject!=null && mapObject instanceof HashMap) {
				HashMap<Object, Object> map=(HashMap<Object, Object>)mapObject;
				return map.entrySet();
			}
		}
		return null;
	}
	
	/**
	 * Gets all the (name, value) pairs contained in this context
	 * 
	 * @return The hash map representing this context
	 */
	public Map<Object, Object> getValuesMap() {
		return valuePairs;
	}
	
	public Map<Object, Object> getSubValuesMap(Object tableKey) {
		if(subTableManaged.contains(tableKey)) {
			Object mapObject=valuePairs.get(tableKey);
			if(mapObject!=null && mapObject instanceof HashMap) {
				HashMap<Object, Object> map=(HashMap<Object, Object>)mapObject;
				return map;
			}
		}
		return null;
	}
	
	/**
	 * Cleans up the internal structures of this context
	 */
	public void clean() {
		valuePairs=new HashMap<Object, Object>();
		subTableManaged=new HashSet<Object>();
		changes=new HashMap<Object, Object>();
	}
	
	public void cleanSubTable(Object tableKey) {
		if(subTableManaged.contains(tableKey)) {
			valuePairs.put(tableKey, new HashMap<Object, Object>());
			changes.put(tableKey, new HashMap<Object, Object>());
		}
	}
	
	@Override
	public String toString() {
		String res="";
		Set<Map.Entry<Object, Object>> s=valuePairs.entrySet();
		for (Map.Entry<Object, Object> e: s) {
			if(subTableManaged.contains(e.getKey())) {
				res+="\t"+e.getKey().toString()+" has SubTable Management\n";
				HashMap<Object, Object> subMap=(HashMap<Object, Object>)e.getValue();
				Set<Map.Entry<Object, Object>> sub=subMap.entrySet();
				for (Map.Entry<Object, Object> subE: sub) {
					res+="\t\t"+subE.getKey().toString()+" = "+((subE.getValue()==null)?"null":subE.getValue().toString())+"\n";
				}
			} else {
				res+="\t"+e.getKey().toString()+" = "+((e.getValue()==null)?"null":e.getValue().toString())+"\n";
			}
		}
		return res;
	}
}
