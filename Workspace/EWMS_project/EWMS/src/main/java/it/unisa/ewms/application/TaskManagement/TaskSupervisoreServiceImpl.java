package it.unisa.ewms.application.TaskManagement;

import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TaskSupervisoreServiceImpl implements TaskSupervisoreService {
    private final PersistenceService service;

    public TaskSupervisoreServiceImpl() {
        service =  PersistenceServiceImpl.getInstance();;
    }

    //utilizzato per il testing
    public TaskSupervisoreServiceImpl(PersistenceService persistenceService) {

        if (persistenceService == null) {throw new IllegalArgumentException();}
        this.service = persistenceService;
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

        IUtenteDAO utenteDAO = service.getUtenteDAO();


        Utente dipendente = utenteDAO.findByMatricola(dipendenteId);
        if (dipendente == null) {
            throw new IllegalArgumentException("Il dipendente con matricola " + dipendenteId + " non esiste.");
        }

        if (dipendente.getRuolo() != Tipi.ruolo.DIPENDENTE) {
            throw new IllegalArgumentException("La matricola inserita non appartiene ad un dipendente.");
        }


        Utente supervisore = utenteDAO.findByMatricola(supervisoreId);
        if (supervisore == null) {
            throw new IllegalArgumentException("Il supervisore con matricola " + supervisoreId + " non esiste.");
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
        if (supervisore.getMatricola() <= 0){
            throw new IllegalArgumentException("Matricola non valida");
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

    @Override
    public Allegato uploadAllegato(FileItem fileItem, String uploadDir) {
        //Validazione base: se il file è vuoto o non esiste, ritorna null
        if (fileItem == null || fileItem.getName() == null || uploadDir == null) return null;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            // Tenta di creare la cartella se non esiste
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Impossibile creare la directory di upload: " + uploadDir);
            }
        }

        // 3. Controllo Permessi (Opzionale ma utile per fail-fast)
        if (!dir.canWrite()) {
            throw new RuntimeException("Permessi di scrittura negati nella cartella: " + uploadDir);
        }

        // Recupera il nome originale pulito
        String originalFilename = FilenameUtils.getName(fileItem.getName());

        // Recupera l'estensione (es. "pdf", "jpg")
        String extension = FilenameUtils.getExtension(originalFilename);

        // Genera un nome univoco (UUID) + estensione
        String storedFilename = UUID.randomUUID().toString();
        if (!extension.isEmpty()) {
            storedFilename += "." + extension;
        }

        //Costruisci il path completo con il nome univoco
        String fullPath = uploadDir + File.separator + storedFilename;
        File fileOnDisk = new File(fullPath);

        //Scrivi il file
        try {
            fileItem.write(fileOnDisk);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile salvare file su disco");
        }

        //Ritorna l'oggetto per la persistenza su db
        return new Allegato(originalFilename, storedFilename, fullPath, fileItem.getContentType());
    }


}
