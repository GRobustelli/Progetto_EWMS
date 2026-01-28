package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.model.beans.Allegato;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO implements ITaskDAO {
    @Override
    public void create(Task task) throws Exception {
    if (task == null) {
        throw new IllegalArgumentException("Il task non può essere null");
    }

        // Definisco le query SQL
        String insertTaskSql = "INSERT INTO Task (titolo, dataDiCreazione, dataDiScadenza, istruzioni, stato, supervisore, dipendente, priorita) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // Assumo che la tabella Allegato abbia queste colonne come da tua precedente indicazione
        String insertAllegatoSql = "INSERT INTO Allegato (filename, task_id, filepath, contentType) VALUES (?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement psTask = null;
        PreparedStatement psAllegato = null;

        try {
            connection = DataSourceFactory.getConnection();

            // 1. Disabilito l'Auto-Commit per gestire la transazione manualmente.
            // Questo assicura che Task e Allegato vengano salvati insieme o per niente.
            connection.setAutoCommit(false);

            // 2. Preparo lo statement per il Task specificando che voglio indietro l'ID generato
            psTask = connection.prepareStatement(insertTaskSql, Statement.RETURN_GENERATED_KEYS);

            psTask.setString(1, task.getTitolo());
            psTask.setDate(2, task.getDataCreazione());
            psTask.setDate(3, task.getDataDiScadenza());
            psTask.setString(4, task.getIstruzioni());
            psTask.setString(5, task.getStato().name()); // Enum -> String
            psTask.setString(6, task.getSupervisore());
            psTask.setString(7, task.getDipendente());
            psTask.setString(8,task.getPriorita().name());

            psTask.executeUpdate();

            // 3. Recupero l'ID generato dal database (Auto_Increment)
            try (ResultSet generatedKeys = psTask.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerato = generatedKeys.getInt(1);
                    task.setId(idGenerato); // Setto l'ID nell'oggetto Java
                } else {
                    throw new SQLException("Creazione task fallita, nessun ID ottenuto.");
                }
            }

            // 4. Se nel bean Task è presente un allegato, lo salvo ora usando l'ID appena recuperato
            if (task.getAllegato() != null) {
                psAllegato = connection.prepareStatement(insertAllegatoSql);

                psAllegato.setString(1, task.getAllegato().getFilename()); // Primary Key allegato
                psAllegato.setLong(2, task.getId()); // Foreign Key che collega al Task
                psAllegato.setString(3, task.getAllegato().getFilePath());
                psAllegato.setString(4, task.getAllegato().getContentType());

                psAllegato.executeUpdate();
            }

            // 5. Commit della transazione: confermo le modifiche nel database
            connection.commit();

        } catch (SQLException e) {
            // 6. Rollback in caso di errore: se qualcosa va storto, annullo tutto
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Log dell'errore di rollback
                }
            }
            throw new Exception("Errore durante l'inserimento del task e dell'eventuale allegato", e);
        } finally {
            // 7. Chiusura manuale delle risorse (ordine inverso di apertura)
            if (psAllegato != null) psAllegato.close();
            if (psTask != null) psTask.close();

            if (connection != null) {
                connection.setAutoCommit(true); // Ripristino lo stato di default
                connection.close();
            }
        }
    }


    @Override
    public Task findById(long id) throws Exception {

        //Controllo se l'id è valido
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID deve essere un numero positivo");
        }

        /* Uso una LEFT JOIN per collegare la tabella Task (t) con Allegato (a).
           Se l'allegato non esiste, le colonne che iniziano con 'a.' saranno NULL,
           ma il Task verrà comunque recuperato.
        */
        String selectSql = "SELECT t.*, a.filename, a.filepath, a.contentType " +
                "FROM task t " +
                "LEFT JOIN Allegato a ON t.id = a.task_id " +
                "WHERE t.id = ?";

        Task task = null;

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(selectSql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    task = new Task();

                    // --- Mappatura dati Task ---
                    task.setId(rs.getInt("id"));
                    task.setTitolo(rs.getString("titolo"));
                    task.setDataCreazione(rs.getDate("data_creazione"));
                    task.setDataDiScadenza(rs.getDate("data_scadenza"));
                    task.setIstruzioni(rs.getString("istruzioni"));


                    // Conversione Enum
                    String statoStr = rs.getString("stato");
                    String prioritaStr = rs.getString("priorita");
                    if (statoStr != null && prioritaStr != null) {
                        task.setStato(Tipi.stato.valueOf(statoStr));
                        task.setPriorita(Tipi.priorita.valueOf(prioritaStr));
                    }

                    task.setSupervisore(rs.getString("supervisore"));
                    task.setDipendente(rs.getString("dipendente"));

                    // --- Mappatura dati Allegato ---
                    /* Controllo se esiste un allegato verificando se la sua chiave primaria
                       (o una colonna not null) è diversa da null nel ResultSet.
                    */
                    String filename = rs.getString("filename");

                    if (filename != null) {
                        Allegato allegato = new Allegato();
                        allegato.setFilename(filename);
                        allegato.setFilePath(rs.getString("filepath"));
                        allegato.setContentType(rs.getString("contentType"));
                        // Imposto il task_id (che è uguale all'id del task che ho già)
                        allegato.setTaskId(task.getId());

                        // Aggiungo l'allegato al task
                        task.setAllegato(allegato);
                    }
                }
            }

        } catch (SQLException e) {
            throw new Exception("Errore durante il recupero del task con ID: " + id, e);
        }

        return task;
    }

    //Serve per la pagina principale, qui è inutile caricarsi gli allegati
    @Override
    public List<Task> findByUtente(Utente utente) throws Exception {
        // Query per trovare i task assegnati al dipendente specifico
        if (utente == null){
            throw new IllegalArgumentException("Utente non valido");
        }
        if (utente.getRuolo() == Tipi.ruolo.GESTORE){
            throw new IllegalArgumentException("Privilegi utente non validi");
        }

        String selectSql;
        if (utente.getRuolo() == Tipi.ruolo.DIPENDENTE){
            selectSql = "SELECT * FROM task WHERE dipendente = ?";
        }
        else if (utente.getRuolo() == Tipi.ruolo.SUPERVISORE){

            selectSql = "SELECT * FROM task WHERE supervisore = ?";
        }
        else{
            throw new IllegalArgumentException("Utente non supportato");
        }

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(selectSql)) {

            // Imposto la matricola dell'utente come parametro della query
            ps.setString(1, utente.getMatricola());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setTitolo(rs.getString("titolo"));
                    task.setDataCreazione(rs.getDate("dataDiCreazione"));
                    task.setDataDiScadenza(rs.getDate("dataDiScadenza"));
                    task.setIstruzioni(rs.getString("istruzioni"));

                    // Conversione da String ad Enum
                    String statoStr = rs.getString("stato");
                    String prioritaStr = rs.getString("priorita");

                    if (statoStr != null && prioritaStr != null) {
                        task.setStato(Tipi.stato.valueOf(statoStr));
                        task.setPriorita(Tipi.priorita.valueOf(prioritaStr));
                    }

                    task.setSupervisore(rs.getString("supervisore"));
                    task.setDipendente(rs.getString("dipendente"));

                    tasks.add(task);
                }
            }

        } catch (SQLException e) {
            throw new Exception("Errore durante la ricerca dei task per l'utente: " + utente.getMatricola(), e);
        }

        return tasks;
    }

    @Override
    public void updateStatus(long id, Tipi.stato nuovoStato) throws Exception {

        if (id <= 0 || nuovoStato == null){
            throw new IllegalArgumentException("Parametri non validi");
        }

        String sql = "UPDATE task SET stato = ? where  id = ?";

        try (Connection con = DataSourceFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuovoStato.toString());
            ps.setLong(2, id);
            int righe = ps.executeUpdate();

            if (righe == 0){
                throw new Exception("Errore durante l'update");
            }

        }catch (SQLException e){
            throw new Exception("Errore durante il update per task: " + id, e);
        }
    }

    @Override
    public void update(Task task) throws Exception {

        if (task == null){
            throw new IllegalArgumentException("Task non può essere null");
        }
        String updateSql = "UPDATE task SET titolo = ?, dataDiCreazione = ?, dataDiScadenza = ?, istruzioni = ?, stato = ?, supervisore = ?, dipendente = ?, priorita = ? WHERE id = ?";

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSql)) {

            ps.setString(1, task.getTitolo());
            ps.setDate(2, task.getDataCreazione());
            ps.setDate(3, task.getDataDiScadenza());
            ps.setString(4, task.getIstruzioni());

            // Converto l'Enum in Stringa per l'aggiornamento
            ps.setString(5, task.getStato().name());

            ps.setString(6, task.getSupervisore());
            ps.setString(7, task.getDipendente());

            ps.setString(8, task.getPriorita().name());

            // L'ID viene usato nella clausola WHERE come ultimo parametro
            ps.setLong(9, task.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Errore durante l'aggiornamento del task con ID: " + task.getId(), e);
        }
    }

    @Override
    public void delete(long id) throws Exception {

        if (id <= 0){
            throw new IllegalArgumentException("Id non può essere negativo o uguale a 0");
        }

        String deleteSql = "DELETE FROM task WHERE id = ?";

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Errore durante l'eliminazione del task con ID: " + id, e);
        }

    }
}
