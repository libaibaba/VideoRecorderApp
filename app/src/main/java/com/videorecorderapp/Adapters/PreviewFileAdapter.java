package com.videorecorderapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.videorecorderapp.Activities.PreviewActivity;
import com.videorecorderapp.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 26/8/17.
 */

public class PreviewFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<HashMap<String, Object>> data;


    public PreviewFileAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_file_adapter, parent, false);
        return new ViewHolderMain(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        ViewHolderMain mHolder = (ViewHolderMain) holder;

        if (data.get(position) != null) {

            HashMap<String, Object> map = data.get(position);

            final int type = (int)map.get("type");
            final String path = "" + map.get("path");

            Glide.with(context).load(path).into(mHolder.pic);
            if(type == 1)
            {
               mHolder.playIcon.setVisibility(View.VISIBLE);
            }
            else
            {
                mHolder.playIcon.setVisibility(View.GONE);
            }

            mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(context, PreviewActivity.class);
                    i.putExtra("videoPath", path);
                    i.putExtra("type", type);
                    context.startActivity(i);
                    ((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolderMain extends RecyclerView.ViewHolder {

        @BindView(R.id.pic)
        ImageView pic;

        @BindView(R.id.play_icon)
        ImageView playIcon;


        public ViewHolderMain(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
