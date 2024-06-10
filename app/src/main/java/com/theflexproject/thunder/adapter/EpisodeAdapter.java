package com.theflexproject.thunder.adapter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.theflexproject.thunder.Constants;
import com.theflexproject.thunder.R;
import com.theflexproject.thunder.database.DatabaseClient;
import com.theflexproject.thunder.model.TVShowInfo.Episode;
import com.theflexproject.thunder.model.TVShowInfo.TVShow;
import com.theflexproject.thunder.player.PlayerActivity;
import com.theflexproject.thunder.utils.StringUtils;

import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeAdapterHolder> {

    Context context;
    InterstitialAd mInterstitialAd;
    List<Episode> episodeList;
    TVShow tvShow;
    private EpisodeAdapter.OnItemClickListener listener;

    public EpisodeAdapter(TVShow tvShow, Context context, List<Episode> episodeList, EpisodeAdapter.OnItemClickListener listener) {
        this.context = context;
        this.episodeList = episodeList;
        this.listener= listener;
        this.tvShow = tvShow;

    }

    @NonNull
    @Override
    public EpisodeAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_item, parent, false);
        return new EpisodeAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeAdapterHolder holder, @SuppressLint("RecyclerView") int position) {
        if(episodeList!=null){
            Episode episode = episodeList.get(position);
            if(episode.getEpisode_number()>9 && episode.getEpisode_number()>999) {
                holder.episodeNumber.setText("E" + episode.getEpisode_number());
            }else {
                holder.episodeNumber.setText("E0"+episode.getEpisode_number());
            }
            if(episode.getSeason_number()>9){
                holder.seasonNumber.setText("S"+episode.getSeason_number());
            }else {holder.seasonNumber.setText("S0"+episode.getSeason_number());}
            if(episode.getName()!=null){
                holder.episodeName.setText(episode.getName());
            }
            if(episode.getStill_path()!=null){
                Glide.with(context)
                        .load(Constants.TMDB_IMAGE_BASE_URL+episode.getStill_path())
                        .placeholder(new ColorDrawable(Color.BLACK))
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                        .into(holder.episodeStill);
            }
            if(episode.getOverview()!=null){
                holder.overview.setText(episode.getOverview());
            }
            if(episode.getRuntime()!=0){
                String result = StringUtils.runtimeIntegerToString(episode.getRuntime());
                holder.runtime.setVisibility(View.VISIBLE);
                holder.runtime.setText(result);
            }
            if(episode.getPlayed()!=0){
                holder.watched.setVisibility(View.VISIBLE);
            }

                holder.play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.playEpisode(episode);
                    }
                });

        }




        setAnimation(holder.itemView,position);

    }



    @Override
    public int getItemCount() {
        return episodeList.size();
    }



    public class EpisodeAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        BlurView blurView;
        ViewGroup rootView;
        View decorView;

        TextView episodeName;
        ImageView episodeStill;
        TextView seasonNumber;
        TextView episodeNumber;
        TextView runtime;
        TextView overview;
        Button play;
        TextView watched;

        public EpisodeAdapterHolder(@NonNull View itemView) {
            super(itemView);
            blurView = itemView.findViewById(R.id.blurView3);
            decorView =  ((Activity) context).getWindow().getDecorView();
            rootView = decorView.findViewById(android.R.id.content);


            episodeName = itemView.findViewById(R.id.episodeNameInItem);
            episodeStill = itemView.findViewById(R.id.episodeStill);
            seasonNumber = itemView.findViewById(R.id.seasonNumberInItem);
            episodeNumber = itemView.findViewById(R.id.episodeNumberInItem);
            runtime = itemView.findViewById(R.id.RuntimeInItem);
            overview = itemView.findViewById(R.id.overviewDescInItem);
            watched = itemView.findViewById(R.id.markWatchedEpisode);
            play = itemView.findViewById(R.id.playInEpisodeItem);

            blurBottom();

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v,getAbsoluteAdapterPosition());
        }
        private void loadAds(){
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(context, "ca-app-pub-7142401354409440/5207281951", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            Log.i(TAG, "onAdLoaded");

                            // Set full-screen content callback
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    // Called when a click is recorded for an ad.
                                    Log.d(TAG, "Ad was clicked.");
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    Log.d(TAG, "Ad dismissed fullscreen content.");
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    // Called when ad fails to show.
                                    Log.e(TAG, "Ad failed to show fullscreen content.");
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                    Log.d(TAG, "Ad recorded an impression.");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                    Log.d(TAG, "Ad showed fullscreen content.");
                                }
                            });

                            // Show the ad
                            if (mInterstitialAd != null) {
                                mInterstitialAd.show((Activity) context);
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.d(TAG, loadAdError.toString());
                            mInterstitialAd = null;
                        }
                    });
        }

        private void playEpisode(Episode episode){
            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("Settings" , Context.MODE_PRIVATE);
            boolean savedEXT = sharedPreferences.getBoolean("EXTERNAL_SETTING" , false);

            if (savedEXT) {
                //External Player
                addToLastPlayed(episode.getId());
                Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(episode.getUrlString()));
                intent.setDataAndType(Uri.parse(episode.getUrlString()) , "video/*");
                itemView.getContext().startActivity(intent);
            } else {
                //Play video
                addToLastPlayed(episode.getId());
                Intent in = new Intent(itemView.getContext() , PlayerActivity.class);
                in.putExtra("url" , episode.getUrlString());
                String season = String.valueOf(episode.getSeason_number());
                String epsnum = String.valueOf(episode.getEpisode_number());
                in.putExtra("season" , season);
                in.putExtra("number" , epsnum);
                in.putExtra("episode" , episode.getName());
                in.putExtra("title" , tvShow.getName());
                String tmdbId = String.valueOf(episode.getId());
                in.putExtra("tmdbId", tmdbId);
                itemView.getContext().startActivity(in);
                Toast.makeText(itemView.getContext() , "Play" , Toast.LENGTH_LONG).show();
            }
        }
        private void addToLastPlayed(int id) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseClient.getInstance(itemView.getContext()).getAppDatabase().episodeDao().updatePlayed(id);
                }
            });
            thread.start();
        }

        void blurBottom(){

            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            ((Activity) context).getWindow().setStatusBarColor(Color.TRANSPARENT);
            final float radius = 5f;
            final Drawable windowBackground = ((Activity) context).getWindow().getDecorView().getBackground();

            blurView.setupWith(rootView, new RenderScriptBlur(context))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius);
            blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            blurView.setClipToOutline(true);
        }

    }
    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }


    private void setAnimation(View itemView , int position){
        Animation popIn = AnimationUtils.loadAnimation(context,R.anim.pop_in);
        itemView.startAnimation(popIn);
    }
}
