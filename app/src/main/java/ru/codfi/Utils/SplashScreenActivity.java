package ru.codfi.Utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;

import java.util.HashMap;

import ru.codfi.Activities.ProfileActivity;
import ru.codfi.AppController;
import ru.codfi.Authenticator.LoginActivity;
import ru.codfi.Authenticator.SessionManager;
import ru.codfi.BaseAppCompatActivity;
import ru.codfi.Constants;
import ru.codfi.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Ilyas on 22-Apr-17.
 */

public class SplashScreenActivity extends BaseAppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;
    SessionManager session;
    HashMap<String,String> user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        session = new SessionManager(getApplicationContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Tasks().execute();
            }
        }, SPLASH_TIME_OUT);

    }


    class Tasks extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            session.checkLogin();

            if(isNetworkAvailable()) {
                user = session.getUserDetails();
                if (session.isLoggedIn()) {
                    String id = user.get(SessionManager.KEY_ID);
                    String token = FirebaseInstanceId.getInstance().getToken();
                    set_token(id, token);
                    session.create_token(token);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(session.isLoggedIn()){
                Intent i = new Intent(SplashScreenActivity.this, ProfileActivity.class);
                startActivity(i);

                finish();
            }else{
                Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(i);

                finish();
            }

        }
    }


    public void set_token(String id,String token){

        AppController.getApi().set_token(Constants.Methods.Version.VERSION,Constants.Methods.User.SET_TOKEN,id,token).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
}


