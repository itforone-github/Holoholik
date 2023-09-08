package util;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


/**
 * Created by 투덜이2 on 2016-11-21.
 */

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;
    private View view = null;
    public BackPressCloseHandler(View v,Activity act){this.view=v;this.activity=act;}
    public BackPressCloseHandler(Activity act) {
        this.activity = act;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            //백그라운드 비콘 찾기
            if(view == null) {
                toast.cancel();
            }
        }
    }

    public void showGuide() {
        if(view == null) {
            toast = Toast.makeText(activity,
                    "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }else{
            Snackbar.make(view,"뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }
    }

}
