package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.Informazioni;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TaskSupervisoreServiceImpl implements TaskSupervisoreService {
    @Override
    public Task createTask(long taskId, String titolo, Date dataCreazione, Date dataScadenza, String istruzioni, Tipi.stato stato, String supervisoreId, String dipendenteId, Tipi.priorita priorita) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{
            Task tmp = new Task(taskId, titolo, dataCreazione,dataScadenza, istruzioni, stato, supervisoreId, dipendenteId, priorita);

            PersistenceService service = PersistenceServiceImpl.getInstance();
            ITaskDAO taskDAO = service.getTaskDAO();

            try{
                taskDAO.create(tmp);
                return tmp;
            }catch(Exception e){throw new SQLException(e);}
        }
    }

    @Override
    public boolean deleteTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{
            PersistenceService service = PersistenceServiceImpl.getInstance();
            ITaskDAO taskDAO = service.getTaskDAO();


            try{
                taskDAO.delete(taskId);
                return true;
            }catch(Exception e){throw new SQLException(e);}
        }
    }

    @Override
    public List<Task> getAllTaskSup(String supervisoreId) throws SQLException {
        PersistenceService service = PersistenceServiceImpl.getInstance();
        ITaskDAO taskDAO = service.getTaskDAO();
        IUtenteDAO utenteDAO = service.getUtenteDAO();

        try{
            Utente user = utenteDAO.findByMatricola(supervisoreId);
            return taskDAO.findByUtente(user);
        }catch(Exception e){throw new SQLException(e);}
    }


    @Override
    public List<Informazioni> getAllDipendentiInfo(String supervisoreId) throws SQLException {
        PersistenceService service = PersistenceServiceImpl.getInstance();
        IUtenteDAO utenteDAO = service.getUtenteDAO();

        try{
            return utenteDAO.getAllDipendentiInfo(supervisoreId);
        }catch(Exception e){throw new SQLException(e);}
    }
}
