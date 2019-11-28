package com.example.androidthings.assistant.Youtube;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.androidthings.assistant.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.List;


public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.MyViewHolder> {
    String TAG = YoutubeAdapter.class.getName();
    List<YoutubePoster> youtubePosters;
    public VideoClickListener mVideoClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView title;
        public TextView youtubeId;
        public YouTubeThumbnailView imageView;
        public MyViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            youtubeId = (TextView) v.findViewById(R.id.youtubeId);
            imageView = (YouTubeThumbnailView) v.findViewById(R.id.imageView);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mVideoClickListener!=null)
                mVideoClickListener.onItemClick(v, (int)v.getTag());
        }
    }

    public YoutubeAdapter(List<YoutubePoster> youtubePosters) {
        this.youtubePosters=youtubePosters;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public YoutubeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.youtube_adapter, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final YoutubePoster youtubePoster=youtubePosters.get(position);
        holder.title.setText(youtubePoster.getTitle());
        holder.youtubeId.setText("["+position+"]"+youtubePoster.getYoutubeId());

        holder.imageView.initialize(YoutubeConstants.API_KEY, new YouTubeThumbnailView
                .OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final
            YouTubeThumbnailLoader youTubeThumbnailLoader) {
                //when initialization is sucess, set the video id to thumbnail to load
                youTubeThumbnailLoader.setVideo(youtubePoster.getYoutubeId());

                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader
                        .OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView,
                                                  String s) {
                        //when thumbnail loaded successfully release the thumbnail loader as we
                        // are showing thumbnail in adapter
                        youTubeThumbnailLoader.release();
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView,
                                                 YouTubeThumbnailLoader.ErrorReason errorReason) {
                        //print or show error when thumbnail load failed
                        Log.e(TAG, "Youtube Thumbnail Error");
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
                                                YouTubeInitializationResult
                                                        youTubeInitializationResult) {
                //print or show error when initialization failed
                Log.e(TAG, "Youtube Initialization Failure");

            }
        });
        holder.itemView.setTag(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return youtubePosters.size();
    }


    public interface VideoClickListener {
        void onItemClick(View v, int postion);
    }

    public void setItems(List<YoutubePoster> youtubePosters) {
        this.youtubePosters = youtubePosters;
    }


    public void setNotifyDataSetChanged(List<YoutubePoster> mData){
        this.youtubePosters=mData;
        notifyDataSetChanged();
    }
}
