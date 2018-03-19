package app.jayang.icebr8k.Modle;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 3/18/2018.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText mEditText;
    private boolean isSignUp;
    private Birthdate mBirthdate;
    private  DatePickerDialog mDatePickerDialog;


    private OnDoneListener mOnDoneListener;

    public DatePickerFragment() {
        super();

    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = 1985;
        int month = 0;
        int day = 1;
        mDatePickerDialog = new DatePickerDialog(getActivity(), R.style.CustomDatePickerDialogTheme ,this, year, month, day);
        mDatePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());

        return mDatePickerDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
       signOut();

    }

    public DatePickerDialog getDatePickerDialog() {
        return mDatePickerDialog;
    }

    public TextInputEditText getEditText() {
        return mEditText;
    }

    public void setEditText(TextInputEditText editText) {
        mEditText = editText;
    }

    public boolean isSignUp() {
        return isSignUp;
    }

    public void setSignUp(boolean signUp) {
        isSignUp = signUp;
    }

    public Birthdate getBirthdate() {
        return mBirthdate;
    }
    public Boolean checkAge(Birthdate birthdate){
        int currentYear =  Calendar.getInstance().get(Calendar.YEAR);

        return currentYear -birthdate.getYear()>=13;
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(),"datePicker");
    }

    public void setOnDoneListener(OnDoneListener onDoneListener) {
        mOnDoneListener = onDoneListener;
    }
    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
         mBirthdate = new Birthdate(year,month+1,day);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null && !isSignUp){

            if(!checkAge(mBirthdate)){
                Toast.makeText(getActivity(), getString(R.string.ageError), Toast.LENGTH_LONG).show();
                showDatePickerDialog();
            }else{
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(currentUser.getUid()).child("birthdate");
                mRef.keepSynced(true);
                mRef.setValue(mBirthdate);
                mOnDoneListener.isDone();

            }

        }else{
            if(mEditText instanceof TextInputEditText){
              ((TextInputEditText) mEditText).setText(year+"/"+(++month)+"/"+day);
            }
        }


    }}

