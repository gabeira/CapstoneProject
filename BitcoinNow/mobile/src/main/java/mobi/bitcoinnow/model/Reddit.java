package mobi.bitcoinnow.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by gabrielbernardopereira on 11/4/16.
 */
public class Reddit implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<Reddit> CREATOR = new Creator<Reddit>() {
        @Override
        public Reddit createFromParcel(Parcel in) {
            return new Reddit(in);
        }

        @Override
        public Reddit[] newArray(int size) {
            return new Reddit[size];
        }
    };

    String id;
    String title;
    String author;
    Long created;
    String thumbnail;
    String url;
    Integer numberOfComments;
    String selfText;

    public Reddit() {
    }

    protected Reddit(Parcel in) {
        id = in.readString();
        title = in.readString();
        author = in.readString();
        created = in.readLong();
        thumbnail = in.readString();
        url = in.readString();
        numberOfComments = in.readInt();
        selfText = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getHoursFromCreation() {
        return TimeUnit.HOURS.convert(Calendar.getInstance().getTimeInMillis() - (created * 1000l), TimeUnit.MILLISECONDS);
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(Integer numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelfText() {
        return selfText;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeLong(created);
        dest.writeString(thumbnail);
        dest.writeString(url);
        dest.writeInt(numberOfComments);
        dest.writeString(selfText);
    }
}
