package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Class to encapsulate a Weather object.
 *
 * Optional fields include URL, teaser, and Author.
 *
 *
 * @author Conner Canning
 * @version November 2019
 */
public class WeatherObject implements Serializable, Parcelable {

    private final String mTemp;
    private final String mLat;
    private final String mLon;
    private final String mMaxTemp;
    private final String mMinTemp;
    private final String mIcon;
    private final String mDesciption;
    private final String mLocation;
    private final String mDate;
    private final String mTime;

    protected WeatherObject(Parcel in) {
        mTemp = in.readString();
        mLat = in.readString();
        mLon = in.readString();
        mMaxTemp = in.readString();
        mMinTemp = in.readString();
        mIcon = in.readString();
        mDesciption = in.readString();
        mLocation = in.readString();
        mDate = in.readString();
        mTime = in.readString();
    }

    public static final Creator<WeatherObject> CREATOR = new Creator<WeatherObject>() {
        @Override
        public WeatherObject createFromParcel(Parcel in) {
            return new WeatherObject(in);
        }

        @Override
        public WeatherObject[] newArray(int size) {
            return new WeatherObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTemp);
        dest.writeString(mLat);
        dest.writeString(mLon);
        dest.writeString(mMaxTemp);
        dest.writeString(mMinTemp);
        dest.writeString(mIcon);
        dest.writeString(mDesciption);
        dest.writeString(mDate);
        dest.writeString(mTime);
    }

    /**
     * Helper class for building.
     *
     * @author Charles Bryan and Conner Canning
     */
    public static class Builder {

        private final String mTemp;
        private final String mLat;
        private final String mLon;
        private String mMaxTemp ="";
        private String mMinTemp = "";
        private String mIcon = "";
        private String mDesciption = "";
        private String mLocation = "";
        private String mDate ="";
        private String mTime ="";

        /**
         * Constructs a new Current Weather Builder.
         *
         * @param temp the tem the published date of the blog post
         * @param lat the latitude of this weather reading
         * @param lon the longitude of this weather reading
         */
        public Builder(String temp, String lat, String lon) {
            this.mTemp = tempConverter(temp);
            this.mLat = lat;
            this.mLon = lon;
        }

        public Builder addMaxTemp(final String val) {
            mMaxTemp = tempConverter(val);
            return this;
        }

        public Builder addMinTemp(final String val) {
            mMinTemp = tempConverter(val);
            return this;
        }

        public Builder addIcon(final String val) {
            mIcon = val;
            return this;
        }

        public Builder addLocation(final String val) {
            mLocation = val;
            return this;
        }

        public Builder addDescription(final String val) {
            mDesciption = val;
            return this;
        }

        public Builder addDate(final String val) {
            String s = val.substring(5);
            s = s.replace('-', '/');
            mDate = s;
            return this;
        }

        public Builder addTime(final String val) {
            mTime = val;
            return this;
        }

        // Handles possible conversion from doubles in Kelvin to integers in Fahrenheit
        private String tempConverter(String theKelvin) {
            double maybeKelvin = Double.parseDouble(theKelvin);
            if (maybeKelvin > 120) {
                return Integer.toString((int) ((maybeKelvin - 273.15) * 9 / 5.0 + 32));
            }
            return Integer.toString((int) maybeKelvin);
        }

        public WeatherObject build() {
            return new WeatherObject(this);
        }

    }

    private WeatherObject(final Builder builder) {
        this.mTemp = builder.mTemp;
        this.mLat = builder.mLat;
        this.mLon = builder.mLon;
        this.mMaxTemp = builder.mMaxTemp;
        this.mMinTemp = builder.mMinTemp;
        this.mIcon = builder.mIcon;
        this.mDesciption = builder.mDesciption;
        this.mLocation = builder.mLocation;
        this.mDate = builder.mDate;
        this.mTime = builder.mTime;
    }

    public String getTemp() {
        return mTemp;
    }

    public String getLat() {
        return mLat;
    }

    public String getLon() {
        return mLon;
    }

    public String getMaxTemp() {
        return mMaxTemp;
    }

    public String getMinTemp() {
        return mMinTemp;
    }

    public String getIcon() {
        return mIcon;
    }

    public String getDesciption() {
        return mDesciption;
    }

    public String getLocation() { return mLocation; }

    public String getDate() { return mDate; }

    public String getTime() { return mTime; }

}
