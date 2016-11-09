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

public class MyGeoCodingService extends IntentService {

    public MyGeoCodingService() {
        //The Thread name for debugging purposes.
        super(MyGeoCodingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LatLng latLng = intent.getParcelableExtra(Constants.EXTRA_LOCATION);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);


            if (addresses != null && addresses.size() > 0) {
                StringBuilder builder = new StringBuilder();

                Address address = addresses.get(0);
                for (int i = 0; i <=address.getMaxAddressLineIndex() ; i++) {
                    String addressLine = address.getAddressLine(i);
                    builder.append(addressLine).append(" ");
                }

                //Log.d(Constants.TAG, builder.toString());
                //Report the result to the activity
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

                Intent resultIntent = new Intent(Constants.ACTION_LOCATION_RESULT);
                resultIntent.putExtra(Constants.EXTRA_LOCATION, latLng);
                resultIntent.putExtra(Constants.EXTRA_LOCATION_ADDRESS, builder.toString());
                manager.sendBroadcast(resultIntent);

            }


        } catch (IOException e) {
            Log.d(Constants.TAG, e.getLocalizedMessage());
        }

    }
}
