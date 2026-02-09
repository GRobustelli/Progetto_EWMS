package it.unisa.ewms.PersistenceManagement.interfaces;

import it.unisa.ewms.PersistenceManagement.ClassiDAO.NotificaDAO;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.TaskDAO;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.UtenteDAO;

public interface PersistenceService {

    public UtenteDAO getUtenteDAO();

    public NotificaDAO getNotificaDAO();

    public TaskDAO getTaskDAO();

}
