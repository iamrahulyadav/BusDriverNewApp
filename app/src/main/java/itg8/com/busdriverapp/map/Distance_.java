
package itg8.com.busdriverapp.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Distance_ implements Parcelable
{

    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("value")
    @Expose
    private int value;
    public final static Creator<Distance_> CREATOR = new Creator<Distance_>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Distance_ createFromParcel(Parcel in) {
            Distance_ instance = new Distance_();
            instance.text = ((String) in.readValue((String.class.getClassLoader())));
            instance.value = ((int) in.readValue((int.class.getClassLoader())));
            return instance;
        }

        public Distance_[] newArray(int size) {
            return (new Distance_[size]);
        }

    }
    ;

    /**
     * 
     * @return
     *     The text
     */
    public String getText() {
        return text;
    }

    /**
     * 
     * @param text
     *     The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 
     * @return
     *     The value
     */
    public int getValue() {
        return value;
    }

    /**
     * 
     * @param value
     *     The value
     */
    public void setValue(int value) {
        this.value = value;
    }



    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(text);
        dest.writeValue(value);
    }

    public int describeContents() {
        return  0;
    }

}
