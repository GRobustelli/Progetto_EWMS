package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.ClassiDAO.NotificaDAO;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;

public interface PersistenceService {

    public UtenteDAO getUtenteDAO();

    public NotificaDAO getNotificaDAO();

    public TaskDAO getTaskDAO();

}
