package it.unisa.ewms.application.TaskManagement.interfaces;

import it.unisa.ewms.model.beans.Task;

import java.sql.SQLException;
import java.time.LocalDate;

public interface TaskSupervisoreService {
    Task createTask(long taskId, String descrizione, LocalDate dataScadenza, String priorita, String dipendenteId) throws SQLException;
    boolean deleteTask(long taskId) throws SQLException;
    boolean sendWarning(long taskId, String msg);
}
