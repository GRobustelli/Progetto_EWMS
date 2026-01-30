package it.unisa.ewms.application.TaskManagement.interfaces;

import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public interface TaskSupervisoreService {
    Task createTask(long taskId, String titolo, Date dataCreazione, Date dataScadenza, String istruzioni, Tipi.stato stato, String supervisoreId, String dipendenteId, Tipi.priorita priorita) throws SQLException;
    boolean deleteTask(long taskId) throws SQLException;
    //boolean sendWarning(long taskId, String msg) throws Exception;
}
