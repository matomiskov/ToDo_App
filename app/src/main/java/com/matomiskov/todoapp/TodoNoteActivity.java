package com.matomiskov.todoapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TodoNoteActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    Spinner spinner;
    EditText inTitle, inDesc, inDate, inTime;
    boolean isNewTodo = false;
    EditText eDate;
    EditText eTime;

    public ArrayList<String> spinnerList = new ArrayList<>();
    MyDatabase myDatabase;

    Todo updateTodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        eDate = findViewById(R.id.inDate);
        eTime = findViewById(R.id.inTime);

        eDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        spinner = findViewById(R.id.spinner);
        inTitle = findViewById(R.id.inTitle);
        inDesc = findViewById(R.id.inDescription);
        inDate = findViewById(R.id.inDate);
        inTime = findViewById(R.id.inTime);

        spinnerList.add(getResources().getString(R.string.household));
        spinnerList.add(getResources().getString(R.string.work));
        spinnerList.add(getResources().getString(R.string.other));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, MyDatabase.DB_NAME).build();

        int todo_id = getIntent().getIntExtra("id", -100);

        if (todo_id == -100)
            isNewTodo = true;

        if (!isNewTodo) {
            fetchTodoById(todo_id);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.delete_all_todos).setVisible(false);
        menu.findItem(R.id.changeL).setVisible(false);
        menu.findItem(R.id.btnDone).setVisible(true);
        if (!isNewTodo) {
            menu.findItem(R.id.btnDelete).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnDone:
                if (isNewTodo) {
                    String title = inTitle.getText().toString();
                    String date = inDate.getText().toString();
                    String time = inTime.getText().toString();
                    if (title == null || title.length() == 0) {
                        toast(0);
                    }
                    if (date == null || date.length() == 0) {
                        toast(1);
                    }
                    if (time == null || time.length() == 0) {
                        toast(2);
                    } else {
                        Todo todo = new Todo();
                        todo.name = title;
                        todo.description = inDesc.getText().toString();
                        todo.date = date;
                        todo.time = time;
                        todo.category = convertSpinner();

                        insertRow(todo);
                    }
                } else {
                    String title = inTitle.getText().toString();
                    String date = inDate.getText().toString();
                    String time = inTime.getText().toString();
                    if (title == null || title.length() == 0) {
                        toast(0);
                    }
                    if (date == null || date.length() == 0) {
                        toast(1);
                    }
                    if (time == null || time.length() == 0) {
                        toast(2);
                    } else {
                        updateTodo.name = title;
                        updateTodo.description = inDesc.getText().toString();
                        updateTodo.date = date;
                        updateTodo.time = time;
                        updateTodo.category = convertSpinner();

                        updateRow(updateTodo);
                    }
                }
                return true;
            case R.id.btnDelete:
                deleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String convertSpinner(){
        String sCategory;
        String ssp = spinner.getSelectedItem().toString();
        if (ssp.equals(getResources().getString(R.string.household))) {
            sCategory = "1";
        }
        else if (ssp.equals(getResources().getString(R.string.work))) {
            sCategory = "2";
        }
        else if (ssp.equals(getResources().getString(R.string.other))) {
            sCategory = "3";
        } else {
            throw new IllegalStateException("Unexpected value: " + spinner.getSelectedItem().toString());
        }
        return sCategory;
    }

    public void deleteDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteRow(updateTodo);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.sure)).setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    public void toast(int i) {
        if (i == 0) {
            Toast.makeText(this, getResources().getString(R.string.titleSetUp), Toast.LENGTH_SHORT).show();
        }
        if (i == 1) {
            Toast.makeText(this, getResources().getString(R.string.dateSetUp), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.timeSetUp), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchTodoById(final int todo_id) {
        new AsyncTask<Integer, Void, Todo>() {
            @Override
            protected Todo doInBackground(Integer... params) {

                return myDatabase.daoAccess().fetchTodoListById(params[0]);

            }

            @Override
            protected void onPostExecute(Todo todo) {
                super.onPostExecute(todo);
                inTitle.setText(todo.name);
                inDesc.setText(todo.description);
                inDate.setText(todo.date);
                inTime.setText(todo.time);
                spinner.setSelection(spinnerList.indexOf(indexToCategory(todo.category)));

                updateTodo = todo;
            }
        }.execute(todo_id);

    }

    public String indexToCategory(String category){
        String sCategory = "";
       switch (category){
            case "1": sCategory = getResources().getString(R.string.household);
                break;
            case "2": sCategory = getResources().getString(R.string.work);
                break;
            case "3": sCategory = getResources().getString(R.string.other);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + category);
        }
        return sCategory;
    }

    @SuppressLint("StaticFieldLeak")
    private void insertRow(Todo todo) {
        new AsyncTask<Todo, Void, Long>() {
            @Override
            protected Long doInBackground(Todo... params) {
                return myDatabase.daoAccess().insertTodo(params[0]);
            }

            @Override
            protected void onPostExecute(Long id) {
                super.onPostExecute(id);

                Intent intent = getIntent();
                intent.putExtra("isNew", true).putExtra("id", id);
                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute(todo);

    }

    @SuppressLint("StaticFieldLeak")
    private void deleteRow(Todo todo) {
        new AsyncTask<Todo, Void, Integer>() {
            @Override
            protected Integer doInBackground(Todo... params) {
                return myDatabase.daoAccess().deleteTodo(params[0]);
            }

            @Override
            protected void onPostExecute(Integer number) {
                super.onPostExecute(number);

                Intent intent = getIntent();
                intent.putExtra("isDeleted", true).putExtra("number", number);
                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute(todo);

    }


    @SuppressLint("StaticFieldLeak")
    private void updateRow(Todo todo) {
        new AsyncTask<Todo, Void, Integer>() {
            @Override
            protected Integer doInBackground(Todo... params) {
                return myDatabase.daoAccess().updateTodo(params[0]);
            }

            @Override
            protected void onPostExecute(Integer number) {
                super.onPostExecute(number);

                Intent intent = getIntent();
                intent.putExtra("isNew", false).putExtra("number", number);
                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute(todo);

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, month);
        myCalendar.set(Calendar.DAY_OF_MONTH, day);
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        eDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String min = String.valueOf(minute);
        if (minute / 10 == 0) {
            min = "0" + minute;
        }
        Log.d("MIN ", String.valueOf(minute));
        eTime.setText(hour + ":" + min);
    }
}
