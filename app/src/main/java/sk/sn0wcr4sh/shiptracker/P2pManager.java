package sk.sn0wcr4sh.shiptracker;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.Map;

/**
 * Created by sn0wcr4sh on 9/12/2017.
 *
 */

class P2pManager {

    interface Listener {
        void onGotP2pInfo(WifiP2pInfo info);
    }

    private final String TAG = "ShipTracker";
    private final String SERVICE_NAME = "tracked_ship";

    private Listener mListener;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    P2pManager(Context context, Listener listener) {

        mListener = listener;
        mManager = (WifiP2pManager)context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);

        registerListeners();
    }

    void startServiceDiscovery() {
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Service request added");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Add service request failed: " + reason);
            }
        });
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discover services started");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Discover services failed");
            }
        });
    }

    void getConnectionInfo(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    Log.d(TAG, "Connection info ready");
                    Log.d(TAG, "Owner address: " + info.groupOwnerAddress.getHostAddress());
                    mListener.onGotP2pInfo(info);
                }
            });
        }
    }

    private void registerListeners() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {

                if (fullDomainName.contains(SERVICE_NAME)) {
                    Log.d(TAG, "Service(s) discovered on device " + fullDomainName + ", " +
                            srcDevice.deviceAddress + "/" + srcDevice.deviceName);

                    if (txtRecordMap != null)
                        Log.d(TAG, "Records: " + txtRecordMap.toString());
                    else
                        Log.d(TAG, "No records");

                    connect(srcDevice);
                }
                else {
                    Log.d(TAG, "Not mine service");
                }
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener responseListener =
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(TAG, "Service available: " + instanceName);
                    }
                };

        mManager.setDnsSdResponseListeners(mChannel, responseListener, txtListener);
    }

    private void connect(WifiP2pDevice device) {
        Log.d(TAG, "P2P connect...");

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Connect initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Connect failed: " + reason);
            }
        });
    }
}
