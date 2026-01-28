package it.unisa.ewms.application.TaskManagement.interfaces;

import java.sql.SQLException;

public interface TaskDipendenteService {
    boolean inizializzaTask(long taskId) throws SQLException;
    boolean completeTask(long taskId) throws SQLException;
}
