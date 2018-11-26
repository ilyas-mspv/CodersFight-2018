package ru.codfi.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import ru.codfi.AppController;
import ru.codfi.Authenticator.SessionManager;
import ru.codfi.BaseAppCompatActivity;
import ru.codfi.Constants;
import ru.codfi.Fragments.AnswerResultFragment;
import ru.codfi.Fragments.QuestionFragment;
import ru.codfi.Models.Game.Game;
import ru.codfi.Models.Game.Question;
import ru.codfi.Models.Game.Zones;
import ru.codfi.R;
import ru.codfi.Services.Config;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.codfi.Utils.MapItemView;

import static ru.codfi.Constants.DEBUG;
import static ru.codfi.Utils.MapItemView.INVADED;
import static ru.codfi.Utils.MapItemView.NOT_INVADED;
import static ru.codfi.Utils.MapItemView.NO_COLOR;

public class MainActivity extends BaseAppCompatActivity {

    public static int GREEN;
    public static int RED;

    Unbinder unbinder;

    @BindView(R.id.user_id1) TextView textView_user_id1;
    @BindView(R.id.user_id2) TextView textView_user_id2;
    @BindView(R.id.logs_main) TextView textview_logs;
    @BindView(R.id.log_scroll) ScrollView log_scroll;
    @BindView(R.id.r1) MapItemView r1;
    @BindView(R.id.r2) MapItemView r2;
    @BindView(R.id.r3) MapItemView r3;
    @BindView(R.id.r4) MapItemView r4;
    @BindView(R.id.r5) MapItemView r5;

    private static final String TAG = MainActivity.class.getSimpleName();
    SessionManager session;
    int account_status = 0;
    FragmentManager fm = getSupportFragmentManager();
    Bundle bundle = new Bundle();
    AlertDialog.Builder wait_dialog; AlertDialog d;
    boolean doublePress = false;

    String log;

    //users' data
    String  game_id,name,
            first_player,
            user_id1, user_id2,
            start_zone1, start_zone2,
            username1, username2;
    int move_user_id;

    int stepsCounter = 0;
    int[] areas = new int[5];
    int zone_1, zone_2;
    boolean is_success_answer = false;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    MapItemView[] zonesImageView;


    int[] zonesDrawables = new int[]{
            R.drawable.map_area_1_normal,
            R.drawable.map_area_2_normal,
            R.drawable.map_area_3_normal,
            R.drawable.map_area_4_normal,
            R.drawable.map_area_5_normal};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        unbinder = ButterKnife.bind(this);

        GREEN = getResources().getColor(R.color.green);
        RED = getResources().getColor(R.color.red);

        log = "Welcome to the game!";

        zonesImageView = new MapItemView[] {r1, r2, r3, r4, r5};

        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        name = user.get(SessionManager.KEY_NAME);
        account_status = Integer.parseInt(user.get(SessionManager.KEY_ACCOUNT_STATUS));

        notification_receiver();
        GetSetData();
    }



    private void notification_receiver() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String tag = intent.getStringExtra("tag");
                    switch (tag) {
                        case "question":
                            int user_id = intent.getIntExtra("user_id", 0);
                            int question_id = intent.getIntExtra("question_id", 0);
                            String question_type = intent.getStringExtra("question_type");
                            String question_answer = intent.getStringExtra("question");
                            String answer1_question = intent.getStringExtra("answer1");
                            String answer2_question = intent.getStringExtra("answer2");
                            String answer3_question = intent.getStringExtra("answer3");
                            String answer4_question = intent.getStringExtra("answer4");
                            int time = intent.getIntExtra("time", 0);
                            int answer_true = intent.getIntExtra("answer_true", 0);
                            int zone_question = intent.getIntExtra("zone",0);
                            int game_round_id = intent.getIntExtra("game_round_id",0);

                            Question ques = new Question(getApplicationContext());
                            Question.create_question(tag, user_id, question_id,
                                                question_answer,
                                                answer1_question, answer2_question, answer3_question, answer4_question,
                                    answer_true, time,zone_question,game_round_id,question_type);

                            HashMap<String, String> question_data = Question.getQuestion();
                            String question = question_data.get(Question.KEY_QUESTION);
                            String ques_type = question_data.get("question_type");

                            if (!question.equals("")){
                                if(ques_type.equals("insert")){
                                    create_question_dialog();

                                }
                                if(ques_type.equals("select")){
                                    create_question_dialog();
                                }
                            }

                            break;

                        case "success_answer":
                            Zones _zones = new Zones(getApplicationContext());
                            HashMap<String,String> s = Zones.get_success();
                            is_success_answer = Boolean.parseBoolean(s.get("success"));
                            Question _question = new Question(getApplicationContext());
                            HashMap<String, String> q_data = Question.getQuestion();
                            if(is_success_answer){
                                int my_id;
                                if(username1.equals(name)){
                                    my_id = Integer.parseInt(user_id1);
                                }else{
                                    my_id = Integer.parseInt(user_id2);
                                }
                                AppController.getApi().get_answer_time(Constants.Methods.Version.VERSION,Constants.Methods.Game.GET_ANSWER_TIME,my_id,
                                        Integer.parseInt(q_data.get("game_round_id"))).enqueue(new Callback<JSONObject>() {
                                    @Override
                                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<JSONObject> call, Throwable t) {

                                    }
                                });
                            }
                            break;

                        case "answer":

                            int _winner = intent.getIntExtra("winner",0);
                            int _zone = intent.getIntExtra("zone",0);
                            double _time1 = intent.getDoubleExtra("time1",0);
                            double _time2 = intent.getDoubleExtra("time2",0);
                            int _answer1 = intent.getIntExtra("answer1",0);
                            int _answer2 = intent.getIntExtra("answer2",0);
                            String correct1 = intent.getStringExtra("correct1");
                            String correct2 = intent.getStringExtra("correct2");
                            boolean _win1 = intent.getBooleanExtra("win1",false);
                            boolean _win2 = intent.getBooleanExtra("win2",false);

                            Zones zones = new Zones(getApplicationContext());
                            Zones.create_result_session(_winner,_zone,_time1,_time2,_answer1,_answer2,correct1,correct2,_win1,_win2);

                            HashMap<String, String> zones_data = Zones.get_result_session();
                            int winner = Integer.parseInt(zones_data.get(Zones.KEY_WINNER));
                            int zone = Integer.parseInt(zones_data.get(Zones.KEY_ZONE));
                            double time1 = Double.parseDouble(zones_data.get(Zones.KEY_TIME1));
                            double time2 = Double.parseDouble(zones_data.get(Zones.KEY_TIME2));
                            String answer1 = zones_data.get(Zones.KEY_ANSWER1);
                            String answer2 = zones_data.get(Zones.KEY_ANSWER2);


                            Log.i(TAG, String.valueOf(winner));
                            Log.i(TAG, String.valueOf(zone));
                            Log.i(TAG, String.valueOf(time1));
                            Log.i(TAG, String.valueOf(time2));
                            Log.i(TAG, String.valueOf(answer1));
                            Log.i(TAG, String.valueOf(answer2));

                            int u_id1 = Integer.parseInt(user_id1);
                            int u_id2 = Integer.parseInt(user_id2);

                            setArea( winner, zone, u_id1, u_id2,time1,time2,answer1,answer2,correct1,correct2);

                            break;
                    }
                }
            }
        };
    }

    private  void GetSetData(){

        try{
            Game game = new Game(getApplicationContext());
            HashMap<String, String> data = Game.getDetails();
            game_id = data.get(Game.KEY_GAME_ID);
            first_player = data.get(Game.KEY_FIRST);
            user_id1 = data.get(Game.KEY_USER_ID1);
            user_id2 = data.get(Game.KEY_USER_ID2);
            start_zone1 = data.get(Game.KEY_START_ZONE_1);
            start_zone2 = data.get(Game.KEY_START_ZONE_2);
            username1 = data.get(Game.KEY_USERNAME_1);
            username2 = data.get(Game.KEY_USERNAME_2);
        }catch (Error e){
            Log.e(TAG,e.toString());
        }

        init_first_player();


        //new
        for (int i = 0; i < zonesImageView.length; i++) {
            zonesImageView[i].setImageResource(zonesDrawables[i]);
        }

        for (int i = 0; i < zonesImageView.length; i++) {
            if(i == zone_1-1) zonesImageView[i].paintZoneCapital(zonesDrawables[i],GREEN);
            if(i == zone_2-1) zonesImageView[i].paintZoneCapital(zonesDrawables[i],RED);
        }

        for (int i = 0; i < zonesImageView.length; i++) {
            final MapItemView item = zonesImageView[i];
            final int t = i+1;
            item.setMapListener(new MapItemView.MapEventListener() {
                @Override
                public void onZoneTouch(MapItemView zone) {
                    if (zone.getWhoseZone() == INVADED) {
                        if (zone.isCapital()) {
                            Toast.makeText(MainActivity.this, "My Capital" + t, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Invaded " + t, Toast.LENGTH_SHORT).show();
                        }
                    } else if (zone.getWhoseZone() == NOT_INVADED) {
                        if (zone.isCapital()) {

                            Toast.makeText(MainActivity.this, "Enemy's Capital " + t, Toast.LENGTH_SHORT).show();
                        } else if (zone.getZoneColor() == NO_COLOR) {

                            if(DEBUG) Toast.makeText(MainActivity.this, "Empty zone " + t, Toast.LENGTH_SHORT).show();
                            choose_land(t);
                            zone.limitClick();
                        } else {
                            choose_land(t);
                            if(DEBUG) Toast.makeText(MainActivity.this, "Enemy's zone " + t, Toast.LENGTH_SHORT).show();
                            zone.limitClick();
                        }
                    }
                }
            });
        }



    }

    private void init_first_player() {

        //if username1 equals default profile's name, define first player.
        if(username1.equals(name)) {
            //if user1 is first
            if(first_player.equals(user_id1)) {

                move_user_id = Integer.parseInt(user_id1);
                log(username1+"'s"+" move!");
                setOnClickListenersForUser(true);

            }else{

                move_user_id = Integer.parseInt(user_id2);
                log(username2+"'s"+" move!");
                setOnClickListenersForUser(false);

            }

            textView_user_id1.setText(username1 + "(You)");
            zone_1 = Integer.parseInt(start_zone1);
            zone_2 = Integer.parseInt(start_zone2);
            textView_user_id2.setText(username2);


        } else{
            if(first_player.equals(user_id2)){

                move_user_id = Integer.parseInt(user_id2);
                log(username2+"'s"+" move!");
                setOnClickListenersForUser(true);


            }else{

                move_user_id = Integer.parseInt(user_id1);
                log(username1+"'s"+" move!");
                setOnClickListenersForUser(false);
            }

            textView_user_id1.setText(username2 + "(You)");
            zone_1 = Integer.parseInt(start_zone2);
            zone_2 = Integer.parseInt(start_zone1);
            textView_user_id2.setText(username1);

        }
    }

    public void log(String msg){
        log += "\n" + msg;
        textview_logs.setText(log);
        log_scroll.fullScroll(View.FOCUS_DOWN);
    }

    //new
    public void setOnClickListenersForUser(boolean isTouchable) {
        if (isTouchable)
            for (MapItemView aZonesImageView : zonesImageView) aZonesImageView.enableZone();
        else
            for (MapItemView aZonesImageView : zonesImageView) aZonesImageView.disableZone();
    }

    private boolean isCapital(int zone) {
        return zone == Integer.parseInt(start_zone1) || zone == Integer.parseInt(start_zone2);
    }

    //new
    public void paint(int zone,int color){
        for (int i = 0; i < zonesImageView.length; i++) {
            if(zone-1 == i) zonesImageView[i].paintZone(zonesDrawables[i],color);
        }
    }

    private void setArea(int winner,int zone,int u_id1,int u_id2,double time1,double time2,String answer1,String answer2, String correct1,String correct2) {
        Game game = new Game(getApplicationContext());

        areas[Integer.parseInt(start_zone1)-1] = u_id1;
        areas[Integer.parseInt(start_zone2)-1] = u_id2;
        stepsCounter++;
        if(isStepAvailable(stepsCounter) && isAreaAvailable() && !isAllInvaded()){
            if(account_status==2)
            if(isAreaAvailable()) Toast.makeText(getApplicationContext(),"available",Toast.LENGTH_SHORT).show();

            log("Steps: "+String.valueOf(stepsCounter));
            create_result_dialog_round(winner, time1, time2, answer1, answer2,correct1,correct2);
            if (username1.equals(name)) {
                //user1 green
                //user2 red
                if (winner == u_id1) {
                    //green
                    areas[zone - 1] = u_id1;
//                    set_green_zone(zone);
                    //new
                    paint(zone,GREEN);
                    log(username1 + "'s move!");

                    move_user_id = u_id1;
                    setOnClickListenersForUser(true);

                }
                if (winner == u_id2) {
                    //red
//                    set_red_zone(zone);
                    areas[zone - 1] = u_id2;


                    //new
                    paint(zone,RED);
                    log(username2 + "'s move!");
                    move_user_id = u_id2;
                    setOnClickListenersForUser(false);
                }
                if (winner == 0) {
                    if (move_user_id == u_id1) {
                        log(username2 + "'s" + " move!");

                        move_user_id = u_id2;
                        setOnClickListenersForUser(false);
                    } else {
                        log(username1 + "'s" + " move!");

                        move_user_id = u_id1;
                        setOnClickListenersForUser(true);
                    }
                }

            } else {
                //user1 red
                //user2 green
                if(account_status==2)
                    if(isAreaAvailable()) Toast.makeText(getApplicationContext(),"available",Toast.LENGTH_SHORT).show();

                if (winner == u_id2) {
                    //green
                    areas[zone - 1] = u_id2;
//                    set_green_zone(zone);
                    //new
                    paint(zone,GREEN);
                    log(username2 + "'s" + " move!");

                    move_user_id = u_id2;
                    setOnClickListenersForUser(true);

                }
                if (winner == u_id1) {
                    //red
//                    set_red_zone(zone);
                    areas[zone - 1] = u_id1;
                    //new
                    paint(zone,RED);
                    log(username1 + "'s" + " move!");

                    move_user_id = u_id1;
                    setOnClickListenersForUser(false);

                }
                if (winner == 0) {
                    if (move_user_id == u_id2) {
                        log(username1 + "'s" + " move!");
                        move_user_id = u_id1;

                        setOnClickListenersForUser(false);
                    } else {
                        log(username2 + "'s" + " move!");
                        move_user_id = u_id2;
                        setOnClickListenersForUser(true);
                    }
                }

            }

        }else{
            if(account_status==2)
                if(!isAreaAvailable())
                    Toast.makeText(getApplicationContext(), R.string.game_no_available, Toast.LENGTH_SHORT).show();

            //end round
            int zones1 = countZones(Integer.parseInt(user_id1));
            int zones2 = countZones(Integer.parseInt(user_id2));
            create_result_dialog_end(winner, time1, time2, answer1, answer2,correct1,correct2,zones1,zones2);

        }
    }

    private int countZones(int user_id){
        int c = 0;
        for (int area : areas) if (area == user_id) c++;
        return c;
    }



   private boolean isAreaAvailable(){
       int counter = 0;
       for (int i = 0; i < areas.length; i++) {
           if(areas[i]==Integer.parseInt(user_id1) || areas[i] == Integer.parseInt(user_id2))
               counter++;
       }

       return counter != areas.length;
   }


    private  boolean isAllInvaded(){
        int counter =0;
        int my_id;
        if(username1.equals(name)){
            my_id = Integer.parseInt(user_id1);
        }else{
            my_id = Integer.parseInt(user_id2);
        }

        for (int i = 0; i < areas.length; i++) {
            if(areas[i]==my_id){
                counter++;
            }
        }
        return counter == areas.length;
    }

    public boolean isStepAvailable(int move){
        int steps = 6;
        return move < steps;
    }

    //override methods
    @Override
    public void onBackPressed() {
        if(doublePress){
            super.onBackPressed();
            return;
        }
        this.doublePress = true;
        Toast.makeText(getApplicationContext(), R.string.game_back_press,Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doublePress = false;
            }
        },2000);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time areasFlag new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();

    }

    //giving data to server
    public void choose_land(int id){
//        final int i = get_touched(id);
        AppController.getApi().get_question(Constants.Methods.Version.VERSION,Constants.Methods.Game.Question.GET, Integer.parseInt(game_id), id, move_user_id).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {

            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {

            }
        });
    }

        //question part
    private void create_question_dialog() {
        wait_dialog = new AlertDialog.Builder(MainActivity.this);
        wait_dialog.setTitle(getString(R.string.dialog_load_type));
        d = wait_dialog.create();
        d.show();
        int my_id;
        if(username1.equals(name)){
            my_id = Integer.parseInt(user_id1);
        }else{
            my_id = Integer.parseInt(user_id2);
        }
        QuestionFragment questionFragment = new QuestionFragment(my_id);
        questionFragment.setArguments(bundle);
        questionFragment.show(fm,"tag");
    }

    private void create_result_dialog_round(int winner, double time1, double time2, String answer1, String answer2, String correct1, String correct2) {
        wait_dialog.setTitle(R.string.game_forward_answer);
        AnswerResultFragment fragment = new AnswerResultFragment(winner,time1,time2,answer1,answer2,correct1,correct2,0,d);
        fragment.show(fm,"tag");
    }

    private void create_result_dialog_end(int winner, double time1, double time2, String answer1, String answer2, String correct1, String correct2,int zones1,int zones2) {
        wait_dialog.setTitle(R.string.game_forward_answer);
        AnswerResultFragment fragment = new AnswerResultFragment(winner,time1,time2,answer1,answer2,correct1,correct2,1,zones1,zones2,d);
        fragment.show(fm,"tag");
    }


}