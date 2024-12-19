package com.example.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.OkHttpClient;

public class MyNotificationListenerService extends NotificationListenerService {

    private final OkHttpClient client = new OkHttpClient();
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Lấy nội dung thông báo
        CharSequence  notificationText = sbn.getNotification().extras.getCharSequence("android.text");
        CharSequence title = sbn.getNotification().extras.getCharSequence("android.title");

        // Kiểm tra nếu notificationText không null và chuyển thành String
        // Kiểm tra nếu title và notificationText không null
        if (notificationText != null && title != null) {
            Log.d("NotificationListener", "Title: " + title + ", Text: " + notificationText);
            // Gửi thông báo đến API nếu cần
            sendNotificationToApi(title.toString(),notificationText.toString());
        } else {
            // Log khi không có title hoặc notificationText
            Log.d("NotificationListener", "Notification missing title or text");
            // Nếu chỉ cần thông báo text, bạn có thể gửi thông báo chỉ với text
            if (notificationText != null) {
                sendNotificationToApi("No title", notificationText.toString());
            } else {
                sendNotificationToApi("No notification text available", "No notification text available");
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Log khi thông báo bị xóa
        Log.d("NotificationListener", "Notification removed: " + sbn.getPackageName());
//        System.out.println(sbn.getPackageName());
    }

    private void sendNotificationToApi(String title, String notificationText) {
        String titleHandle = title.toLowerCase();
        if ( ! (titleHandle.contains("tiền") || titleHandle.contains("agribank")) ){
            return;
        }

        String ip = "192.168.2.122";
        // Gửi thông báo đến API của bạn
        Log.d("NotificationListener", "Sending notification to API: " + title + " - " + notificationText);
        String url = "http://" + ip + ":3030/notifications"; // Đảm bảo địa chỉ đúng

        // Tạo nội dung JSON cho request
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{\"notification\": {\"title\": \"" + title + "\", \"text\": \"" + notificationText + "\"}}";
        RequestBody body = RequestBody.create(json, JSON);

        // Tạo request POST
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Thực hiện gửi request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("NotificationListener", "Failed to send notification to API: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("NotificationListener", "Successfully sent notification to API");
                } else {
                    Log.e("NotificationListener", "Failed to send notification, response code: " + response.code());
                    Log.e("NotificationListener", "Response: " + response.body().string());
                }
            }
        });
    }

}
