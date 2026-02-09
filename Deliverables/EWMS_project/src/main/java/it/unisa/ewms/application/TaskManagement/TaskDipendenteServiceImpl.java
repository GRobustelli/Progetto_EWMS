package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.persistence.PersistenceServiceImpl;
import it.unisa.ewms.persistence.interfaces.ITaskDAO;
import it.unisa.ewms.persistence.interfaces.PersistenceService;

import java.sql.SQLException;
import java.util.List;

public class TaskDipendenteServiceImpl implements TaskDipendenteService {

    private final PersistenceService service;

    public TaskDipendenteServiceImpl() {
        this.service = PersistenceServiceImpl.getInstance();
    }

    public TaskDipendenteServiceImpl(PersistenceService service) {
        if (service == null) {throw new IllegalArgumentException("Service cannot be null");}
        this.service = service;
    }

    @Override
    public boolean inizializzaTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }

            ITaskDAO taskDAO = service.getTaskDAO();

            Task task = null;
            try {
                task=  taskDAO.findById(taskId);
            }catch (Exception e) {
                throw new  SQLException(e.getMessage());
            }

                if(task != null){
                    if (task.getStato() == Tipi.stato.COMPLETATO) {
                        throw new IllegalStateException("Impossibile sospendere un task nello stato completato o da completare");
                    }
                } else{
                    return false;
                }

            try{
               taskDAO.updateStatus(taskId, Tipi.stato.IN_ESECUZIONE);
               return true;
            }catch(Exception e){
                throw new SQLException(e.getMessage());

            }
        }


    @Override
    public boolean completeTask(long taskId) throws SQLException {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{

            ITaskDAO taskDAO = service.getTaskDAO();
            Task task= null;

                try {task = taskDAO.findById(taskId);
                } catch (Exception e) {
                    throw new  SQLException(e.getMessage());}

                if(task != null){
                    if (task.getStato() !=  Tipi.stato.IN_ESECUZIONE ) {
                        throw new IllegalStateException("Impossibile completare un task nello stato completato o in sospensione");
                    }
                } else{
                    return false;
                }



            try{
                taskDAO.updateStatus(taskId, Tipi.stato.COMPLETATO);
                return true;
            }catch(Exception e){
                throw new SQLException(e.getMessage());
            }
        }
    }

    @Override
    public List<Task> getAllTaskDip(Dipendente dipendente) throws SQLException {
        if (dipendente == null) {
            throw new IllegalArgumentException("Parametro non può essere null");
        }
        if (dipendente.getMatricola() <= 0){
            throw new IllegalArgumentException("Matricola non valida");
        }
        if (dipendente.getRuolo() != Tipi.ruolo.DIPENDENTE){
            throw new IllegalArgumentException("Accesso non autorizzato");
        }


        ITaskDAO taskDAO = service.getTaskDAO();

        try{

            return taskDAO.findByUtente(dipendente);
        }catch(Exception e){throw new SQLException(e);}
    }
}
