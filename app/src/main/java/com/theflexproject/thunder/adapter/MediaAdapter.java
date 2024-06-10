
package com.theflexproject.thunder.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theflexproject.thunder.Constants;
import com.theflexproject.thunder.R;
import com.theflexproject.thunder.database.DatabaseClient;
import com.theflexproject.thunder.model.FirebaseManager;
import com.theflexproject.thunder.model.Movie;
import com.theflexproject.thunder.model.MyMedia;
import com.theflexproject.thunder.model.TVShowInfo.Episode;
import com.theflexproject.thunder.model.TVShowInfo.TVShow;
import com.theflexproject.thunder.model.TVShowInfo.TVShowSeasonDetails;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaAdapterHolder> {

    Context context;
    List<MyMedia> mediaList;

    private MediaAdapter.OnItemClickListener listener;

    public MediaAdapter(Context context, List<MyMedia> mediaList, MediaAdapter.OnItemClickListener listener) {
        this.context = context;
        this.mediaList = mediaList;
        this.listener= listener;
    }

    @NonNull
    @Override
    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new MediaAdapterHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MediaAdapterHolder holder, int position) {


        if(mediaList.get(position) instanceof Movie) {
            Movie movie = ((Movie)mediaList.get(position));
           // if(movie.getTitle()==null){
            //    holder.name.setText(movie.getFileName());
            //} else holder.name.setText(movie.getTitle());
            //if(movie.getPlayed()!=0){
               // holder.watched.setVisibility(View.VISIBLE);
            //}
            if(movie.getVote_average()!=0){
                // holder.star.setVisibility(View.VISIBLE);
                holder.textStar.setVisibility(View.VISIBLE);
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                String roundedVoteAverage = decimalFormat.format(movie.getVote_average());
                holder.textStar.setText(roundedVoteAverage);
            }

            if(movie.getPoster_path()!=null){
                Glide.with(context)
                        .load(Constants.TMDB_IMAGE_BASE_URL+movie.getPoster_path())
                        .placeholder(new ColorDrawable(Color.BLACK))
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                        .into(holder.poster);
            }
        }
        
        
        if(mediaList.get(position) instanceof TVShow){
            TVShow tvShow = ((TVShow)mediaList.get(position));
            if(tvShow.getName()!=null){
              //  holder.name.setText(tvShow.getName());
                Glide.with(context)
                        .load(Constants.TMDB_IMAGE_BASE_URL+tvShow.getPoster_path())
                        .placeholder(new ColorDrawable(Color.BLACK))
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                        .into(holder.poster);
            }
            if(tvShow.getVote_average()!=0){
                // holder.star.setVisibility(View.VISIBLE);
                holder.textStar.setVisibility(View.VISIBLE);
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                String roundedVoteAverage = decimalFormat.format(tvShow.getVote_average());
                holder.textStar.setText(roundedVoteAverage);
            }
        }
        
        
        if(mediaList.get(position) instanceof TVShowSeasonDetails){
            TVShowSeasonDetails tvShowSeason = ((TVShowSeasonDetails)mediaList.get(position));
            if(tvShowSeason.getName()!=null){
                //holder.name.setVisibility(View.VISIBLE);
                //holder.name.setText(tvShowSeason.getName());

                holder.season2.setVisibility(View.VISIBLE);
                holder.season2.setText(tvShowSeason.getName());
                String poster_path = tvShowSeason.getPoster_path();
                if(poster_path!=null){
                    Glide.with(context)
                            .load(Constants.TMDB_IMAGE_BASE_URL+poster_path)
                            .placeholder(new ColorDrawable(Color.BLACK))
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                            .into(holder.poster);
                }

            }
        }


        setAnimation(holder.itemView,position);

    }



    @Override
    public int getItemCount() {
        return mediaList.size();
    }



    public class MediaAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name;
        TextView season2;
        ImageView poster;
        TextView movieYear;
        ImageView star;
        TextView textStar;
        TextView watched;



        public MediaAdapterHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameInMediaItem);
            poster= itemView.findViewById(R.id.posterInMediaItem);
            movieYear = itemView.findViewById(R.id.yearInMediaItem);
            star = itemView.findViewById(R.id.starRate);
            textStar = itemView.findViewById(R.id.textStar);
            watched = itemView.findViewById(R.id.markWatchedMedia);
            season2 = itemView.findViewById(R.id.season2);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onClick(v,getAbsoluteAdapterPosition());
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
//package com.theflexproject.thunder.adapter;
//
//import static com.theflexproject.thunder.Constants.TMDB_IMAGE_BASE_URL;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
//import com.bumptech.glide.request.RequestOptions;
//import com.theflexproject.thunder.Constants;
//import com.theflexproject.thunder.R;
//import com.theflexproject.thunder.model.Movie;
//import com.theflexproject.thunder.model.MyMedia;
//import com.theflexproject.thunder.model.TVShowInfo.TVShow;
//import com.theflexproject.thunder.model.TVShowInfo.TVShowSeasonDetails;
//
//import java.util.List;
//
//
//public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaAdapterHolder> {
//
//    Context context;
//    List<MyMedia> mediaList;
//    private OnItemClickListener listener;
//
//    public MediaAdapter(Context context, List<MyMedia> mediaList) {
//        this.context = context;
//        this.mediaList = mediaList;
//        this.listener= listener;
//    }
//
//    @NonNull
//    @Override
//    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
//        return new MediaAdapterHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MediaAdapterHolder holder, int position) {
//        if(mediaList!=null){
//            try {
//                if(((Movie)mediaList.get(position)).getTitle()!=null){
//                    holder.name.setText(((Movie)mediaList.get(position)).getTitle());
//                    Glide.with(context)
//                            .load(TMDB_IMAGE_BASE_URL+((Movie)mediaList.get(position)).getPoster_path())
//                            .placeholder(new ColorDrawable(Color.BLACK))
//                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
//                            .into(holder.poster);
//                }
//                if(((TVShow)mediaList.get(position)).getNumber_of_seasons()>0){
//                    holder.name.setText(((TVShow)mediaList.get(position)).getName());
//                    Glide.with(context)
//                            .load(Constants.TMDB_IMAGE_BASE_URL+((TVShow)mediaList.get(position)).getPoster_path())
//                            .placeholder(new ColorDrawable(Color.BLACK))
//                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
//                            .into(holder.poster);
//                }
//                else {
//                    holder.name.setText(((TVShowSeasonDetails)mediaList.get(position)).getName());
//                    Glide.with(context)
//                            .load(Constants.TMDB_IMAGE_BASE_URL+((TVShowSeasonDetails)mediaList.get(position)).getPoster_path())
//                            .placeholder(new ColorDrawable(Color.BLACK))
//                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
//                            .into(holder.poster);
//                }
//            }catch (ClassCastException e){
//                System.out.println("Exception class cast");
//            }
//
//        }
//
//
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return (mediaList==null)?0:mediaList.size();
//    }
//
//    public class MediaAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//
//        TextView name;
//        ImageView poster;
//        public MediaAdapterHolder(@NonNull View itemView) {
//            super(itemView);
//            name = itemView.findViewById(R.id.mediaName);
//            poster= itemView.findViewById(R.id.mediaPoster);
//            itemView.setOnClickListener(this);
//        }
//
//
//        @Override
//        public void onClick(View v) {
//            listener.onClick(v,getAbsoluteAdapterPosition());
//        }
//
//    }
//    public interface OnItemClickListener {
//        public void onClick(View view, int position);
//    }
//}

