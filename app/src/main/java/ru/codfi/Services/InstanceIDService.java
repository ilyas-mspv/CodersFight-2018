package ru.codfi.Services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.codfi.Authenticator.SessionManager;


public class InstanceIDService extends FirebaseMessagingService {

    private static final String TAG = InstanceIDService.class.getSimpleName();
    String refreshedToken;
    SessionManager session;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        session = new SessionManager(getApplicationContext());

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", refreshedToken);
        editor.commit();
        sendRegistrationToServer(refreshedToken);


        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        session.create_token(token);
        //todo send to sever!
    }

}