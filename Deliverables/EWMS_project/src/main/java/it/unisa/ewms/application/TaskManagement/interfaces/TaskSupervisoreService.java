package it.unisa.ewms.application.TaskManagement.interfaces;

import it.unisa.ewms.model.beans.*;
import org.apache.commons.fileupload.FileItem;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface TaskSupervisoreService {
    void createTask(String titolo, Date dataCreazione, Date dataScadenza, String istruzioni, Tipi.stato stato, int supervisoreId, int dipendenteId, Tipi.priorita priorita, Allegato allegato) throws SQLException;
    boolean deleteTask(long taskId) throws SQLException;
    List<Task> getAllTaskSup(Supervisore supervisore) throws SQLException;
    List<Informazioni> getAllDipendentiInfo(int supervisoreId) throws SQLException;
    Allegato uploadAllegato(FileItem fileItem, String uploadDir);
}
