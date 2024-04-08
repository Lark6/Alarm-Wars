package com.example.alarm__wars;

public class Room {
    private String question;
    private String answer;
    private boolean isHostSelected;
    private long alarmTime; // 알람 시간 정보를 저장할 변수

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    }

    public Room(String question, String answer, boolean isHostSelected, long alarmTime) {
        this.question = question;
        this.answer = answer;
        this.isHostSelected = isHostSelected;
        this.alarmTime = alarmTime;
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

    public long getAlarmTime() {
        return alarmTime;
    }

    // Getters and setters for Firebase
}
