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
package com.artemchep.horario.ui.fragments.master;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.basic.interfaces.IActivityBase;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.basic.utils.LongUtils;
import com.artemchep.horario.R;
import com.artemchep.horario.analytics.AnalyticsEvent;
import com.artemchep.horario.analytics.AnalyticsParam;
import com.artemchep.horario.billing.Bitcoin;
import com.artemchep.horario.billing.SkuUi;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.adapters.SupportMonetaryAdapter;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.artemchep.horario.utils.CoinUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.DiscreteScrollItemTransformer;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.ResponseCodes;
import org.solovyev.android.checkout.Sku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

/**
 * This is named "Support developer".
 *
 * @author Artem Chepurnoy
 */
public class SupportFragment extends MasterFragment implements
        View.OnClickListener,
        MenuItem.OnMenuItemClickListener {

    private static final String TAG = "SupportFragment";

    private static final String URL_PLAY_STORE_WEB = "http://play.google.com/store/apps/details?id=com.artemchep.horario";
    private static final String URL_PLAY_STORE = "market://details?id=com.artemchep.horario";
    private static final String URL_GITHUB = "https://github.com/XJSHQ/horario";
    private static final String URL_CROWDIN = "https://crowdin.com/project/horario";

    private final String SKU_DONATION_WATER = "donation_water";
    private final String SKU_DONATION_COFFEE = "donation_coffee";
    private final String SKU_DONATION_PIZZA = "donation_pizza";
    private final String SKU_DONATION_LAPTOP = "donation_laptop";
    private final String SKU_DONATION_JOURNEY = "donation_journey";

    private static final int SCREEN_LOADING = 1;
    private static final int SCREEN_INVENTORY = 2;

    @NonNull
    private final Map<String, Integer> mMap = new HashMap<>();

    {
        mMap.put(SKU_DONATION_WATER, R.drawable.ic_candycane);
        mMap.put(SKU_DONATION_COFFEE, R.drawable.ic_coffee);
        mMap.put(SKU_DONATION_PIZZA, R.drawable.ic_pizza);
        mMap.put(SKU_DONATION_LAPTOP, R.drawable.ic_laptop);
        mMap.put(SKU_DONATION_JOURNEY, R.drawable.ic_mountain);
    }

    @NonNull
    private final SupportMonetaryAdapter.OnSkuClickListener mSkuClickListener = new SupportMonetaryAdapter.OnSkuClickListener() {
        @Override
        public void onSkuClick(@NonNull View view, @NonNull SkuUi skuUi) {
            SkuUi curSkuUi = mAdapter.getItem(mDiscreteScrollView.getCurrentItem());
            if (curSkuUi == skuUi) {
                // Purchase only if clicked item
                // is selected one.
                purchase(skuUi.sku);
            }
        }
    };

    private DiscreteScrollView mDiscreteScrollView;
    private View mProgressBar;

    private ActivityCheckout mCheckout;
    private SupportMonetaryAdapter mAdapter;
    private Inventory mInventory;

    private FirebaseAnalytics mAnalytics;

    @NonNull
    private final InventoryLoadedListener mInventoryLoadedListener = new InventoryLoadedListener();
    @NonNull
    private final PurchaseListener mPurchaseListener = new PurchaseListener();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IActivityBase) {
            IActivityBase ma = (IActivityBase) context;
            mCheckout = ma.getCheckout();

            if (mCheckout == null) {
                String message = "You must call #requestCheckout() on the activity before!";
                throw new RuntimeException(message);
            }

            return; // don't crash
        }

        throw new RuntimeException("Host activity must be an instance of IActivityBase.class!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInventory = mCheckout.makeInventory();

        // Analytics
        MainActivity activity = (MainActivity) getActivity();
        mAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_donate));
        appBar.getToolbarSpecific().inflateMenu(R.menu.main_support);
        Menu menu = appBar.getToolbarSpecific().getMenu();
        menu.findItem(R.id.action_donate_btc).setOnMenuItemClickListener(this);

        /*
        if (appBar.hasGeneralToolbar()) {
            // Show fancy red heart icon in title on tablets
            Drawable drawable = ResourcesCompat
                    .getDrawable(getResources(), R.drawable.ic_heart_white_24dp, null)
                    .mutate();
            drawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            appBar.getToolbarSpecific().setNavigationIcon(drawable);
        }
        */
    }

    @Override
    protected void setupFab() {
        super.setupFab();
        getMainActivity().mFab.hide();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_support, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.progress_bar);

        mAdapter = new SupportMonetaryAdapter(new ArrayList<SkuUi>(), mSkuClickListener);
        mDiscreteScrollView = (DiscreteScrollView) view.findViewById(R.id.picker);
        mDiscreteScrollView.setItemTransformer(new Transformer());
        mDiscreteScrollView.setAdapter(mAdapter);

        setupNonMonetaryItems(view);
    }

    private void setupNonMonetaryItems(View view) {
        // Load icons
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_star_grey, R.attr.icon_source_fork_grey,
                        R.attr.icon_translate_grey, R.attr.icon_email_grey,
                        R.attr.icon_share_grey});

        class Item {
            @IdRes
            private int viewId;
            @DrawableRes
            private int iconRes;
            @StringRes
            private int titleRes;
            @StringRes
            private int summaryRes;

            private Item(@IdRes int viewId, @DrawableRes int iconRes,
                         @StringRes int titleRes, @StringRes int summaryRes) {
                this.viewId = viewId;
                this.iconRes = iconRes;
                this.titleRes = titleRes;
                this.summaryRes = summaryRes;
            }
        }

        Item[] items = new Item[]{
                new Item(R.id.rate, a.getResourceId(0, 0),
                        R.string.main_support_item_rate_title,
                        R.string.main_support_item_rate_summary),
                new Item(R.id.share, a.getResourceId(4, 0),
                        R.string.main_support_item_share_title,
                        R.string.main_support_item_share_summary),
                new Item(R.id.dev, a.getResourceId(1, 0),
                        R.string.main_support_item_dev_title,
                        R.string.main_support_item_dev_summary),
                new Item(R.id.translate, a.getResourceId(2, 0),
                        R.string.main_support_item_translate_title,
                        R.string.main_support_item_translate_summary),
                new Item(R.id.feedback, a.getResourceId(3, 0),
                        R.string.main_support_item_feedback_title,
                        R.string.main_support_item_feedback_summary),
        };

        a.recycle();

        for (Item item : items) {
            View root = view.findViewById(item.viewId);
            root.setOnClickListener(this);
            ImageView iconView = (ImageView) root.findViewById(R.id.icon);
            TextView titleView = (TextView) root.findViewById(R.id.title);
            TextView summaryView = (TextView) root.findViewById(R.id.summary);
            iconView.setImageResource(item.iconRes);
            titleView.setText(item.titleRes);
            summaryView.setText(item.summaryRes);
        }
    }

    @Override
    public void onClick(View v) {
        Bundle analytics = new Bundle();
        ActivityBase activity = (ActivityBase) getActivity();
        switch (v.getId()) {
            case R.id.feedback:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Support.FEEDBACK);
                DialogHelper.showFeedbackDialog(activity);
                break;
            case R.id.share: {
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Support.SHARE);
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    i.putExtra(Intent.EXTRA_TEXT, "Horario is an open source app for " +
                            "managing your school or university life: " + URL_PLAY_STORE_WEB);
                    startActivity(Intent.createChooser(i, getString(R.string.share_horario_via)));
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception("Failed to share application.", e));
                }
                break;
            }
            case R.id.dev: {
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Support.DEV);
                Uri uri = Uri.parse(URL_GITHUB);
                Intent goToGitHub = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToGitHub);
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception("Failed to open GitHub in browser.", e));
                }
                break;
            }
            case R.id.translate: {
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Support.TRANSLATE);
                Uri uri = Uri.parse(URL_CROWDIN);
                Intent goToCrowdin = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToCrowdin);
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception("Failed to open Crowdin in browser.", e));
                }
                break;
            }
            case R.id.rate: {
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Support.RATE);
                Uri uri = Uri.parse(URL_PLAY_STORE);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(URL_PLAY_STORE_WEB)));
                    } catch (Exception e2) {
                        FirebaseCrash.report(new Exception("Failed to open Play Store in browser.", e2));
                    }
                }
                break;
            }
        }

        mAnalytics.logEvent(AnalyticsEvent.SELECT_SUPPORT, analytics);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_donate_btc: {
                Bitcoin bitcoin = new Bitcoin();
                try {
                    // Try to open directly bitcoin application
                    // if installed.
                    Intent intent = CoinUtils.getPaymentIntent(bitcoin);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    try {
                        // Otherwise try to open web page of the wallet
                        Uri uri = bitcoin.getUriBrowseWallet();
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception e2) {
                        FirebaseCrash.report(new Exception("Failed to open Bitcoin wallet in browser.", e2));
                    }
                }
                break;
            }
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mCheckout.createPurchaseFlow(mPurchaseListener);
        reloadInventory();
    }

    @Override
    public void onStop() {
        mCheckout.destroyPurchaseFlow();
        super.onStop();
    }

    /**
     * Starts a purchase routine.
     * User can cancel purchase process later on.
     */
    private void purchase(final @NonNull Sku sku) {
        Timber.tag(TAG).d("Purchasing " + sku.toString() + "...");

        Bundle analytics = new Bundle();
        analytics.putString(AnalyticsParam.NAME, sku.id.code);
        mAnalytics.logEvent(AnalyticsEvent.SELECT_DONATION, analytics);

        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@NonNull BillingRequests requests) {
                Timber.tag(TAG).d("Purchasing " + sku.toString() + "... request sent!");
                requests.purchase(sku, null, mCheckout.getPurchaseFlow());
            }
        });
    }

    private void reloadInventory() {
        // Set `loading` state.
        refreshUi(SCREEN_LOADING);
        // Reload the inventory.
        mInventory.load(Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.IN_APP,
                        SKU_DONATION_COFFEE,
                        SKU_DONATION_JOURNEY,
                        SKU_DONATION_LAPTOP,
                        SKU_DONATION_PIZZA,
                        SKU_DONATION_WATER), mInventoryLoadedListener);
    }

    private void refreshUi(int visibility) {
        mProgressBar.setVisibility(visibility == SCREEN_LOADING ? View.VISIBLE : View.GONE);
        mDiscreteScrollView.setVisibility(visibility == SCREEN_INVENTORY ? View.VISIBLE : View.GONE);
    }

    /**
     * @author Artem Chepurnoy
     */
    private final class PurchaseListener implements RequestListener<Purchase> {

        @Override
        public void onSuccess(@NonNull Purchase purchase) {
            onPurchased(false);
        }

        @Override
        public void onError(int response, @NonNull Exception e) {
            switch (response) {
                case ResponseCodes.ITEM_ALREADY_OWNED:
                    onPurchased(true);
                    break;
                default:
                    Timber.tag(TAG).w("Purchase listener error: " +
                            "response=" + response + "; " +
                            "stack trace below:");
                    e.printStackTrace();
            }
        }

        private void onPurchased(boolean alreadyOwned) {
            Toasty.success(getActivity(), getString(R.string.main_support_donation_thanks)).show();

            if (alreadyOwned) {
                // Nothing has changed, so we don't need
                // to reload the inventory.
                return;
            }

            reloadInventory();
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    private class InventoryLoadedListener implements Inventory.Callback {

        @Override
        public void onLoaded(@NonNull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            final List<SkuUi> skuList = new ArrayList<>();

            if (product.supported) {
                for (Sku sku : product.getSkus()) {
                    final Purchase purchase = product.getPurchaseInState(sku, Purchase.State.PURCHASED);
                    final SkuUi skuUi = new SkuUi(sku, mMap.get(sku.id.code), purchase != null);
                    skuList.add(skuUi);
                }
            }

            Collections.sort(skuList, new Comparator<SkuUi>() {
                @Override
                public int compare(SkuUi o1, SkuUi o2) {
                    return LongUtils.compare(
                            o1.sku.detailedPrice.amount,
                            o2.sku.detailedPrice.amount);
                }
            });

            refreshUi(SCREEN_INVENTORY);
            setInventory(skuList);
        }
    }

    private void setInventory(@NonNull List<SkuUi> skuList) {
        mAdapter.getList().clear();
        mAdapter.getList().addAll(skuList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * @author Artem Chepurnoy
     */
    private static class Transformer implements DiscreteScrollItemTransformer {

        @Override
        public void transformItem(View item, float position) {
            float closenessToCenter = 1f - Math.abs(position);
            float scale = 0.7f + 0.3f * closenessToCenter;
            item.setAlpha(0.6f + 0.4f * closenessToCenter);
            item.setScaleX(scale);
            item.setScaleY(scale);
        }

    }

}
