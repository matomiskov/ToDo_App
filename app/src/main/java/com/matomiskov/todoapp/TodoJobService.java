package com.matomiskov.todoapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoJobService extends JobService {
    private static final String TAG = "TodoJobService";
    private boolean jobCancelled = false;
    MyDatabase myDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, MyDatabase.DB_NAME).build();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("TAG", "Job started");
        doBackgroundWorks(jobParameters);

        return true;
    }

    public void doBackgroundWorks(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAllTodos();
                Log.d("TAG", "Job finished");
                jobFinished(jobParameters, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("TAG", "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private void loadAllTodos() {
        new AsyncTask<String, Void, List<Todo>>() {
            @Override
            protected List<Todo> doInBackground(String... params) {
                return myDatabase.daoAccess().fetchAllTodos();
            }

            @Override
            protected void onPostExecute(List<Todo> todoList) {
                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                Date dt = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                c.add(Calendar.MINUTE, 14);
                dt = c.getTime();
                String currentTimePlus = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dt);


                Log.d("TAG", "+++");
                for (int i = 0; i < todoList.size(); i++) {
                    Todo todo = todoList.get(i);
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    try {
                        Date date_from = formatter.parse(currentTime);
                        Date date_to = formatter.parse(currentTimePlus);
                        Date dateNow = formatter.parse(todo.time);
                        if (currentDate.equals(todo.date) && (date_from.before(dateNow) && date_to.after(dateNow))) {
                            showNotification(todo.name, todo.date + " " + todo.time);
                        }
                    } catch (ParseException e) {
                        Log.e("Date parsing error.", "");
                    }
                }
            }
        }.execute();
    }

    private void showNotification(String title, String task) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("inducesmile", "inducesmile", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "inducesmile")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(1, notification.build());
    }
}
