package it.unisa.ewms.application.TaskManagement.interfaces;

import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Task;

import java.sql.SQLException;
import java.util.List;

public interface TaskDipendenteService {
    boolean inizializzaTask(long taskId) throws SQLException;
    boolean completeTask(long taskId) throws SQLException;
    List<Task> getAllTaskDip(Dipendente dipendenteId) throws SQLException;
}
