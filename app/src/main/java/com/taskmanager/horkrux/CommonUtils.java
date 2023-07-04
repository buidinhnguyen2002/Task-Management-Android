package com.taskmanager.horkrux;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.Models.Task;
import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.Notification.NotificationData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CommonUtils {
    private static FirebaseDatabase database;
    private static final String PATH = "Notifications";

    @RequiresApi(api = Build.VERSION_CODES.O)
    static public void sendNotificationToUser(Task task, Context context) {
        database = FirebaseDatabase.getInstance();
        for (String userId : task.getGrpTask()) {
            database.getReference("Users/" + userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users user = snapshot.getValue(Users.class);
                            NotificationData data = new NotificationData();
                            data.setNotificationTitle(task.getTaskTitle());
                            data.setNotificationMessage(task.getTaskDescription());
                            data.setNotificationId(generateId());
                            data.setNotificationDate(getCurrentDateAndTime());
                            database.getReference().child(PATH).child(user.getFireuserid()).child(data.getNotificationId()).setValue(data);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static public String getCurrentDateAndTime() {
        LocalDateTime myDateObj = LocalDateTime.now();
        System.out.println("Before Formatting: " + myDateObj);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("E,MMM dd yyyy HH:mm");

        String formattedDate = myDateObj.format(myFormatObj);

        return formattedDate;
    }

    static public String generateId() {
        return String.valueOf(new Date().getTime());
    }

    static public AlertDialog generateAlertDialog(Context context, String message, DialogInterface.OnClickListener positiveAction,
                                                  DialogInterface.OnClickListener negativeAction) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes", positiveAction);
        builder1.setNegativeButton(
                "No", negativeAction);

        return builder1.create();
    }
}
