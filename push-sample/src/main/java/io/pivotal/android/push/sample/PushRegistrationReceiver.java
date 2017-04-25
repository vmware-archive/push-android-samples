package io.pivotal.android.push.sample;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.android.pushservice.RegistrationReceiver;


public class PushRegistrationReceiver extends RegistrationReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.e("Push", "On receive with registration");
    }
}
