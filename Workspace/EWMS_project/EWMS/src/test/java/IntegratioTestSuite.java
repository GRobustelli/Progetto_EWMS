import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class IntegratioTestSuite {
    ProfileManagementService profileManagementService;

    @BeforeAll
    static void setupDatabase() {
        // Fondamentale: forza la Factory a usare la config di test con H2
        DataSourceFactory.setPropertiesFile("test.properties");
    }

    @Nested
    @DisplayName("Classe per integrazione AccountManagement (A) con PersistenceManagement (P)")
    class IntegrationTest_AccountManagement{
        private Utente baseUtente;
        private Dipendente baseDipendente;
        private Supervisore baseSupervisore;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. IMPORTANTE: Sposta la connessione corrente sullo schema di default (PUBLIC).
                // Se rimani dentro 'ewmsDB', H2 non ti permetterà mai di cancellarlo.
                stmt.execute("SET SCHEMA PUBLIC");

                // 2. Cancella TUTTO (Tabelle, Viste, Vincoli)
                stmt.execute("DROP ALL OBJECTS");

                // 3. (Opzionale ma consigliato) Forza la cancellazione dello schema per evitare residui
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                // 4. Ora puoi ricreare tutto da zero in sicurezza
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() {
            baseUtente = new Utente();
            baseUtente.setNome("Mario");
            baseUtente.setCognome("Rossi");
            baseUtente.setEmail("mario.rossi@azienda.it");
            baseUtente.setDataNasc(Date.valueOf("1985-05-20"));
            baseUtente.setRuolo(Tipi.ruolo.GESTORE);

            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Supervisore");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("supervisore.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


            baseDipendente = new Dipendente();
            baseDipendente.setNome("Luigi");
            baseDipendente.setCognome("Rossi");
            baseDipendente.setEmail("luigi.rossi@azienda.it");
            baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            profileManagementService = new ProfileManagementServiceImpl();
        }

        @Test
        @DisplayName("addAccount: Test per la verifica del funzionamento metodo addAccount")
        void Test_addAccount() throws SQLException, ClassNotFoundException {
            // ACT

            try {
                profileManagementService.addAccount(baseUtente, "Goodpwd12!");
                profileManagementService.addAccount(baseSupervisore, "Goodpwd12!");
            } catch (EmailGiaPresenteException e) {
                Assertions.fail("Non ci dovrebbero essere email duplicate");
                throw new RuntimeException(e);
            }

            //DAO per la verifica del funzionamento e separazione dei test

            UtenteDAO udao = new  UtenteDAO();
            List<Utente> lista = udao.getAllUtente();

            boolean check= (lista.size() == 2);

            assertTrue(check, "La lista dovrebbe avere una size di 2");

        }

        @Test
        @DisplayName("getAccount: Test per la verifica del funzionamento metodo addAccount")
        void Test_getAccount() throws SQLException, ClassNotFoundException {

            // SETUP

            UtenteDAO udao = new  UtenteDAO();

            try {

                udao.createUtente(baseUtente, "Goodpwd.12");
            } catch (EmailGiaPresenteException e) {
                Assertions.fail("Non ci dovrebbero essere email duplicate");
            }

            // Recuperiamo l'utente appena inserito per avere la matricola generata e i dati attesi
            // Nota: findByEmail è più sicuro di getAll().get(0) in un DB non vuoto
            Utente utenteAtteso = udao.findByEmail(baseUtente.getEmail());

            // Verifica preliminare che il setup sia andato a buon fine
            assertNotNull(utenteAtteso, "Errore Setup: L'utente dovrebbe essere nel DB");
            int matricola = utenteAtteso.getMatricola();


            // 2. ACT & VERIFICA ECCEZIONI
            // Verifichiamo che non lanci IllegalArgumentException e catturiamo il risultato
            Utente utenteVerifica = Assertions.assertDoesNotThrow(() -> {
                return profileManagementService.getAccount(matricola);
            }, "Il metodo non deve lanciare eccezioni con una matricola valida");


            // 3. ASSERT (Verifica Campi)
            assertNotNull(utenteVerifica, "L'utente restituito non deve essere null");

            // Usiamo assertAll per verificare tutti i campi insieme
            Assertions.assertAll("Verifica corrispondenza campi Utente",
                    () -> assertEquals(utenteAtteso.getMatricola(), utenteVerifica.getMatricola(), "Matricola non corrisponde"),
                    () -> assertEquals(utenteAtteso.getEmail(), utenteVerifica.getEmail(), "Email non corrisponde"),
                    () -> assertEquals(utenteAtteso.getNome(), utenteVerifica.getNome(), "Nome non corrisponde"),
                    () -> assertEquals(utenteAtteso.getCognome(), utenteVerifica.getCognome(), "Cognome non corrisponde"),
                    // Nota: per le date assicurati che il formato (Date vs Timestamp) sia compatibile
                    () -> assertEquals(utenteAtteso.getDataNasc().toString(), utenteVerifica.getDataNasc().toString(), "Data di nascita non corrisponde")
            );
        }

        @Test
        @DisplayName("test getAllAccount: restituisce lista vuota se non ci sono utenti, restituisce lista popolata altrimenti")
        void testGetAllAccount() throws SQLException {
            // Test DB vuoto

            List<Utente> risultato = profileManagementService.getAllAccount();

            // 2. ASSERT
            assertNotNull(risultato, "La lista non deve essere null");
            assertTrue(risultato.isEmpty(), "La lista deve essere vuota se non ci sono utenti nel DB");


            // 1. ARRANGE (Popoliamo il DB di test)



            UtenteDAO dao = new UtenteDAO();

            // Inseriamo un Gestore
            try {
                dao.createUtente(baseUtente, "Pwd.1234");
                dao.createSupervisore(baseSupervisore, "Pwd.1234");

                Utente utenteAtteso = dao.findByEmail(baseSupervisore.getEmail());
                baseDipendente.setSupervisoreInfo(new Informazioni(utenteAtteso.getMatricola(), utenteAtteso.getNome(), utenteAtteso.getCognome()));

                dao.createDipendente(baseDipendente, "Pwd.123");
            } catch (EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }




              // 2. ACT
            risultato = profileManagementService.getAllAccount();

            // 3. ASSERT

            assertNotNull(risultato);
            assertEquals(3, risultato.size(), "Devono esserci esattamente 3 utenti nel sistema");

        }


    @Test
    @DisplayName("test getAllSupervisori: restituisce la lista di tutti i supervisori del sistema")
    void testGetAllSupervisori() throws SQLException, EmailGiaPresenteException {
        List<Informazioni> result = profileManagementService.getAllSupervisori();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "La lista deve essere vuota se non ci sono supervisori");

        // 1. ARRANGE
        UtenteDAO dao = new UtenteDAO();

        // Inseriamo UN Gestore (non deve apparire)
        dao.createUtente(baseUtente, "Pwd.1234");
        dao.createSupervisore(baseSupervisore, "Pwd.1234");

        // 2. ACT
        List<Informazioni> risultato = profileManagementService.getAllSupervisori();

        // 3. ASSERT
        assertNotNull(risultato);
        assertEquals(1, risultato.size(),
                "La lista deve contenere SOLO il supervisore, ignorando Gestori e Dipendenti");


        Informazioni infoSupervisore = risultato.get(0);

        assertAll("Verifica mappatura DTO Informazioni",
                () -> assertEquals(baseSupervisore.getNome(), infoSupervisore.getNome(), "Il nome nel DTO errato"),
                () -> assertEquals(baseSupervisore.getCognome(), infoSupervisore.getCognome(), "Il cognome nel DTO errato"),
                () -> assertTrue(infoSupervisore.getMatricola() > 0, "La matricola deve essere valorizzata (>0)")
        );
    }
        @Test
        @DisplayName("Cancellazione Supervisore (Cascade/Multi-tabella) -> Rimosso dal DB")

        void testDeleteAccount_Supervisore() throws SQLException, EmailGiaPresenteException {
            // 1. ARRANGE
            UtenteDAO dao = new UtenteDAO();

            // Inseriamo un Supervisore nel DB reale.
            // Questo popola sia la tabella 'utente' che la tabella 'supervisore'.
            dao.createSupervisore(baseSupervisore, "Pwd.1234");

            Utente utenteDaCancellare = dao.findByEmail(baseSupervisore.getEmail());

            Assertions.assertNotNull(utenteDaCancellare, "Setup fallito: L'utente deve esistere prima del test");
            int matricola = utenteDaCancellare.getMatricola();

            // 2. ACT

            Assertions.assertDoesNotThrow(() -> {
                profileManagementService.deleteAccount(utenteDaCancellare);
            });

            // 3. ASSERT
            // Verifichiamo che non esista più tramite il DAO
            Utente risultato = dao.findByMatricola(matricola);
            Assertions.assertNull(risultato, "L'utente dovrebbe essere stato rimosso completamente dal DB");

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM supervisore WHERE matricola = ?")) {
                ps.setInt(1, matricola);
                ResultSet rs = ps.executeQuery();
                rs.next();
                Assertions.assertEquals(0, rs.getInt(1), "Il record deve essere sparito anche dalla tabella supervisore");
            }
        }


    }
    @Nested
    @DisplayName("Classe per integrazione AccessManagement  --> AccountManagement e PersistenceManagement")
    class IntegrationTesst_AccessManagement{

    }

}






