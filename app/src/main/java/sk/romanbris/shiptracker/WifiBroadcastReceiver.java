package sk.romanbris.shiptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Roman Bris on 9/7/2017.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private MainActivity mActivity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Collection<WifiP2pDevice> mPeers = new ArrayList<>();

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Collection<WifiP2pDevice> refreshedPeers = peers.getDeviceList();
            if (!refreshedPeers.equals(mPeers)) {
                mPeers.clear();
                mPeers.addAll(refreshedPeers);
            }
        }
    };

    public WifiBroadcastReceiver(
            MainActivity activity,
            WifiP2pManager manager,
            WifiP2pManager.Channel channel) {

        mActivity = activity;
        mManager = manager;
        mChannel = channel;
        mActivity.setWifiState(false);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            mActivity.setWifiState(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            return;
        }

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            mManager.requestPeers(mChannel, peerListListener);
            return;
        }

        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
        }
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
//
//        }
    }
}
