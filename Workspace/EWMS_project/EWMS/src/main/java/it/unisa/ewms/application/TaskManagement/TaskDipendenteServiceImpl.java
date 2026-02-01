package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class TaskDipendenteServiceImpl implements TaskDipendenteService {
    @Override
    public boolean inizializzaTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{
            PersistenceService service = PersistenceServiceImpl.getInstance();
            ITaskDAO taskDAO = service.getTaskDAO();

            try{
               taskDAO.updateStatus(taskId, Tipi.stato.IN_ESECUZIONE);
               return true;
            }catch(Exception e){throw new SQLException(e);}
        }
    }

    @Override
    public boolean completeTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{
            PersistenceService service = PersistenceServiceImpl.getInstance();
            ITaskDAO taskDAO = service.getTaskDAO();

            try{
                taskDAO.updateStatus(taskId, Tipi.stato.COMPLETATO);
                return true;
            }catch(Exception e){throw new SQLException(e);}
        }
    }

    @Override
    public List<Task> getAllTaskDip(String dipendenteId) throws SQLException {
        PersistenceService service = PersistenceServiceImpl.getInstance();
        ITaskDAO taskDAO = service.getTaskDAO();
        IUtenteDAO utenteDAO = service.getUtenteDAO();

        try{
            Utente user = utenteDAO.findByMatricola(dipendenteId);
            return taskDAO.findByUtente(user);
        }catch(Exception e){throw new SQLException(e);}
    }
}
