package com.example.alarm__wars;

import java.util.Calendar;
import java.util.List;

public class Room {
    private String question;
    private String answer;
    private boolean isHostSelected;
    private long alarmTime; // 알람 시간 정보를 저장할 변수

    private List<Boolean> dates;

    private boolean isButtonPressed;

    private boolean isQuestionSubmitted;

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    }

    public Room(String question, String answer, boolean isHostSelected, long alarmTime, boolean isButtonPressed, boolean isQuestionSubmitted) {
        this.question = question;
        this.answer = answer;
        this.isHostSelected = isHostSelected;
        this.alarmTime = alarmTime;
        this.isButtonPressed = isButtonPressed;
        this.isQuestionSubmitted = isQuestionSubmitted;
//        this.dates = dates;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isHostSelected() {
        return isHostSelected;
    }
    public boolean isButtonPressed() {
        return isButtonPressed;
    }
    public boolean isQuestionSubmitted() {
        return isQuestionSubmitted;
    }
    public List<Boolean> getDates() { return dates; }
    public long getAlarmTime() {
        return alarmTime;
    }



    // Getters and setters for Firebase
}
