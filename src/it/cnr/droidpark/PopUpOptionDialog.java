package it.cnr.droidpark;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PopUpOptionDialog extends DialogFragment{
	
	
	public interface PopUpDialogListener{
		public void gameEvaluation();
		public void preferedGameUpdate();
	}
	
	PopUpDialogListener mListener;
	
	
	
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PopUpDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
		@Override
	public Dialog onCreateDialog(Bundle bundle){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Cosa vuoi fare?")
			   .setItems(R.array.OptionGameList, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					
					
					switch (which){
					case 0 : {
						Intent i = new Intent(getActivity(), SimpleListViewActivity.class);
						startActivity(i);
						break;
					
					}
					case 1 : {mListener.preferedGameUpdate();break;}
					case 2 : {mListener.gameEvaluation(); break;}
					}
					
				}
			});
		return builder.create();
		
	}

}
