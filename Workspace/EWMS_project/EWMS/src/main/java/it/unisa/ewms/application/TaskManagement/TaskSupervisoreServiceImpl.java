package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TaskSupervisoreServiceImpl implements TaskSupervisoreService {
    private final PersistenceService service;

    public TaskSupervisoreServiceImpl() {
        service =  PersistenceServiceImpl.getInstance();;
    }

    @Override
    public void createTask(String titolo, Date dataCreazione, Date dataScadenza, String istruzioni, Tipi.stato stato, int supervisoreId, int dipendenteId, Tipi.priorita priorita, Allegato allegato) throws SQLException {
            if (titolo == null || dataCreazione == null || dataScadenza == null ||
            istruzioni == null || priorita == null || stato == null) {
                throw new IllegalArgumentException("Patametri non possono essere null");
            }
            if (supervisoreId <= 0 || dipendenteId <= 0) {
                throw new IllegalArgumentException("Matricole non valide");
            }

            if (istruzioni.length() < 10){
                throw new IllegalArgumentException("Business rule: istruzioni non più piccole di 10 carattteri");
            }
            if (dataScadenza.before(dataCreazione)) {
                throw new IllegalArgumentException("Scadenza non può essere prima impostata prima della data di creazione");
            }


            Task tmp = new Task(titolo, dataCreazione,dataScadenza, istruzioni, stato, supervisoreId, dipendenteId, priorita);
            tmp.setAllegato(allegato);

            ITaskDAO taskDAO = service.getTaskDAO();

            try{
                taskDAO.create(tmp);
            }catch(Exception e){throw new SQLException(e);}
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
            }catch(Exception e){return false;}
        }
    }

    @Override
    public List<Task> getAllTaskSup(Supervisore supervisore) throws SQLException {
        if (supervisore == null){
            throw new IllegalArgumentException("Parametro non può essere null");
        }
        if (supervisore.getRuolo() != Tipi.ruolo.SUPERVISORE){
            throw new IllegalArgumentException("Accesso non autorizzato");
        }

        ITaskDAO taskDAO = service.getTaskDAO();

        try{
            return taskDAO.findByUtente(supervisore);
        }catch(Exception e){throw new SQLException(e);}
    }


    @Override
    public List<Informazioni> getAllDipendentiInfo(int supervisoreId) throws SQLException {
        if  (supervisoreId <= 0){
            throw new IllegalArgumentException("Matricola non valida");
        }

        IUtenteDAO utenteDAO = service.getUtenteDAO();

        try{
            return utenteDAO.getAllDipendentiInfo(supervisoreId);
        }catch(Exception e){throw new SQLException(e);}
    }
}
