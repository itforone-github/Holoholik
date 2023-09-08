package util;

import android.app.Activity;
import android.util.Log;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class PermissionCheck {
    Activity mActivity;

    public PermissionCheck(Activity act){
        this.mActivity=act;
    }
    public void setPermission(String[] permissions){
        TedPermission.with(mActivity)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("To use the weather widget, you need to set up location permissions.")
                .setDeniedMessage("Please set permissions.")
                .setPermissions(permissions)
                .check();
    }
    public void setPermission(String[] permissions,String message){
        TedPermission.with(mActivity)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(message)
                .setDeniedMessage("Please set permissions.")
                .setPermissions(permissions)
                .check();
    }
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.d("permission","성공");

        }
        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }
    };
}
