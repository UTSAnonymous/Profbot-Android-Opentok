package com.utsanonymous.profbotandroidopentok.adt;

public class BotPresence {
    private String botModel;
    private String assignedRoom;
    private String user;

    public BotPresence(String botModel, String assignedRoom, String user) {
        this.botModel = botModel;
        this.assignedRoom = assignedRoom;
        this.user = user;
    }

    public String getBotModel() { return botModel; }

    public String getAssignedRoom() { return assignedRoom; }

    public String getUser() { return user; }
}
