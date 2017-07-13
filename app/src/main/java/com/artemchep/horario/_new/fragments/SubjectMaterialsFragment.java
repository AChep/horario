package com.artemchep.horario._new.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario.database.models.SubjectInfo;

/**
 * @author Artem Chepurnoy
 */
public class SubjectMaterialsFragment extends FragmentSubject {

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_materials, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubject(@Nullable SubjectInfo subject) {

    }

}
