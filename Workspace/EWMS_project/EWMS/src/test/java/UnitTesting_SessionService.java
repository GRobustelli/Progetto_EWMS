import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    // MOCK: Simuliamo il factory dei servizi di persistenza
    @Mock
    private PersistenceService persistenceServiceMock;

    // MOCK: Simuliamo il DAO specifico per gli utenti
    @Mock
    private UtenteDAO utenteDAOMock;

    // SUT: System Under Test (la classe da testare)
    private SessionServiceImpl sessionService;


    @Test
    void testCostruttore_ConNull_LanciaException() {
        assertThrows(IllegalArgumentException.class, () -> new SessionServiceImpl(null));
    }

    @Nested
    @DisplayName("Classe per il test del metodo login")
    class Test_login {
        @BeforeEach
        void setup() {
            sessionService = new SessionServiceImpl(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF1: Credenziali corrette -> Successo (True)")
        void testLogin_Success() throws SQLException {
            // 1. SETUP DATI DI INPUT
            String email = "mario.rossi@azienda.it";
            String passwordInserita = "goodPWd12!";


            String hashSimulatoDalDB = BCrypt.hashpw(passwordInserita, BCrypt.gensalt());

            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            when(utenteDAOMock.recuperaPassword(email)).thenReturn(hashSimulatoDalDB);

            // 3. ESECUZIONE

            boolean risultato = sessionService.login(email, passwordInserita);

            // 4. ORACOLO
            assertTrue(risultato, "Il login deve restituire true se la password corrisponde all'hash restituito dal DAO.");
        }

        @Test
        @DisplayName("TF2: Password errata (Non corrispondente) -> Fallimento (False)")
        void testLogin_WrongPassword() throws SQLException {
            // 1. SETUP DATI
            String email = "mario.rossi@azienda.it";
            String passwordInserita = "goodPWd12!"; // La password che l'utente digita

            // TRUCCO PER IL TEST:
            // Generiamo l'hash di una password DIVERSA ("altraPassword").
            // In questo modo, quando il service farà BCrypt.checkpw("goodPWd12!", hashDiAltra),
            // il risultato sarà matematicamente false.
            String hashSalvatoNelDB = BCrypt.hashpw("WRONG_PASSWORD_VALUE", BCrypt.gensalt());

            // 2. CONFIGURAZIONE MOCK
            // Collega il service al DAO mockato
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Simula che nel DB esista l'utente, ma con l'hash della password diversa
            when(utenteDAOMock.recuperaPassword(email)).thenReturn(hashSalvatoNelDB);

            // 3. ESECUZIONE
            boolean risultato = sessionService.login(email, passwordInserita);

            // 4. ORACOLO
            assertFalse(risultato, "Il login deve fallire (false) se la password inserita non corrisponde all'hash nel DB.");

            // Verifica opzionale: il DAO è stato chiamato?
            verify(utenteDAOMock).recuperaPassword(email);
        }

        @Test
        @DisplayName("TF3: Utente non trovato (Email inesistente) -> Fallimento (False)")
        void testLogin_UserNotFound() throws SQLException {
            // 1. SETUP DATI
            String email = "mario.rossi@azienda.it";
            String passwordInserita = "goodPWd12!";

            // 2. CONFIGURAZIONE MOCK
            // Colleghiamo il mock del DAO
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Simuliamo il caso "Not Found":
            when(utenteDAOMock.recuperaPassword(email)).thenReturn(null);

            // 3. ESECUZIONE
            boolean risultato = sessionService.login(email, passwordInserita);

            // 4. ORACOLO
            // Ci aspettiamo false perché il blocco 'if (actualPassword != null)' sarà saltato
            assertFalse(risultato, "Il login deve fallire se l'utente non viene trovato (DAO restituisce null).");

            // Verifica
            verify(utenteDAOMock).recuperaPassword(email);
        }

        @Test
        @DisplayName("TF4: Email null -> Lancia IllegalArgumentException")
        void testLogin_NullEmail() {
            // 1. SETUP DATI
            String email = null;
            String password = "passwordQualsiasi";

            assertThrows(IllegalArgumentException.class, () -> {
                sessionService.login(email, password);
            });

            // Se la validazione funziona, il flusso si deve interrompere subito.
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF5: Password null -> Lancia IllegalArgumentException")
        void testLogin_NullPassword() {
            // 1. SETUP DATI
            String email = "mario.rossi@azienda.it";
            String password = null;

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                sessionService.login(email, password);
            });


            verifyNoInteractions(persistenceServiceMock);
        }



    }

    @Nested
    @DisplayName("Classe test per il metodo getUtente")
    class Test_getUtente{
        @BeforeEach
        void setup() {
            sessionService = new SessionServiceImpl(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF1: Email valida, Utente presente nel DB -> Restituisce oggetto Utente")
        void testGetUtente_Found() throws SQLException {
            // 1. SETUP DATI
            String email = "mario.rossi@azienda.it";

            // Creiamo un oggetto per simulare l'oggetto chiamato dal DB (non completo solo per il testing)
            Utente utenteAtteso = new Utente();
            utenteAtteso.setEmail(email);
            utenteAtteso.setNome("Mario");
            utenteAtteso.setCognome("Rossi");


            // 2. CONFIGURAZIONE MOCK
            // Colleghiamo il service al DAO
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Istruiamo il DAO: se cerchi questa email, restituisci l'oggetto che abbiamo creato
            when(utenteDAOMock.findByEmail(email)).thenReturn(utenteAtteso);

            // 3. ESECUZIONE
            Utente risultato = sessionService.getUtente(email);

            // 4. ORACOLO
            assertNotNull(risultato, "Il metodo non dovrebbe restituire null se l'utente esiste.");

            // Verifichiamo che l'oggetto restituito sia esattamente quello fornito dal DAO
            assertEquals(utenteAtteso, risultato, "L'utente restituito deve coincidere con quello trovato dal DAO.");
            assertEquals("mario.rossi@azienda.it", risultato.getEmail());

            // Verifica interazione
            verify(utenteDAOMock).findByEmail(email);
        }

        @Test
        @DisplayName("TF2: Email valida, Utente assente nel DB -> Restituisce null")
        void testGetUtente_NotFound() throws SQLException {
            // 1. SETUP DATI
            String email = "mario.rossi@azienda.it";

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            when(utenteDAOMock.findByEmail(email)).thenReturn(null);

            // 3. ESECUZIONE
            Utente risultato = sessionService.getUtente(email);

            // 4. ORACOLO
            assertNull(risultato, "Il metodo deve restituire null se il DAO non trova l'utente.");

            // Verifica che il DAO sia stato chiamato correttamente
            verify(utenteDAOMock).findByEmail(email);
        }

        @Test
        @DisplayName("TF3: Email null -> Lancia IllegalArgumentException")
        void testGetUtente_NullEmail() {
            // 1. SETUP DATI
            String email = null;

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                sessionService.getUtente(email);
            });

            verifyNoInteractions(persistenceServiceMock);
        }

    }

}