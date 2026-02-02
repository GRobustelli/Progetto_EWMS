package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.SQLException;

public class TaskCommonServiceImpl implements TaskCommonService {
    private final  PersistenceService service;

    public TaskCommonServiceImpl() {
        this.service = PersistenceServiceImpl.getInstance();
    }

    @Override
    public Task getTask(long taskId) throws SQLException {
        if(taskId <= 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{

            ITaskDAO taskDAO = service.getTaskDAO();

            try{
                return taskDAO.findById(taskId);
            }catch(Exception e){throw new SQLException(e);}
        }
    }

    /*
    @Override
    public FileOutputStream downloadAllegato(long taskId) throws Exception {
        return null;
    }
     */

    @Override
    public boolean holdTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }

        ITaskDAO taskDAO = service.getTaskDAO();

        try {
            Task task=  taskDAO.findById(taskId);
            if(task != null){
                if (task.getStato() == Tipi.stato.COMPLETATO ||task.getStato() == Tipi.stato.DA_COMPLETARE) {
                    throw new IllegalStateException("Impossibile sospendere un task nello stato completato o da completare");
                } else{
                    return false;
                }
            }
        } catch (Exception e) {
            throw new  SQLException(e.getMessage());
        }

        try{
            taskDAO.updateStatus(taskId, Tipi.stato.IN_SOSPENSIONE);
            return true;
        }catch(Exception e){
            throw new SQLException(e.getMessage());
        }

        }
    }

