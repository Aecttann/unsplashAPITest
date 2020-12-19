package com.aectann.unsplashapitest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.aectann.unsplashapitest.MainActivity;
import com.aectann.unsplashapitest.R;
import com.aectann.unsplashapitest.additional.AppPreferences;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdapterPhotos extends RecyclerView.Adapter<AdapterPhotos.ItemHolder>  {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final static String TAG = "AdapterPhotos";

    List<String> mArrayIDs;
    List<String> mArrayPhotoURLsThumb;
    List<String> mArrayPhotoURLsRaw;
    List<String> mArrayPhotoURLsFull;
    List<String> mArrayPhotoURLsRegular;

    public AdapterPhotos(Context context, List<String> arrayIDs,
                         List<String> arrayPhotoURLsThumb, List<String> arrayPhotoURLsRaw, List<String> arrayPhotoURLsFull, List<String> arrayPhotoURLsRegular) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mArrayIDs = arrayIDs;
        mArrayPhotoURLsThumb = arrayPhotoURLsThumb;
        mArrayPhotoURLsRaw = arrayPhotoURLsRaw;
        mArrayPhotoURLsFull = arrayPhotoURLsFull;
        mArrayPhotoURLsRegular = arrayPhotoURLsRegular;
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View sView = mInflater.inflate(R.layout.recycle_item_image, parent, false);

        return new ItemHolder(sView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NotNull final ItemHolder holder, final int position) {
        String id = mArrayIDs.get(position);
        String photoURLThumb = mArrayPhotoURLsThumb.get(position);
        String photoURLRaw = mArrayPhotoURLsRaw.get(position);
        String photoURLFull = mArrayPhotoURLsFull.get(position);

        Picasso.get().load(photoURLThumb).into(holder.IVRVItem);

        String searchingStatus = AppPreferences.getSearchingStatus(mContext);
        if(position == getItemCount() - 1){
            if(searchingStatus.equals("true")){
                ((MainActivity) mContext).downloadMoreSearchPhotos();
            } else /*if(searchingStatus.equals("false"))*/{
                ((MainActivity) mContext).downloadMoreRandomPhotos();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mArrayIDs.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView IVRVItem;

        ItemHolder(View view) {
            super(view);
            IVRVItem = view.findViewById(R.id.IVRVItem);
            view.setOnClickListener(this);
        }

        public void onClick(View v) {
            String id = mArrayIDs.get(getAdapterPosition());
            String clickedThumbURL = mArrayPhotoURLsThumb.get(getAdapterPosition());
            String clickedFullURL = mArrayPhotoURLsFull.get(getAdapterPosition());
            String clickedRegularURL = mArrayPhotoURLsFull.get(getAdapterPosition());
            ((MainActivity) mContext).itemClickHandler(id, clickedThumbURL, clickedFullURL, clickedRegularURL);
        }
    }
}