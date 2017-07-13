package com.artemchep.horario._new.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTaskInstructionFragment extends FragmentBase implements SubjectTaskFragment.Page {

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_task_instructions, container, false);
    }

    @Override
    public void onFabClick(@NonNull View view) {
    }

    @Override
    public boolean hasFab() {
        return false;
    }

}
