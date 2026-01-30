package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.Task;

import java.sql.SQLException;
import java.time.LocalDate;

public class TaskSupervisoreServiceImpl implements TaskSupervisoreService {
    @Override
    public Task createTask(long taskId, String descrizione, LocalDate dataScadenza, String priorita, String dipendenteId) throws SQLException {
        return null;
    }

    @Override
    public boolean deleteTask(long taskId) throws SQLException {
        return false;
    }
}
