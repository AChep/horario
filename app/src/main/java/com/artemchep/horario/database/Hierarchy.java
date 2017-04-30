package com.artemchep.horario.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Artem Chepurnoy
 */
public abstract class Hierarchy {

    public abstract String path();

    public DatabaseReference ref() {
        return FirebaseDatabase.getInstance().getReference(path());
    }

}
