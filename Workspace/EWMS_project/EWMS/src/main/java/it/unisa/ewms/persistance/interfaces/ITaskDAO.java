package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Task;
import it.unisa.ewms.persistance.beans.Tipi;
import it.unisa.ewms.persistance.beans.Utente;

import java.sql.SQLException;
import java.util.List;

public interface ITaskDAO {
    void create(Task task) throws Exception;
    Task findById(int id) throws Exception;
    List<Task> findByUtente(Utente utente) throws Exception;
    void update(Task task) throws Exception;
    void updateStatus(long id, Tipi.stato nuovoStato) throws Exception;
    void delete(int id) throws Exception;

}
