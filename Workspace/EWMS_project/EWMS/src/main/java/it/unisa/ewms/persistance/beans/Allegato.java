package it.unisa.ewms.persistance.beans;

public class Allegato {
    private String filename;
    private int taskId;
    private String filePath;
    private String contentType;

    public Allegato() {}

    public Allegato(String filename, int taskId, String filePath, String contentType) {
        this.filename = filename;
        this.taskId = taskId;
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
