package com.artemchep.horario.interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * @author Artem Chepurnoy
 */
public interface FragmentHost {

    int FLAG_AS_DIALOG = 1;
    int FLAG_AS_SECONDARY = 1 << 1;

    void fragmentShow(@NonNull Class<? extends Fragment> clazz, @Nullable Bundle args, int flags);

    void fragmentFinish(@NonNull Fragment fragment);

}
