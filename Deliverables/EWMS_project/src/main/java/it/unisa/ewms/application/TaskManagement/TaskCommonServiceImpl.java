package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.persistence.PersistenceServiceImpl;
import it.unisa.ewms.persistence.interfaces.ITaskDAO;
import it.unisa.ewms.persistence.interfaces.PersistenceService;

import java.sql.SQLException;

 public class TaskCommonServiceImpl implements TaskCommonService {
    private final  PersistenceService service;

    public TaskCommonServiceImpl() {
        this.service = PersistenceServiceImpl.getInstance();
    }

    //Utilizzato per testing
     public TaskCommonServiceImpl(PersistenceService persistenceService) {
        if (persistenceService == null) { throw new  IllegalArgumentException("persistenceService == null"); }
        this.service = persistenceService;
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

    @Override
    public boolean holdTask(long taskId) throws Exception {
        if(taskId < 0){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }

        ITaskDAO taskDAO = service.getTaskDAO();

            Task task=  taskDAO.findById(taskId);

            if(task != null){

                if (task.getStato() == Tipi.stato.COMPLETATO ||task.getStato() == Tipi.stato.DA_COMPLETARE) {
                    throw new IllegalStateException("Impossibile sospendere un task nello stato completato o da completare");

                }else{
                    try{
                        taskDAO.updateStatus(taskId, Tipi.stato.IN_SOSPENSIONE);
                        return true;
                    }catch(Exception e){
                        throw new SQLException(e.getMessage());
                    }
                }
            }else{

                return false;
            }

        }

    /*
         @Override
        public Allegato downloadAllegato(long taskId) throws Exception {

          if (taskId <= 0) {
             throw new IllegalArgumentException("Id non valido");
         }

         Task task = getTask(taskId);

         if (task == null) {
             return null;
         } else {
             Allegato allegato = task.getAllegato();

             if (allegato != null) {
                 File file = new File(allegato.getFilePath());
                 if (!file.exists()) {
                     throw new FileNotFoundException("Il file non è presente sul disco: " + allegato.getFilename());
                 }

             }

             return allegato;
         }*/

 }



