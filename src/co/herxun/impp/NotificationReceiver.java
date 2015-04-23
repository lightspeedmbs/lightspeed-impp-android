package co.herxun.impp;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import co.herxun.impp.activity.LoginActivity;

import com.arrownock.push.PushBroadcastReceiver;

public class NotificationReceiver extends PushBroadcastReceiver {
    @Override
    public void showNotification(Context context, JSONObject payload, int notificationId) {
        if (payload == null) {
            Log.e(LOG_TAG, "Payload is null!");
        }

        // Ensure payload is correct, and get needed information from received
        // payload
        String alert = null;
        boolean vibrate = false;
        boolean collapse = true;
        long[] vibrateTag = new long[] { 0, 500 };
        String sound = null;
        Uri soundUri = null;
        String title = null;
        String icon = null;
        int iconID = 0;
        int badge = 0;

        try {
            JSONObject androidPartJson = payload.getJSONObject("android");
            alert = androidPartJson.optString("alert", null);
            vibrate = androidPartJson.optBoolean("vibrate", false);
            sound = androidPartJson.optString("sound", null);
            title = androidPartJson.optString("title", null);
            icon = androidPartJson.optString("icon", null);
            badge = androidPartJson.optInt("badge", 0);
           // collapse = androidPartJson.optBoolean("collapse", false);
        } catch (JSONException ex) {
            if (alert == null)
                alert = payload.toString();
        }

        // Prepare notification to show message
        if (title == null) {
            try {
                title = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
            } catch (Exception ex) {
                title = "";
            }
        }

        if (icon != null) {
            try {
                iconID = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
                if (iconID < 1)
                    iconID = context.getApplicationInfo().icon;
            } catch (Exception ex) {
                iconID = context.getApplicationInfo().icon;
            }
        } else {
            iconID = context.getApplicationInfo().icon;
        }

        if (sound == null) {
        } else if (sound.startsWith("media:")) {
            String number = null;
            try {
                number = sound.substring(6);
            } catch (Exception ex) {
            }
            if (number == null) {
            } else {
                soundUri = Uri.parse("content://media/internal/audio/media/" + number);
            }
        } else if (sound.startsWith("sd:")) {
            String name = null;
            try {
                name = sound.substring(3);
            } catch (Exception ex) {
            }
            if (name == null) {
            } else {
                soundUri = Uri.parse("file://sdcard/" + name);
            }
        } else {
            String uriAddr = "android.resource://" + context.getPackageName() + "/raw/" + sound;
            soundUri = Uri.parse(uriAddr);
        }

        Intent intent = getIntentByPayload(context, payload);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = null;

        if (Build.VERSION.SDK_INT < 11) {
            // Use previous method to create a Notification instance
            // Log.d(LOG_TAG, "Use Notification.new");
            n = new Notification();

            n.flags |= Notification.FLAG_SHOW_LIGHTS;
            n.flags |= Notification.FLAG_AUTO_CANCEL;
            // n.defaults = Notification.DEFAULT_ALL;
            if (sound != null && sound.equals("default"))
                n.defaults |= Notification.DEFAULT_SOUND;
            else
                n.sound = soundUri;
            n.when = System.currentTimeMillis();
            n.icon = iconID;
            if (badge > 0)
                n.number = badge;
            if (vibrate)
                n.vibrate = vibrateTag;
            n.setLatestEventInfo(context, title, alert, pi);
        } else {
            // Use Notification.builder to create a Notification instance
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(pi).setSmallIcon(iconID).setWhen(System.currentTimeMillis()).setAutoCancel(true)
                    .setContentTitle(title);
            if (badge > 0)
                builder.setNumber(badge);
            if (alert != null && !"".equals(alert))
                builder.setContentText(alert);
            if (sound != null && sound.equals("default"))
                builder.setDefaults(Notification.DEFAULT_SOUND);
            else
                builder.setSound(soundUri);
            if (vibrate)
                builder.setVibrate(vibrateTag);

            n = builder.getNotification();
        }
        int notifyId = 1;
        if (!collapse) {
            notifyId = (notificationId == -1 ? (int) System.currentTimeMillis() : notificationId);
        }
        notifManager.notify(notifyId, n);
    }

    private Intent getIntentByPayload(Context context, JSONObject payload) {
        Log.e("receiver", payload.toString());
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payload", payload.toString());
        return intent;
    }
}