package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.Notifica;
import it.unisa.ewms.persistance.beans.Utente;
import it.unisa.ewms.persistance.interfaces.INotificaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificaDAO implements INotificaDAO {

    @Override
    public void create(Notifica notifica) {
        if (notifica == null) {
            throw new IllegalArgumentException("L'oggetto notifica non può essere null.");
        }

        String insertSql = "INSERT INTO notifica (task_id,messaggio, sender, receiver) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataSourceFactory.getConnection();

             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setInt(1, notifica.getTaskId());
            ps.setString(2,notifica.getMessaggio());
            ps.setString(3, notifica.getSender());
            ps.setString(4, notifica.getReceiver());

            ps.executeUpdate();

            // La Post-condizione (Notifica salvata) è soddisfatta se non vengono lanciate eccezioni

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Notifica> getByUtente(Utente utente) throws SQLException {
        if (utente == null) {
            throw new IllegalArgumentException("L'utente non può essere null.");
        }

        // Implementazione della logica per la Post-condizione:
        // Seleziono le notifiche dove il receiver corrisponde all'email dell'utente
        String selectSql = "SELECT * FROM notifica WHERE receiver = ?";
        List<Notifica> notifiche = new ArrayList<>();

        try (Connection connection = DataSourceFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(selectSql)) {

            ps.setString(1, utente.getMatricola());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notifica notifica = new Notifica();
                    notifica.setId(rs.getInt("id"));
                    // Assumo che la colonna nel DB sia 'task_id' (snake_case)
                    notifica.setTaskId(rs.getInt("task_id"));
                    notifica.setSender(rs.getString("sender"));
                    notifica.setReceiver(rs.getString("receiver"));
                    notifica.setVista(rs.getBoolean("vista"));
                    notifiche.add(notifica);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Errore durante il recupero delle notifiche per l'utente: " + utente.getEmail(), e);
        }

        return notifiche;
    }
}
