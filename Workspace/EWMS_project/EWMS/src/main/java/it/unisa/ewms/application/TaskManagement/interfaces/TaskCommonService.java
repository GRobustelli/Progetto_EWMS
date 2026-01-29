package it.unisa.ewms.application.TaskManagement.interfaces;

import it.unisa.ewms.model.beans.Task;

import java.io.FileOutputStream;
import java.sql.SQLException;

public interface TaskCommonService {
    Task getTask(long taskId) throws SQLException;
    //List<Task> filterTask(String filtro) throws SQLException;
    //boolean holdTask(long taskId) throws Exception;
    FileOutputStream downloadAllegato(long taskId) throws Exception;
}
