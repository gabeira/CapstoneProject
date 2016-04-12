package mobi.bitcoinnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import mobi.bitcoinnow.model.Reddit;

public class NewsDetailActivity extends AppCompatActivity {

    private Reddit r;
    private TextView txtMain, txtTitle;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        Bundle bundle = getIntent().getExtras();
        r = (Reddit) bundle.get("reddit");

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Reddit " + getString(R.string.by) + " " + r.getAuthor());

        if (null != r.getUrl()) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, getString(R.string.action_share) + " " + r.getUrl(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(NewsDetailActivity.this)
                            .setType("text/plain")
                            .setText(r.getUrl())
                            .getIntent(), getString(R.string.action_share)));
                }
            });
        }
        if (null != r.getTitle()) {
            txtTitle = (TextView) findViewById(R.id.txtTitle);
            txtTitle.setText(r.getTitle());
        }

        txtMain = (TextView) findViewById(R.id.txtMain);
        if (null != r.getSelfText()) {
            txtMain.setText(r.getSelfText());
        } else {
            txtMain.setText(r.getHoursFromCreation() +
                    " " + getString(R.string.hours_ago) +
                    " " + getString(R.string.by) +
                    " " + r.getAuthor());
        }

        imageView = (ImageView) findViewById(R.id.backdrop);

        final String thumb = r.getThumbnail();
        if (null != thumb && !thumb.isEmpty() && !thumb.equals("self") && !thumb.equals("default")) {
            try {

                Glide.with(imageView.getContext())
                        .load(thumb)
                        .error(R.mipmap.ic_launcher)
                        .fitCenter()
                        .into(imageView);


            } catch (Exception e) {
            }
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }
}