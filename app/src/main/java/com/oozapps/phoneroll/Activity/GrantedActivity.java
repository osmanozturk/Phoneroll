package com.oozapps.phoneroll.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oozapps.phoneroll.MidClasses.Firebase.Course;
import com.oozapps.phoneroll.MidClasses.Firebase.FirebasePhonerollFunctions;
import com.oozapps.phoneroll.MidClasses.Firebase.Lecture;
import com.oozapps.phoneroll.MidClasses.Firebase.onGetServerDate;
import com.oozapps.phoneroll.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class GrantedActivity extends Activity implements onGetServerDate {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final int MATCHINGROOMREQUEST = 55642;
    private TextView extraText;
    private String uniqueDeviceId = null;
    private Date serverDate = null;
    private Lecture currentLecture = null;
    private Button confirmAttendanceButton = null;
    private TextView lectureTextView = null;
    private DatabaseReference currentDayLectureReference = null;
    private ListView attendanceListView = null;
    private Intent intent;
    private ArrayAdapter<String> attendListAdapter = null;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MATCHINGROOMREQUEST && resultCode == RESULT_OK &&
                data != null) {
            Bundle result = data.getExtras();
            boolean matchResult = result.getBoolean("result");

            if (matchResult) {
                DatabaseReference courseReference = firebaseDatabase.getReference("Users").child(uniqueDeviceId).child("courseList");
                ValueEventListener courseListener = new ValueEventListener() {
                    Course curCourse = null;
                    boolean found = false;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int i = 0;
                        for (DataSnapshot iter : snapshot.getChildren()) {
                            curCourse = iter.getValue(Course.class);
                            if (curCourse != null && curCourse.getCourseCode().equals(currentLecture.getCourseCode())) {
                                found = true;
                                Map<String, Object> updatedChildren = new HashMap<>();
                                Calendar serverCal = Calendar.getInstance();
                                Calendar lastAttendCal = Calendar.getInstance();
                                lastAttendCal.setTime(curCourse.getLastAttendDate());
                                serverCal.setTime(serverDate);


                                if (lastAttendCal.get(Calendar.DAY_OF_YEAR) == serverCal.get(Calendar.DAY_OF_YEAR) && lastAttendCal.get(Calendar.HOUR_OF_DAY) >= currentLecture.getStart().getHour() &&
                                        lastAttendCal.get(Calendar.HOUR_OF_DAY) <= currentLecture.getEnd().getHour()) {
                                    Toast.makeText(GrantedActivity.this, "You cannot attend more than once", Toast.LENGTH_LONG).show();

                                } else {
                                    curCourse.setAttendCount(curCourse.getAttendCount() + 1);
                                    curCourse.setLastAttendDate(serverDate);
                                    Toast.makeText(GrantedActivity.this, "Attendance Accepted", Toast.LENGTH_LONG).show();
                                }

                                updatedChildren.put("/" + Integer.toString(i) + "/", curCourse);
                                courseReference.updateChildren(updatedChildren);
                                break;
                            }
                            i++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Cannot update course for attendance");
                    }


                };
                courseReference.addListenerForSingleValueEvent(courseListener);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_granted);
        extraText = findViewById(R.id.extraTextGranted);
        confirmAttendanceButton = findViewById(R.id.confirmAttendanceButton);
        attendanceListView = findViewById(R.id.attendanceList);
        confirmAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lectureIntent = new Intent(GrantedActivity.this, MatchActivity.class);
                lectureIntent.putExtra("courseCode", currentLecture.getCourseCode());
                lectureIntent.putExtra("roomName", currentLecture.getRoomName());
                GrantedActivity.this.startActivityForResult(lectureIntent, MATCHINGROOMREQUEST);
            }
        });
        lectureTextView = findViewById(R.id.lectureTextView);
        intent = getIntent();
        String extraMessageString = intent.getStringExtra("extraTextInfo");
        uniqueDeviceId = intent.getStringExtra("uniqueDeviceId");
        extraText.setText(extraMessageString);


        FirebasePhonerollFunctions.getServerTime(this);

        DatabaseReference courseListReference = firebaseDatabase.getReference("Users").child(uniqueDeviceId).child("courseList");

        attendListAdapter = new ArrayAdapter<String>(GrantedActivity.this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        attendanceListView.setAdapter(attendListAdapter);


        ValueEventListener attendanceListerListener = new ValueEventListener() {
            Course curCourse;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> allCoursesAttendances = new ArrayList<>();
                for (DataSnapshot iter : snapshot.getChildren()) {
                    StringBuilder builder = new StringBuilder();
                    curCourse = (iter.getValue(Course.class));
                    builder.append(curCourse.getCourseCode()).append(": %").
                            append((float) curCourse.getAttendCount() / curCourse.getTotalCount() * 100).append(" attendance absent ")
                            .append(curCourse.getTotalCount() - curCourse.getAttendCount()).append(" times");
                    allCoursesAttendances.add(builder.toString());
                }

                attendListAdapter.clear();
                attendListAdapter.addAll(allCoursesAttendances);
                attendListAdapter.notifyDataSetChanged();

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        courseListReference.addValueEventListener(attendanceListerListener);


    }


    @Override
    public void onSuccess(long timestamp) {
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone(this.getString(R.string.app_timezone))); //Turkey Timezone
        serverDate = new Date(timestamp);
        Calendar temp = Calendar.getInstance();
        currentDayLectureReference =
                firebaseDatabase.getReference("Users").child(uniqueDeviceId).child("week").child(Integer.toString(
                        temp.get(Calendar.DAY_OF_WEEK) - 1)
                );


        ValueEventListener lectureListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(serverDate);
                Lecture curLect;
                boolean found = false;
                for (DataSnapshot iter : snapshot.getChildren()) {
                    curLect = iter.getValue(Lecture.class);
                    if (temp.get(Calendar.HOUR_OF_DAY) >= curLect.getStart().getHour() &&
                            temp.get(Calendar.HOUR_OF_DAY) <= curLect.getEnd().getHour()) {
                        if ((temp.get(Calendar.HOUR_OF_DAY) == curLect.getEnd().getHour() && temp.get(Calendar.MINUTE) <= curLect.getEnd().getMinute()) ||
                                (temp.get(Calendar.HOUR_OF_DAY) == curLect.getStart().getHour() && temp.get(Calendar.MINUTE) >= curLect.getStart().getMinute())) { //same hour check for minutes
                            //within minute range

                            found = true;
                            currentLecture = curLect;
                            break;


                        } else {
                            found = true;
                            currentLecture = curLect;
                            break;
                        }


                    }
                }
                if (found) {
                    lectureTextView.setText(String.format("%s\n Classroom is %s", currentLecture.getCourseCode(), currentLecture.getRoomName()));
                    lectureTextView.setVisibility(View.VISIBLE);
                    confirmAttendanceButton.setVisibility(View.VISIBLE);
                } else {
                    lectureTextView.setText(R.string.no_lectures_err);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: Checking hour of lecture Failed");
            }
        };
        currentDayLectureReference.addListenerForSingleValueEvent(lectureListListener);

    }

    @Override
    public void onFailed() {
        Toast.makeText(GrantedActivity.this, "Error Occured While Fetching Server Time", Toast.LENGTH_LONG).show();

    }
}
