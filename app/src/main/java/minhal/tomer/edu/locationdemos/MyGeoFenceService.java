package minhal.tomer.edu.locationdemos;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/***
 * This class handles geofence Events...
 */
public class MyGeoFenceService extends IntentService {
    public MyGeoFenceService() {
        super(MyGeoFenceService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(Constants.TAG, intent.toString());

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(Constants.TAG, geofencingEvent.getErrorCode() + " ");
            return;
        }

        Location location = geofencingEvent.getTriggeringLocation();

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(Constants.TAG, "Exit " + location.toString());
                break;
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(Constants.TAG, "Entered " + location.toString());
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(Constants.TAG, "Dwell " + location.toString());
                break;
        }

    }
}
