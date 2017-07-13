/*
 * Copyright (C) 2017 XJSHQ@github.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.artemchep.horario.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.utils.ViewUtils;

import static android.animation.ObjectAnimator.ofFloat;

/**
 * @author Artem Chepurnoy
 */
public class ContainersLayout extends FrameLayout {

    public static final int ANIM_DURATION = 250;

    private static final String STATE_SUPER = "state_super";
    private static final String STATE_CONTAINERS_STATE = "state_containers_state";

    private View spaceMaster;
    private View spaceMaster2;
    @Nullable
    private View spaceDetails;
    private ViewGroup frameMaster;
    private ViewGroup frameDetails;
    private CardView mBoundedCardView;
    private MainActivity.State mState = MainActivity.State.SINGLE_COLUMN_MASTER;

    private ColorStateList mCardViewBackgroundColor;
    private float mCardViewElevation;

    private TransitionSet mTransition;

    public ContainersLayout(Context context) {
        super(context);
        init(null);
    }

    public ContainersLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ContainersLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        // Obtain layout resource
        int layout = 0;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContainersLayout);
        try {
            layout = a.getResourceId(R.styleable.ContainersLayout_layout, 0);
        } finally {
            a.recycle();
        }
        if (layout == 0) {
            layout = R.layout.view_main_containers;
        }

        Context context = getContext();
        LayoutInflater.from(context).inflate(layout, this, true);

        mTransition = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new ChangeBounds().setStartDelay(32))
                .addTransition(new Fade(Fade.IN | Fade.OUT));

        mBoundedCardView = (CardView) findViewById(R.id.bounded_card_view);

        if (mBoundedCardView != null) {
            mCardViewBackgroundColor = mBoundedCardView.getCardBackgroundColor();
            mCardViewElevation = mBoundedCardView.getCardElevation();
        }

        spaceMaster = findViewById(R.id.activity_main__space_master);
        spaceMaster2 = findViewById(R.id.activity_main__space_master2);
        spaceDetails = findViewById(R.id.activity_main__space_details);
        frameMaster = (ViewGroup) findViewById(R.id.activity_main__frame_master);
        frameDetails = (ViewGroup) findViewById(R.id.activity_main__frame_details);
    }

    private void singleColumnMaster() {
        if (hasTwoColumns()) {
            if (ViewCompat.isLaidOut(this)) {
                TransitionManager.beginDelayedTransition(this, mTransition);
            }

            spaceMaster.setVisibility(View.GONE);
            spaceMaster2.setVisibility(View.GONE);
            spaceDetails.setVisibility(View.GONE);
            frameDetails.setVisibility(View.GONE);
        } else {
            animateOutFrameDetails();
        }
        frameMaster.setVisibility(View.VISIBLE);
    }

    private void singleColumnDetails() {
        if (hasTwoColumns()) {
            spaceMaster.setVisibility(View.GONE);
            spaceMaster2.setVisibility(View.GONE);
            spaceDetails.setVisibility(View.GONE);
        }
        frameMaster.setVisibility(View.GONE);
        frameDetails.setVisibility(View.VISIBLE);
    }

    private void twoColumnsEmpty() {
        if (hasTwoColumns()) {
            if (ViewCompat.isLaidOut(this)) {
                TransitionManager.beginDelayedTransition(this, mTransition);
            }

            spaceMaster.setVisibility(View.VISIBLE);
            spaceMaster2.setVisibility(View.VISIBLE);
            spaceDetails.setVisibility(View.VISIBLE);
            frameDetails.setVisibility(View.VISIBLE);
        } else {
            animateOutFrameDetails();
        }
        frameMaster.setVisibility(View.VISIBLE);
    }

    private void twoColumnsWithDetails() {
        if (hasTwoColumns()) {
            spaceMaster.setVisibility(View.VISIBLE);
            spaceMaster2.setVisibility(View.VISIBLE);
            spaceDetails.setVisibility(View.VISIBLE);
            frameMaster.setVisibility(View.VISIBLE);
            frameDetails.setVisibility(View.VISIBLE);
        } else {
            animateInFrameDetails();
        }
    }

    private void animateInFrameDetails() {
        frameDetails.setVisibility(View.VISIBLE);
        ViewUtils.onLaidOut(frameDetails, new Runnable() {
            @Override
            public void run() {
                ObjectAnimator alpha = ofFloat(frameDetails, View.ALPHA, 0.4f, 1f);
                ObjectAnimator translate = ofFloat(frameDetails, View.TRANSLATION_Y, frameDetails.getHeight() * 0.3f, 0f);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(alpha, translate);
                set.setDuration(ANIM_DURATION);
                set.setInterpolator(new LinearOutSlowInInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        frameMaster.setVisibility(View.GONE);
                    }
                });
                set.start();
            }
        });
    }

    private void animateOutFrameDetails() {
        ViewUtils.onLaidOut(frameDetails, new Runnable() {
            @Override
            public void run() {
                if (!frameDetails.isShown()) {
                    return;
                }
                ObjectAnimator alpha = ObjectAnimator.ofFloat(frameDetails, View.ALPHA, 1f, 0f);
                ObjectAnimator translate = ofFloat(frameDetails, View.TRANSLATION_Y, 0f, frameDetails.getHeight() * 0.3f);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(alpha, translate);
                set.setDuration(ANIM_DURATION);
                set.setInterpolator(new FastOutLinearInInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        frameDetails.setAlpha(1f);
                        frameDetails.setTranslationY(0);
                        frameDetails.setVisibility(View.GONE);
                    }
                });
                set.start();
            }
        });
    }

    public void setState(@NonNull MainActivity.State state) {
        mState = state;
        switch (state) {
            case SINGLE_COLUMN_MASTER:
                singleColumnMaster();
                break;
            case SINGLE_COLUMN_DETAILS:
                singleColumnDetails();
                break;
            case TWO_COLUMNS_EMPTY:
                twoColumnsEmpty();
                break;
            case TWO_COLUMNS_WITH_DETAILS:
                twoColumnsWithDetails();
                break;
        }
    }

    public void setCardDecorationEnabled(boolean enabled) {
        if (mBoundedCardView == null) {
            return;
        }

        if (enabled) {
            mBoundedCardView.setCardBackgroundColor(mCardViewBackgroundColor);
            mBoundedCardView.setCardElevation(mCardViewElevation);
        } else {
            mBoundedCardView.setCardBackgroundColor(0);
            mBoundedCardView.setCardElevation(0);
        }
    }

    /**
     * @return true if layout has two columns:
     * main and details frames.
     */
    public boolean hasTwoColumns() {
        return spaceMaster != null && spaceDetails != null;
    }

    /**
     * @return true if layout has two columns:
     * main and details frames.
     */
    public boolean hasSingleColumn() {
        return !hasTwoColumns();
    }

    @NonNull
    public MainActivity.State getState() {
        return mState;
    }

    public void clearCustomization() {
        setCardDecorationEnabled(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putString(STATE_CONTAINERS_STATE, mState.name());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            setState(MainActivity.State.valueOf(bundle.getString(STATE_CONTAINERS_STATE)));
            parcelable = bundle.getParcelable(STATE_SUPER);
        }
        super.onRestoreInstanceState(parcelable);
    }

}