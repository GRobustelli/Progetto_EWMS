package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.SQLException;

public class TaskCommonServiceImpl implements TaskCommonService {
    @Override
    public Task getTask(long taskId) throws SQLException {
        if(taskId < 0 || taskId == -1L){
            throw new IllegalArgumentException("L'id non può essere null, vuoto o negativo");
        }else{
            PersistenceService service = PersistenceServiceImpl.getInstance();
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
}
