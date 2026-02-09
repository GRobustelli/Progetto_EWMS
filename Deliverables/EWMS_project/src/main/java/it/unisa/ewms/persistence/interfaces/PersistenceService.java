package it.unisa.ewms.persistence.interfaces;

import it.unisa.ewms.persistence.PersistenceManagement.NotificaDAO;
import it.unisa.ewms.persistence.PersistenceManagement.TaskDAO;
import it.unisa.ewms.persistence.PersistenceManagement.UtenteDAO;

public interface PersistenceService {

    public UtenteDAO getUtenteDAO();

    public NotificaDAO getNotificaDAO();

    public TaskDAO getTaskDAO();

}
