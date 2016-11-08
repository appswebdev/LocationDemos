package minhal.tomer.edu.locationdemos;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyCodingService extends IntentService {

    public MyCodingService() {
        //The name of the thread for debugging purposes
        super(MyCodingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LatLng latLng = intent.getParcelableExtra(Constants.EXTRA_LOCATION);
        Geocoder geocoder = new Geocoder(this);


        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    String addressLine = address.getAddressLine(i);
                    builder.append(addressLine).append(" ");
                }

                Log.d(Constants.TAG, builder.toString());
                //Intent resultIntent
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent());
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getLocalizedMessage());
        }


    }
}

