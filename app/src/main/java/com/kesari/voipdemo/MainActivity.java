package com.kesari.voipdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipErrorCode;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.ParseException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public SipManager mSipManager = null;
    public SipProfile mSipProfile = null;
    //public SipProfile mSipPeerProfile = null;
    TextView Call,Disconnect,Answer,End;
    public IncomingCallReceiver callReceiver;
    SipAudioCall sipAudioCall;
    SipRegistrationListener sipRegistrationListener;

    SipAudioCall incomingCall = null;
    SipAudioCall.Listener listener;
    MediaPlayer mPlayer;
    //SipSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        Call = (TextView) findViewById(R.id.Call);
        Disconnect = (TextView) findViewById(R.id.Disconnect);
        Answer = (TextView) findViewById(R.id.Answer);
        End = (TextView) findViewById(R.id.End);

        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(this);
        }

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                SipProfile.Builder builder = null;
                //SipProfile.Builder builderPeer = null;
                try {

                    builder = new SipProfile.Builder("4185", "172.16.1.150");
                    builder.setPassword("Password10");
                    builder.setOutboundProxy("172.16.1.150");
                    mSipProfile = builder.build();

                    /*builderPeer = new SipProfile.Builder("4185", "172.16.1.150");
                    builderPeer.setPassword("Password10");
                    builderPeer.setOutboundProxy("172.16.1.150");
                    mSipPeerProfile = builderPeer.build();*/
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {

                    Intent intent = new Intent();
                    intent.setAction("android.SipDemo.INCOMING_CALL");
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, Intent.FILL_IN_DATA);
                    mSipManager.open(mSipProfile, pendingIntent, null);

                    sipRegistrationListener = new SipRegistrationListener() {

                        public void onRegistering(String localProfileUri) {
                            Log.i(localProfileUri,"Registering with SIP Server...");
                        }

                        public void onRegistrationDone(String localProfileUri, long expiryTime) {
                            Log.i(localProfileUri,"Ready");
                        }

                        public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                         String errorMessage) {
                            Log.i("onRegistrationFailed","onError");
                            Log.i("errorCode", String.valueOf(errorCode));
                            Log.i("errorMessage",errorMessage);

                            if(errorCode == SipErrorCode.IN_PROGRESS) {
                                closeLocalProfile();
                                try {
                                    mSipManager.open(mSipProfile, pendingIntent, null);
                                } catch (SipException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };

                    mSipManager.setRegistrationListener(mSipProfile.getUriString(), sipRegistrationListener);

                } catch (SipException e) {
                    e.printStackTrace();
                }

                mPlayer = MediaPlayer.create(MainActivity.this, R.raw.ios_ringtone);
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setLooping(true);

                listener = new SipAudioCall.Listener() {

                    @Override
                    public void onCallEstablished(SipAudioCall call) {
                        super.onChanged(call);
                        Log.i("Status","onCallEstablished");
                        call.startAudio();
                        call.setSpeakerMode(true);
                        //call.toggleMute();

                       // sipAudioCall =call;
                    }

                    @Override
                    public void onCallEnded(SipAudioCall call) {
                        super.onChanged(call);
                        // Do something.
                        Log.i("Status","onCallEnded");
                       // sipAudioCall =call;
                    }

                    @Override
                    public void onRinging(SipAudioCall call, SipProfile caller) {
                        super.onChanged(call);
                        try {
                            Log.i("Status","onRinging");
                            Log.i("caller",caller.getDisplayName());
                           // sipAudioCall =call;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCalling(SipAudioCall call) {
                        super.onChanged(call);
                        Log.i("Status","onCalling");
                      //  sipAudioCall =call;
                    }

                    @Override
                    public void onRingingBack(SipAudioCall call) {
                        super.onChanged(call);
                        Log.i("Status","onRingingBack");
                       // sipAudioCall =call;
                    }

                    @Override
                    public void onChanged(SipAudioCall call) {
                        super.onChanged(call);
                        Log.i("Status","onChanged");
                    }

                    @Override
                    public void onCallBusy(SipAudioCall call) {
                        super.onCallBusy(call);
                        Log.i("Status","onCallBusy");
                    }

                    @Override
                    public void onCallHeld(SipAudioCall call) {
                        super.onCallHeld(call);
                        Log.i("Status","onCallHeld");
                    }

                    @Override
                    public void onReadyToCall(SipAudioCall call) {
                        super.onReadyToCall(call);
                        Log.i("Status","onReadyToCall");
                    }

                    @Override
                    public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                        super.onError(call, errorCode, errorMessage);
                        Log.i("Status","onError");
                        Log.i("errorCode", String.valueOf(errorCode));
                        Log.i("errorMessage",errorMessage);
                    }
                };


                Call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.i("Status","CallClicked");
                            sipAudioCall =  mSipManager.makeAudioCall(mSipProfile.getUriString(), "4178@172.16.1.150", listener, 30);

//                            sipAudioCall = myMakeAudioCall(MainActivity.this,mSipProfile,mSipPeerProfile,30);
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Disconnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.i("Status","DisconnectCall");
                            sipAudioCall.endCall();
                            if(sipAudioCall != null)
                            {
                                if(sipAudioCall.isInCall() || sipAudioCall.isOnHold())
                                {

                                }
                            }

                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                });




                End.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            if(sipAudioCall != null)
                            {
                                if(sipAudioCall.isInCall() || sipAudioCall.isOnHold())
                                {
                                    Log.i("Status","EndCall");
                                    sipAudioCall.endCall();
                                }
                            }

                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }


        };

        TedPermission.with(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(android.Manifest.permission.USE_SIP,android.Manifest.permission.CALL_PHONE,android.Manifest.permission.RECORD_AUDIO,android.Manifest.permission.READ_CONTACTS,android.Manifest.permission.READ_PHONE_STATE)
                .check();



    }

  /*  private SipAudioCall myMakeAudioCall(Context context, SipProfile sipProfile, SipProfile peerProfile, int timeout) throws SipException{

        SipAudioCall.Listener l = new SipAudioCall.Listener(){
            @Override
            public void onCallEstablished(SipAudioCall call) {
            }
            //add more methods if you want to
        };

        SipAudioCall testCall = new SipAudioCall(context,sipProfile);
        testCall.setListener(l);

        SipSession.Listener sessionListener = new SipSession.Listener(){
            @Override
            public void onCalling(SipSession session) {
                String callId = session.getCallId();
                Log.d("SipSession", "onCalling. call ID: " + callId);
            }

            @Override
            public void onCallBusy(SipSession session) {
                super.onCallBusy(session);
                Log.d("SipSession", "onCallBusy");
            }

            @Override
            public void onCallChangeFailed(SipSession session, int errorCode, String errorMessage) {
                super.onCallChangeFailed(session, errorCode, errorMessage);
                Log.d("SipSession", "onCallChangeFailed");
            }

            @Override
            public void onCallEnded(SipSession session) {
                super.onCallEnded(session);
                Log.d("SipSession", "onCallEnded");
            }

            @Override
            public void onCallEstablished(SipSession session, String sessionDescription) {
                super.onCallEstablished(session, sessionDescription);
                Log.d("SipSession", "onCallEstablished");
            }

            @Override
            public void onError(SipSession session, int errorCode, String errorMessage) {
                super.onError(session, errorCode, errorMessage);
                Log.d("SipSession", "onError");
                Log.i("errorCode", String.valueOf(errorCode));
                Log.i("errorMessage",errorMessage);
            }

            @Override
            public void onRegistering(SipSession session) {
                super.onRegistering(session);
                Log.d("SipSession", "onRegistering");
            }

            @Override
            public void onRegistrationDone(SipSession session, int duration) {
                super.onRegistrationDone(session, duration);
                Log.d("SipSession", "onRegistrationDone");
            }

            @Override
            public void onRegistrationFailed(SipSession session, int errorCode, String errorMessage) {
                super.onRegistrationFailed(session, errorCode, errorMessage);
                Log.d("SipSession", "onRegistrationFailed");
            }

            @Override
            public void onRinging(SipSession session, SipProfile caller, String sessionDescription) {
                super.onRinging(session, caller, sessionDescription);
                Log.d("SipSession", "onRinging");
            }

            @Override
            public void onRegistrationTimeout(SipSession session) {
                super.onRegistrationTimeout(session);
                Log.d("SipSession", "onRegistrationTimeout");
            }

            @Override
            public void onRingingBack(SipSession session) {
                super.onRingingBack(session);
                Log.d("SipSession", "onRingingBack");
            }
        };

        SipSession ss = mSipManager.createSipSession(sipProfile, sessionListener);
        if(ss == null){
            throw new SipException("Failed to create SipSession; Network available?");
        }
        testCall.makeCall(peerProfile, ss, timeout);
        Log.d("SipAudioCall","iD: " + ss.getCallId());
        return testCall;
    }

*/
    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeLocalProfile();
        unregisterReceiver(callReceiver);
    }

    public void closeLocalProfile() {
        if (mSipManager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                mSipManager.close(mSipProfile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity", "Failed to close local profile.", ee);
        }
    }

    public class IncomingCallReceiver extends BroadcastReceiver {
        /**
         * Processes the incoming call, answers it, and hands it over to the
         * WalkieTalkieActivity.
         * @param context The context under which the receiver is running.
         * @param intent The intent being received.
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {

            try {
                final SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                    @Override
                    public void onCallEstablished(SipAudioCall call) {
                        Log.i("Status","onCallEstablished");
                        mPlayer.stop();
                        call.startAudio();
                        call.setSpeakerMode(true);
                        //call.toggleMute();

                        sipAudioCall =call;
                    }

                    @Override
                    public void onCallEnded(SipAudioCall call) {
                        // Do something.
                        Log.i("Status","onCallEnded");
                        sipAudioCall =call;
                    }

                    @Override
                    public void onRinging(SipAudioCall call, SipProfile caller) {
                        try {
                            Log.i("Status","onRinging");
                            sipAudioCall =call;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCalling(SipAudioCall call) {
                        Log.i("Status","onCalling");
                        sipAudioCall =call;
                    }

                    @Override
                    public void onRingingBack(SipAudioCall call) {
                        Log.i("Status","onRingingBack");
                        sipAudioCall =call;
                    }
                };

                mPlayer.start();

                Answer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity wtActivity = (MainActivity) context;
                        try {
                            incomingCall = wtActivity.mSipManager.takeAudioCall(intent, listener);
                            Log.i("Status","takeAudioCall");
                            incomingCall.answerCall(30);
                            incomingCall.startAudio();
                            incomingCall.setSpeakerMode(true);
                            wtActivity.sipAudioCall = incomingCall;
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                if (incomingCall != null) {
                    incomingCall.close();
                }
            }
        }
    }

}


