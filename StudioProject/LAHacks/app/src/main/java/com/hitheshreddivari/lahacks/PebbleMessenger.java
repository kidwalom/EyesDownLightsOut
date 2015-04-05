package com.hitheshreddivari.lahacks;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by hitheshaum on 4/4/15.
 */
public class PebbleMessenger {
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("3afbfb13-0beb-47ad-ae8f-6f665e5d089c");
    public final static int PEBBLE_NOT_CONNECTED = 1;
    private Context mContext;
    public PebbleMessenger(Context mContext){
        this.mContext=mContext;
        PebbleKit.registerReceivedAckHandler(mContext, new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveAck(Context context, int transactionId) {
            }

        });

        PebbleKit.registerReceivedNackHandler(mContext, new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i("nak", "Received nack for transaction ");
            }

        });
    }
    public int sendAlert(){
        if(!PebbleKit.isWatchConnected(mContext)){
            return 1;
        }
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(0,(byte)1);
        PebbleKit.sendDataToPebble(mContext, PEBBLE_APP_UUID, data);
        return 0;
    }
}
