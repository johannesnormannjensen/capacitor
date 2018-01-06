package com.avocadojs.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.avocadojs.JSObject;
import com.avocadojs.NativePlugin;
import com.avocadojs.Plugin;
import com.avocadojs.PluginCall;
import com.avocadojs.PluginMethod;

/**
 * Simple Network status plugin.
 *
 * https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
 * https://developer.android.com/training/basics/network-ops/managing.html
 */
@NativePlugin()
public class Network extends Plugin {
  public static final String NETWORK_CHANGE_EVENT = "networkStatusChange";

  /**
   * Monitor for network status changes and fire our event.
   */
  @SuppressWarnings("MissingPermission")
  public void load() {
    final ConnectivityManager cm =
        (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    getActivity().registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        notifyListeners(NETWORK_CHANGE_EVENT, getStatusJSObject(cm.getActiveNetworkInfo()));
      }
    }, filter);
  }

  /**
   * Get current network status information
   * @param call
   */
  @SuppressWarnings("MissingPermission")
  @PluginMethod()
  public void getStatus(PluginCall call) {
    ConnectivityManager cm =
        (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

    call.success(getStatusJSObject(activeNetwork));
  }

  /**
   * Transform a NetworkInfo object into our JSObject for returning to client
   * @param info
   * @return
   */
  private JSObject getStatusJSObject(NetworkInfo info) {
    JSObject ret = new JSObject();
    if (info == null) {
      ret.put("connected", false);
      ret.put("connectionType", "none");
    } else {
      ret.put("connected", info.isConnected());
      ret.put("connectionType", getNormalizedTypeName(info));
    }
    return ret;
  }

  /**
   * Convert the Android-specific naming for netowrk types into our cross-platform type
   * @param info
   * @return
   */
  private String getNormalizedTypeName(NetworkInfo info) {
    String typeName = info.getTypeName();
    if (typeName.equals("WIFI")) {
      return "wifi";
    }
    if (typeName.equals("MOBILE")) {
      return "cellular";
    }
    return "none";
  }
}
