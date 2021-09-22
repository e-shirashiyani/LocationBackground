package ir.hrk.mapproject;

import android.location.Location;

public class SendLocationToActivity {
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public SendLocationToActivity(Location location) {
        this.location = location;
    }
}
