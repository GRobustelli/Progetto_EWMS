package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;

import java.util.List;

public interface ITaskDAO {
    void create(Task task) throws Exception;
    Task findById(long id) throws Exception;
    List<Task> findByUtente(Utente utente) throws Exception;

   // void update(Task task) throws Exception;

    void updateStatus(long id, Tipi.stato nuovoStato) throws Exception;
    void delete(long id) throws Exception;

}
