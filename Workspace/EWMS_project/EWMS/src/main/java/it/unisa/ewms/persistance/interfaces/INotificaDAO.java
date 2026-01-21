package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Notifica;
import it.unisa.ewms.persistance.beans.Utente;

import java.sql.SQLException;
import java.util.List;

public interface INotificaDAO {
    void create(Notifica notifica) throws SQLException;
    List<Notifica> getByUtente(Utente utente) throws SQLException;

}
