/*
 * Copyright (C) 2011  Chris Baines
 * Copyright (C) 2011  Rebecca Brannum
 * Copyright (C) 2011  Harry Cutts
 * Copyright (C) 2011  John Preston
 * Copyright (C) 2011  James Robinson
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.todome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

// Service example from http://stackoverflow.com/questions/4300291/example-communication-between-activity-and-service-using-messaging

public class TestTabActivity extends Activity {
    Button btnStart, btnStop, btnBind, btnUnbind, btnUpby1, btnUpby10;
    TextView textStatus, textIntValue, textStrValue;
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private OnClickListener btnStartListener = new OnClickListener() {
        public void onClick(View v){
            startService(new Intent(TestTabActivity.this, ToDoMeService.class));
        }
    };
    private OnClickListener btnStopListener = new OnClickListener() {
        public void onClick(View v){
            doUnbindService();
            stopService(new Intent(TestTabActivity.this, ToDoMeService.class));
        }
    };
    private OnClickListener btnBindListener = new OnClickListener() {
        public void onClick(View v){
            doBindService();
        }
    };
    private OnClickListener btnUnbindListener = new OnClickListener() {
        public void onClick(View v){
            doUnbindService();
        }
    };
    private OnClickListener btnUpby1Listener = new OnClickListener() {
        public void onClick(View v){
            sendMessageToService(1);
        }
    };
    private OnClickListener btnUpby10Listener = new OnClickListener() {
        public void onClick(View v){
            sendMessageToService(10);
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ToDoMeService.MSG_LOCATIONS_UPDATED:
                // Deserialise the string into a LocationDatabase
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, ToDoMeService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            textStatus.setText("Disconnected.");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_tab);
        btnStart	 = (Button)findViewById(R.id.btnStart);
        btnStop		 = (Button)findViewById(R.id.btnStop);
        btnBind		 = (Button)findViewById(R.id.btnBind);
        btnUnbind	 = (Button)findViewById(R.id.btnUnbind);
        textStatus	 = (TextView)findViewById(R.id.textStatus);
        textIntValue = (TextView)findViewById(R.id.textIntValue);
        textStrValue = (TextView)findViewById(R.id.textStrValue);
        btnUpby1	 = (Button)findViewById(R.id.btnUpby1);
        btnUpby10	 = (Button)findViewById(R.id.btnUpby10);

        btnStart.setOnClickListener(btnStartListener);
        btnStop.setOnClickListener(btnStopListener);
        btnBind.setOnClickListener(btnBindListener);
        btnUnbind.setOnClickListener(btnUnbindListener);
        btnUpby1.setOnClickListener(btnUpby1Listener);
        btnUpby10.setOnClickListener(btnUpby10Listener);

        restoreMe(savedInstanceState);

        CheckIfServiceIsRunning();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textStatus", textStatus.getText().toString());
        outState.putString("textIntValue", textIntValue.getText().toString());
        outState.putString("textStrValue", textStrValue.getText().toString());
    }
    private void restoreMe(Bundle state) {
        if (state!=null) {
            textStatus.setText(state.getString("textStatus"));
            textIntValue.setText(state.getString("textIntValue"));
            textStrValue.setText(state.getString("textStrValue"));
        }
    }
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (ToDoMeService.isRunning()) {
            doBindService();
        }
    }

    private void sendMessageToService(int value) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ToDoMeService.MSG_TASKS_UPDATED, value, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }


    void doBindService() {
        getParent().bindService(new Intent(this, ToDoMeService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        textStatus.setText("Binding.");
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ToDoMeService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            textStatus.setText("Unbinding.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("TestTabActivity", "Failed to unbind from the service", t);
        }
    }
}
