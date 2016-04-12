package mobi.bitcoinnow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import mobi.bitcoinnow.model.Reddit;


/**
 * Created by gabrielbernardopereira on 10/4/16.
 */
public class NewsRowAdapter extends RecyclerView.Adapter<NewsRowAdapter.MyViewHolder> {
    private final Context context;
    private List<Reddit> entries = Collections.emptyList();
    private OnRecycleViewItemClickListener onRecycleViewItemClickListener;

    public NewsRowAdapter(List<Reddit> values, Context context) {
        this.context = context;
        this.entries = values;
    }

    public void setEntries(List<Reddit> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public NewsRowAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.news_row_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Reddit currentItem = entries.get(position);
        holder.textViewTitle.setText(currentItem.getTitle());
        holder.textViewAuthor.setText(currentItem.getAuthor());
        holder.textViewDate.setText(currentItem.getHoursFromCreation().toString());
        holder.textViewNumberComments.setText(currentItem.getNumberOfComments().toString());
        final String thumb = currentItem.getThumbnail();
        if (null != thumb && !thumb.isEmpty() && !thumb.equals("self") && !thumb.equals("default")) {
            try {

                Glide.with(holder.imageView.getContext())
                        .load(thumb)
                        .error(R.mipmap.ic_launcher)
                        .fitCenter()
                        .into(holder.imageView);

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setData(Uri.parse(thumb));
                        context.getApplicationContext().startActivity(i);
                    }
                });
            } catch (Exception e) {
            }
        } else {
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    public void setOnRecycleViewItemClickListener(OnRecycleViewItemClickListener onRecycleViewItemClickListener) {
        this.onRecycleViewItemClickListener = onRecycleViewItemClickListener;
    }

    public Reddit getItem(int postion) {
        return entries.get(postion);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View parentView;
        ImageView imageView;
        TextView textViewTitle;
        TextView textViewAuthor;
        TextView textViewDate;
        TextView textViewNumberComments;

        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            v.setClickable(true);
            parentView = v;
            imageView = (ImageView) v.findViewById(R.id.thumbnail);
            textViewTitle = (TextView) v.findViewById(R.id.title);
            textViewAuthor = (TextView) v.findViewById(R.id.author);
            textViewDate = (TextView) v.findViewById(R.id.creation_date);
            textViewNumberComments = (TextView) v.findViewById(R.id.number_comments);
        }

        @Override
        public void onClick(View v) {
            if (onRecycleViewItemClickListener != null) {
                onRecycleViewItemClickListener.onRecycleViewItemClicked(v, getPosition());
                parentView.setSelected(true);
            }
        }
    }

    public interface OnRecycleViewItemClickListener {
        void onRecycleViewItemClicked(View view, int position);
    }
}