package mobi.bitcoinnow.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Venue implements Parcelable {

    private Integer id;
    private String name;
    private Integer created_on;
    private String latitude;
    private String longitude;
    private String category;

    public Venue() {
    }

    public static final String TABLE_NAME = "venues";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CREATED = "created_on";
    public static final String COLUMN_LATITUDE = "lat";
    public static final String COLUMN_LONGITUDE = "lon";
    public static final String COLUMN_CATEGORY = "category";

    protected Venue(Parcel in) {
        id = in.readInt();
        name = in.readString();
        created_on = in.readInt();
        latitude = in.readString();
        longitude = in.readString();
        category = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(created_on);
        dest.writeValue(latitude);
        dest.writeString(longitude);
        dest.writeString(category);
    }

    @SuppressWarnings("unused")
    public static final Creator<Venue> CREATOR = new Creator<Venue>() {
        @Override
        public Venue createFromParcel(Parcel in) {
            return new Venue(in);
        }

        @Override
        public Venue[] newArray(int size) {
            return new Venue[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Integer created_on) {
        this.created_on = created_on;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
