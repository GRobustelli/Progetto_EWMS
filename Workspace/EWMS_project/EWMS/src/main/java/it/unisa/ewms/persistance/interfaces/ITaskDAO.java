package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Task;
import it.unisa.ewms.persistance.beans.Utente;

import java.util.List;

public interface ITaskDAO {
    public void create(Task task) throws Exception;
    public Task findById(int id) throws Exception;
    public List<Task> findByUtente(Utente utente) throws Exception;
    public void update(Task task) throws Exception;
    public void delete(int id) throws Exception;

}
