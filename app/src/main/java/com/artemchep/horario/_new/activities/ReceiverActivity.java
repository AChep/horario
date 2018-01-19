package com.artemchep.horario._new.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.artemchep.horario.ui.activities.ActivityHorario;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @author Artem Chepurnoy
 */
public class ReceiverActivity extends ActivityHorario {

    private static final String PATH_HORARIO_SUBJECT = "/horario/subject/";

    private FirebaseAnalytics mAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle analytics = new Bundle();
        Intent intent = getIntent();
        Uri data;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (intent != null
                && (data = intent.getData()) != null
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && user != null) {
            String path = data.getPath();
            if (path.startsWith(PATH_HORARIO_SUBJECT)) {
                /*
                ShareHelper helper = new ShareHelper();
                helper.decode(path.substring(PATH_HORARIO_SUBJECT.length()));

                Toasty.info(this, path.substring(PATH_HORARIO_SUBJECT.length()), Toast.LENGTH_LONG).show();

                if (!helper.isCorrect()) {
                    return;
                }

                Bundle args = new Bundle();
                args.putString(EXTRA_USER_ID, user.getUid());
                args.putString(EXTRA_SUBJECT_ID, helper.getArgs()[0]);

                Intent i = DialogActivity.makeFor(this, SubjectPreviewFragment.class, args);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                finish();
                */
            }
        }
    }


}
