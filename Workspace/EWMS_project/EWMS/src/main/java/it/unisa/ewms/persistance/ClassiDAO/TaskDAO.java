package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.Task;
import it.unisa.ewms.persistance.beans.Tipi;
import it.unisa.ewms.persistance.beans.Utente;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO implements ITaskDAO {
    @Override
    public void create(Task task) throws Exception {
        String insertSql = "INSERT INTO task (titolo, dataDiCreazione, dataDiScadenza, istruzioni, stato, supervisore, dipendente) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setString(1, task.getTitolo());
            ps.setDate(2, task.getDataCreazione());
            ps.setDate(3, task.getDataDiScadenza());
            ps.setString(4, task.getIstruzioni());

            // Gestione dell'Enum: salvo il nome dello stato come stringa
            ps.setString(5, task.getStato().name());

            ps.setString(6, task.getSupervisore());
            ps.setString(7, task.getDipendente());

            ps.executeUpdate();

        } catch (SQLException e) {
            // Rilancio l'eccezione wrappandola o passandola al chiamante
            throw new Exception("Errore durante l'inserimento del task nel database", e);
        }

    }

    @Override
    public Task findById(int id) throws Exception {
        String selectSql = "SELECT * FROM task WHERE id = ?";
        Task task = null;

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(selectSql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setTitolo(rs.getString("titolo"));
                    task.setDataCreazione(rs.getDate("data_creazione"));
                    task.setDataDiScadenza(rs.getDate("data_scadenza"));
                    task.setIstruzioni(rs.getString("istruzioni"));

                    // Converto la stringa del DB nel valore Enum corrispondente
                    String statoStr = rs.getString("stato");
                    if (statoStr != null) {
                        task.setStato(Tipi.stato.valueOf(statoStr));
                    }

                    task.setSupervisore(rs.getString("supervisore"));
                    task.setDipendente(rs.getString("dipendente"));
                }
            }

        } catch (SQLException e) {
            throw new Exception("Errore durante il recupero del task con ID: " + id, e);
        }

        return task;
    }

    @Override
    public List<Task> findByUtente(Utente utente) throws Exception {
        // Query per trovare i task assegnati al dipendente specifico
        String selectSql;
        if (utente.getRuolo() == Tipi.ruolo.DIPENDENTE){
            selectSql = "SELECT * FROM task WHERE dipendente = ?";
        }
        else{
            selectSql = "SELECT * FROM task WHERE supervisore = ?";
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
                    task.setDataCreazione(rs.getDate("data_creazione"));
                    task.setDataDiScadenza(rs.getDate("data_scadenza"));
                    task.setIstruzioni(rs.getString("istruzioni"));

                    // Conversione da String ad Enum
                    String statoStr = rs.getString("stato");
                    if (statoStr != null) {
                        task.setStato(Tipi.stato.valueOf(statoStr));
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
    public void update(Task task) throws Exception {
        String updateSql = "UPDATE task SET titolo = ?, dataDiCreazione = ?, dataDiScadenza = ?, istruzioni = ?, stato = ?, supervisore = ?, dipendente = ? WHERE id = ?";

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

            // L'ID viene usato nella clausola WHERE come ultimo parametro
            ps.setInt(8, task.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Errore durante l'aggiornamento del task con ID: " + task.getId(), e);
        }
    }

    @Override
    public void delete(int id) throws Exception {

        String deleteSql = "DELETE FROM task WHERE id = ?";

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Errore durante l'eliminazione del task con ID: " + id, e);
        }

    }
}
