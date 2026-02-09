package it.unisa.ewms.model.beans;

public class Allegato {
    private String filename;
    private String directoryFilename;
    private long taskId;
    private String filePath;
    private String contentType;

    public Allegato() {}

    public Allegato(String filename, int taskId, String filePath, String contentType) {
        this.filename = filename;
        this.taskId = taskId;
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public Allegato(String filename, String directoryFilename, String filePath, String contentType) {
        this.filename = filename;
        this.directoryFilename = directoryFilename;
        taskId = -1;
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public String getDirectoryFilename() {
        return directoryFilename;
    }

    public void setDirectoryFilename(String directoryFilename) {
        this.directoryFilename = directoryFilename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
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
