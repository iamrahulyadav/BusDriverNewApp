
package itg8.com.busdriverapp.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class OverviewPolyline implements Parcelable
{

    @SerializedName("points")
    @Expose
    private String points;
    public final static Creator<OverviewPolyline> CREATOR = new Creator<OverviewPolyline>() {


        @SuppressWarnings({
            "unchecked"
        })
        public OverviewPolyline createFromParcel(Parcel in) {
            OverviewPolyline instance = new OverviewPolyline();
            instance.points = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public OverviewPolyline[] newArray(int size) {
            return (new OverviewPolyline[size]);
        }

    }
    ;

    /**
     * 
     * @return
     *     The points
     */
    public String getPoints() {
        return points;
    }

    /**
     * 
     * @param points
     *     The points
     */
    public void setPoints(String points) {
        this.points = points;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(points);
    }

    public int describeContents() {
        return  0;
    }

}
