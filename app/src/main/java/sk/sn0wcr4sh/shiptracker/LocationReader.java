package sk.sn0wcr4sh.shiptracker;

import android.util.Log;

/**
 * Created by sn0wcr4sh on 9/18/2017.
 *
 */

public class LocationReader {

    interface Listener {
        void onLocationRead(double latitude, double longitude);
    }

    private Listener mListener;
    private String mData;

    public LocationReader(Listener listener) {
        mListener = listener;
        mData = "";
    }

    public void processData(String data) {
        mData += data;

        if (mData.contains("</loc_update>")) {
            Log.d(Constant.TAG, "Received: " + data);

            int latStart = mData.indexOf("<lat>") + 5;
            int latEnd = mData.indexOf("</lat>");

            int lonStart = mData.indexOf("<lon>") + 5;
            int lonEnd = mData.indexOf("</lon>");

            String latString = mData.substring(latStart, latEnd);
            String lonString = mData.substring(lonStart, lonEnd);

            mData = "";

            try {
                mListener.onLocationRead(Double.parseDouble(latString), Double.parseDouble(lonString));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
