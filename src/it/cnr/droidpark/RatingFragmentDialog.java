package it.cnr.droidpark;

import it.cnr.droidpark.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;


public class RatingFragmentDialog extends DialogFragment{
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, boolean b);
        public void onDialogNegativeClick(DialogFragment dialog, boolean b);
        public void onDialogNeutralClick(DialogFragment dialog);
    }

	NoticeDialogListener mListener;
	 
		
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        			
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
           
        builder.setTitle("Lascia il tuo giudizio")
        
               .setPositiveButton("Valuta e Commenta", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   mListener.onDialogPositiveClick(RatingFragmentDialog.this,false);
                   }
               })
        
               .setNegativeButton("Valuta", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
               	   mListener.onDialogNegativeClick(RatingFragmentDialog.this,false);
               	   
                   }
               })
        		
               .setNeutralButton("Non adesso", new DialogInterface.OnClickListener() {			
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogNeutralClick(RatingFragmentDialog.this);
			
					}
               });
				
                LayoutInflater inflater = getActivity().getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.rating_fragment_dialog, null));               
                
                
               
        return builder.create();
        
    }

}
