package com.artemchep.horario.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class GalleryFragment extends FragmentBase {

    public static final String EXTRA_URLS = "extra::urls";

    private List<String> mPhotos;
    private RecyclerViewPager mRecyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        mPhotos = args.getStringArrayList(EXTRA_URLS);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.pager);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(new PagerAdapter(mPhotos));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        final PhotoView photoView;

        public ViewHolder(View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo);
        }

    }

    private static class PagerAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<String> mUrls;

        public PagerAdapter(List<String> urls) {
            mUrls = urls;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.___item_gallery, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String url = mUrls.get(position);
            Context context = holder.itemView.getContext();
            Glide.with(context)
                    .load(url)
                    .into(holder.photoView);
        }

        @Override
        public int getItemCount() {
            return mUrls.size();
        }
    }

}
