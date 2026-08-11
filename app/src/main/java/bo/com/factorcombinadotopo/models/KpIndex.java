package bo.com.factorcombinadotopo.models;

import com.google.gson.annotations.SerializedName;

public class KpIndex {
    @SerializedName("time_tag")
    public String timeTag;

    @SerializedName("Kp")
    public double kp;

    @SerializedName("a_running")
    public int aRunning;

    @SerializedName("station_count")
    public int stationCount;
}
