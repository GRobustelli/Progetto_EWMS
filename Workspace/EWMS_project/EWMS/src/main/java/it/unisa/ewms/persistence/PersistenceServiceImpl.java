package it.unisa.ewms.PersistenceManagement;

import it.unisa.ewms.PersistenceManagement.ClassiDAO.NotificaDAO;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.TaskDAO;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.UtenteDAO;
import it.unisa.ewms.PersistenceManagement.interfaces.PersistenceService;

public class PersistenceServiceImpl implements PersistenceService {

// Singleton Pattern
private static PersistenceServiceImpl instance;

    private PersistenceServiceImpl() {
            // Costruttore privato
        }

        public static PersistenceServiceImpl getInstance() {
            if (instance == null) {
                instance = new PersistenceServiceImpl();
            }
            return instance;
        }

        @Override
        public UtenteDAO getUtenteDAO() {
            return new UtenteDAO();
        }

        @Override
        public NotificaDAO getNotificaDAO() {
            return new NotificaDAO();
        }

        @Override
        public TaskDAO getTaskDAO() {
            return new TaskDAO();
        }
    }

