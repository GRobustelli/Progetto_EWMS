package it.unisa.ewms.persistance.beans;

public class Notifica {
    private int id;
    private int taskId;
    private String sender;
    private String receiver;
    private boolean vista;
    private String messaggio;
    public Notifica() {
    }

    public Notifica(int id, int taskId, String sender, String receiver, String messaggio) {
        this.id = id;
        this.taskId = taskId;
        this.sender = sender;
        this.receiver = receiver;
        this.messaggio = messaggio;
        this.vista = false;
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

    public boolean isVista() {
        return vista;
    }

    public void setVista(boolean vista) {
        this.vista = vista;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
}
