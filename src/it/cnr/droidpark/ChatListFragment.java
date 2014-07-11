package it.cnr.droidpark;

import it.cnr.droidpark.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatListFragment extends ListFragment {

	ProximityMainInterface activityInterface;
	LinkedHashMap<Integer, String> chatList;
	HashSet<Integer> followedRooms;
	LinkedHashMapAdapter adapter;
	RoomFragment roomFrag;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		chatList = (LinkedHashMap<Integer, String>) activityInterface.getChatList();
		followedRooms = (HashSet<Integer>)activityInterface.getFollowedRooms();
		
		String prova = "prova";
		chatList.put(prova.hashCode(), prova);
		
		// Populate list with our static array
		adapter = new LinkedHashMapAdapter(chatList, followedRooms);
		setListAdapter(adapter);

		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityInterface = (ProximityMainInterface) activity;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (!followedRooms.contains(adapter.getItemIntKey(position))){
			followedRooms.add(adapter.getItemIntKey(position));
			activityInterface.followRoom(adapter.getItemIntKey(position), adapter.getItem(position).toString());
		}
		String roomName = chatList.get(adapter.getItemIntKey(position));
		Log.i("ChatListFragment",  "Recover room id from click on list item:"+roomName);
	//	((ActivityEntertainment) getActivity()).showRoom(roomName);
	}


	
	
	private class LinkedHashMapAdapter extends BaseAdapter{

		Map<Integer, String> mData;
		ArrayList<Integer> mKeys;
		
		public LinkedHashMapAdapter(Map<Integer, String> inMap, Set<Integer> followedRooms){
			this.mData = inMap;
			mKeys = new ArrayList<Integer>(mData.keySet());
		}
		
		public void addElement(Integer key){
			mKeys.add(key);
		}
		
		public void removeElement(Integer key){
			mKeys.remove(key);
		}
		
		@Override
		public int getCount() {
			return mKeys.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(mKeys.get(position));
		}

		@Override
		public long getItemId(int position) {
			return mKeys.get(position);
		}
		
		public Integer getItemIntKey(int position){
			return mKeys.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_list_item, null);
			}
			((TextView) convertView.findViewById(R.id.textView1)).setText((String)getItem(position));
			if (followedRooms.contains(getItemIntKey(position))){
				((ImageView) convertView.findViewById(R.id.imageView1)).setImageDrawable(getResources().getDrawable(R.drawable.ok));
			}
			else{
				((ImageView) convertView.findViewById(R.id.imageView1)).setImageDrawable(getResources().getDrawable(R.drawable.ko));
			}
			return convertView;
		}
		
	}
	
	public void addRoom(Integer key){
		adapter.addElement(key);
		adapter.notifyDataSetChanged();
	}
	
	public void removeRoom(Integer key){
		adapter.removeElement(key);
		adapter.notifyDataSetChanged();
	}

	}

