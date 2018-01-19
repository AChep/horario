package com.artemchep.horario._new.widgets;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * @author Artem Chepurnoy
 */
public class ContextualToolbar {

    private ContextualToolbar mParent;
    private Callback mCallback;
    private Toolbar mToolbar;

    private Toolbar.OnMenuItemClickListener mMenuItemListener =
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return mCallback.onContextMenuItemClick(ContextualToolbar.this, item);
                }
            };

    private View.OnClickListener mNavigationListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            };

    public void setParent(@Nullable ContextualToolbar ctb) {
        mParent = ctb;
    }

    /**
     * @return {@code true} if contextual toolbar can be shown,
     * {@code false} otherwise.
     */
    public boolean isAllowed() {
        ContextualToolbar ctb = this;
        do {
            if (ctb.hasToolbar()) return true;
            ctb = ctb.mParent;
        } while (ctb != null);
        return false;
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Callback {
        boolean onContextCreated(ContextualToolbar ctb, Menu menu);

        boolean onContextMenuItemClick(ContextualToolbar ctb, MenuItem item);

        boolean onContextFinished(ContextualToolbar ctb);
    }

    public void init(@NonNull Toolbar toolbar) {
        mToolbar = toolbar;
        mToolbar.setTitle("Test");
        mToolbar.setOnMenuItemClickListener(mMenuItemListener);
        mToolbar.setNavigationOnClickListener(mNavigationListener);
    }

    public void start(@NonNull Callback callback) {
        ContextualToolbar ctb = this;
        do {
            if (ctb.hasToolbar()) {
                ctb.mCallback = callback;
                ctb.mCallback.onContextCreated(ctb, ctb.mToolbar.getMenu());
                ctb.mToolbar.setVisibility(View.VISIBLE);
                return;
            }
            ctb = ctb.mParent;
        } while (ctb != null);
    }

    public void finish() {
        ContextualToolbar ctb = this;
        do {
            if (ctb.hasToolbar() && ctb.mCallback != null) {
                ctb.mCallback.onContextFinished(ctb);
                ctb.mCallback = null;
                ctb.mToolbar.setVisibility(View.GONE);
                ctb.mToolbar.getMenu().clear();
                return;
            }
            ctb = ctb.mParent;
        } while (ctb != null);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Nullable
    public Toolbar findToolbar() {
        ContextualToolbar ctb = this;
        do {
            if (ctb.hasToolbar()) {
                return ctb.mToolbar;
            }
            ctb = ctb.mParent;
        } while (ctb != null);
        return null;
    }

    public boolean isActive() {
        return mCallback != null;
    }

    public boolean hasToolbar() {
        return mToolbar != null;
    }

}
