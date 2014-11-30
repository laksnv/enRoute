package app.vlnvv.enRoute;

/**
 * Created by Vicky on 11/27/14.
 */
public class MyMarker {

    private String mLabel;
    private byte[] mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Float mDistance;

    public MyMarker(String label, byte[] icon, Double latitude, Double longitude) {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }

    // Getters and Setters
    public String getmLabel() {
        return this.mLabel;
    }

    public void setmLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public byte[] getmIcon() {
        return this.mIcon;
    }

    public void setmIcon(byte[] icon) {
        this.mIcon = icon;
    }

    public Double getmLatitude() {
        return this.mLatitude;
    }

    public void setmLatitude(Double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude() {
        return this.mLongitude;
    }

    public void setmLongitude(Double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public void setmDistance(Float mDistance) {
        this.mDistance = mDistance;
    }

    public Float getmDistance() {
        return this.mDistance;
    }
}
