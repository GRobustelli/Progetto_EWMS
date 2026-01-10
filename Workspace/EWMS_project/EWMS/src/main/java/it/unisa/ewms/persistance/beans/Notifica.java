package it.unisa.ewms.persistance.beans;

public class Notifica {
    private int id;
    private int taskId;
    private String sender;
    private String receiver;

    public Notifica() {
    }

    public Notifica(int id, int taskId, String sender, String receiver) {
        this.id = id;
        this.taskId = taskId;
        this.sender = sender;
        this.receiver = receiver;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
