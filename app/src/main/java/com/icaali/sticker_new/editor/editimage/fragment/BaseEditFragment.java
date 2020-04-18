package com.icaali.sticker_new.editor.editimage.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.icaali.sticker_new.editor.editimage.EditImageActivity;

/**
 * Created by icaali on 27/11/2017.
 */

public abstract class BaseEditFragment extends Fragment {
    protected EditImageActivity activity;

    protected EditImageActivity ensureEditActivity(){
        if(activity==null){
            activity = (EditImageActivity)getActivity();
        }
        return activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ensureEditActivity();
    }

    public abstract void onShow();

    public abstract void backToMain();
}//end class
