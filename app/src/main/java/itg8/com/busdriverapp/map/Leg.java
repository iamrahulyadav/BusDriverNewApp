
package itg8.com.busdriverapp.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Leg implements Parcelable
{

    @SerializedName("distance")
    @Expose

    private Distance distance;
    @SerializedName("duration")
    @Expose

    private Duration duration;
    @SerializedName("end_address")
    @Expose
    private String endAddress;
    @SerializedName("end_location")
    @Expose

    private EndLocation endLocation;
    @SerializedName("start_address")
    @Expose
    private String startAddress;
    @SerializedName("start_location")
    @Expose

    private StartLocation startLocation;
    @SerializedName("steps")
    @Expose

    private List<Step> steps = new ArrayList<Step>();
    @SerializedName("traffic_speed_entry")
    @Expose

    private List<Object> trafficSpeedEntry = new ArrayList<Object>();
    @SerializedName("via_waypoint")
    @Expose

    private List<Object> viaWaypoint = new ArrayList<Object>();
    public final static Creator<Leg> CREATOR = new Creator<Leg>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Leg createFromParcel(Parcel in) {
            Leg instance = new Leg();
            instance.distance = ((Distance) in.readValue((Distance.class.getClassLoader())));
            instance.duration = ((Duration) in.readValue((Duration.class.getClassLoader())));
            instance.endAddress = ((String) in.readValue((String.class.getClassLoader())));
            instance.endLocation = ((EndLocation) in.readValue((EndLocation.class.getClassLoader())));
            instance.startAddress = ((String) in.readValue((String.class.getClassLoader())));
            instance.startLocation = ((StartLocation) in.readValue((StartLocation.class.getClassLoader())));
            in.readList(instance.steps, (Step.class.getClassLoader()));
            in.readList(instance.trafficSpeedEntry, (Object.class.getClassLoader()));
            in.readList(instance.viaWaypoint, (Object.class.getClassLoader()));
            return instance;
        }

        public Leg[] newArray(int size) {
            return (new Leg[size]);
        }

    }
    ;

    /**
     * 
     * @return
     *     The distance
     */
    public Distance getDistance() {
        return distance;
    }

    /**
     * 
     * @param distance
     *     The distance
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    /**
     * 
     * @return
     *     The duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * 
     * @param duration
     *     The duration
     */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * 
     * @return
     *     The endAddress
     */
    public String getEndAddress() {
        return endAddress;
    }

    /**
     * 
     * @param endAddress
     *     The end_address
     */
    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    /**
     * 
     * @return
     *     The endLocation
     */
    public EndLocation getEndLocation() {
        return endLocation;
    }

    /**
     * 
     * @param endLocation
     *     The end_location
     */
    public void setEndLocation(EndLocation endLocation) {
        this.endLocation = endLocation;
    }

    /**
     * 
     * @return
     *     The startAddress
     */
    public String getStartAddress() {
        return startAddress;
    }

    /**
     * 
     * @param startAddress
     *     The start_address
     */
    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    /**
     * 
     * @return
     *     The startLocation
     */
    public StartLocation getStartLocation() {
        return startLocation;
    }

    /**
     * 
     * @param startLocation
     *     The start_location
     */
    public void setStartLocation(StartLocation startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * 
     * @return
     *     The steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    /**
     * 
     * @param steps
     *     The steps
     */
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    /**
     * 
     * @return
     *     The trafficSpeedEntry
     */
    public List<Object> getTrafficSpeedEntry() {
        return trafficSpeedEntry;
    }

    /**
     * 
     * @param trafficSpeedEntry
     *     The traffic_speed_entry
     */
    public void setTrafficSpeedEntry(List<Object> trafficSpeedEntry) {
        this.trafficSpeedEntry = trafficSpeedEntry;
    }

    /**
     * 
     * @return
     *     The viaWaypoint
     */
    public List<Object> getViaWaypoint() {
        return viaWaypoint;
    }

    /**
     * 
     * @param viaWaypoint
     *     The via_waypoint
     */
    public void setViaWaypoint(List<Object> viaWaypoint) {
        this.viaWaypoint = viaWaypoint;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(distance);
        dest.writeValue(duration);
        dest.writeValue(endAddress);
        dest.writeValue(endLocation);
        dest.writeValue(startAddress);
        dest.writeValue(startLocation);
        dest.writeList(steps);
        dest.writeList(trafficSpeedEntry);
        dest.writeList(viaWaypoint);
    }

    public int describeContents() {
        return  0;
    }

}
