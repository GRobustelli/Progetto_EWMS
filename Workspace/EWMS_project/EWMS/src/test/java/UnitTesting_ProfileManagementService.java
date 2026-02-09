import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.UtenteDAO;
import it.unisa.ewms.PersistenceManagement.interfaces.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UnitTesting_ProfileManagementService {

    @Mock
    private PersistenceService persistenceServiceMock;

    @Mock
    private UtenteDAO utenteDAOMock;

    private ProfileManagementServiceImpl profileService;

    @BeforeEach
    void setup() {
        // Garantiamo l'invariante service <> null tramite costruttore
        profileService = new ProfileManagementServiceImpl(persistenceServiceMock);
    }

    @Test
    void testCostruttore_ConNull_LanciaException() {
        assertThrows(IllegalArgumentException.class, () -> new SessionServiceImpl(null));
    }

    @Nested
    @DisplayName("PMST_1: Classe test per metodo addAccount")
    class Test_addAccount {
        private Utente nuovoUtente;

        @BeforeEach
        void setup() {
            nuovoUtente = new Utente();
            nuovoUtente.setNome("Mario");
            nuovoUtente.setCognome("Rossi");
            nuovoUtente.setEmail("mario.rossi@azienda.it");
            nuovoUtente.setDataNasc(Date.valueOf("1984-02-18"));
            nuovoUtente.setRuolo(Tipi.ruolo.GESTORE);
        }

        @Test
        @DisplayName("TF1: Creazione Dipendente Valido -> Successo (Chiama createDipendente)")
        void testAddAccount_Dipendente_Success() throws Exception {
            // 1. SETUP DATI (Input Test Frame)

            String password = "GoodPwd12!";

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // 3. ESECUZIONE
            profileService.addAccount(nuovoUtente, password);

            verify(utenteDAOMock).createUtente(
                    eq(nuovoUtente),
                    argThat(hashRicevuto -> {
                        return BCrypt.checkpw("GoodPwd12!", hashRicevuto);
                    }));

            // Verifica di sicurezza: NON deve aver chiamato createSupervisore o createUtente
            verify(utenteDAOMock, never()).createSupervisore(any(), anyString());
            verify(utenteDAOMock, never()).createDipendente(any(), anyString());
        }

        @Test
        @DisplayName("TF: Utente null -> Lancia IllegalArgumentException")
        void testAddAccount_UtenteNull() {
            // 1. INPUT
            Utente utenteNull = null;
            String password = "GoodPwd12!";


            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(utenteNull, password);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Email null -> Lancia IllegalArgumentException")
        void testAddAccount_EmailNull() {
            // 1. SETUP SPECIFICO

            nuovoUtente.setEmail(null);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Email con dominio/formato non valido -> Lancia IllegalArgumentException")
        void testAddAccount_EmailFormatoNonValido() {
            // 1. SETUP SPECIFICO
            nuovoUtente.setEmail("mariorossi@hotmail.com");

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Nome null -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_NomeNull() {
            // 1. SETUP SPECIFICO
            // Impostiamo il nome a null (o stringa vuota) violando la precondizione
            nuovoUtente.setNome(null);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO
            // Ci aspettiamo un'eccezione perché il nome è obbligatorio
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            // Il DB non deve essere toccato
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Nome vuoto (Empty String) -> Lancia IllegalArgumentException")
        void testAddAccount_NomeVuoto() {
            // 1. SETUP SPECIFICO
            // Impostiamo una stringa vuota. Questo viola la condizione "size() > 0"
            nuovoUtente.setNome("");

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO
            // Ci aspettiamo l'eccezione perché la lunghezza del nome è 0
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            // Fondamentale: il service deve validare la lunghezza PRIMA di chiamare il DAO.
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Nome troppo lungo (>50 caratteri) -> Lancia IllegalArgumentException")
        void testAddAccount_NomeTroppoLungo() {
            // 1. SETUP SPECIFICO

            String nomeLungo = "a".repeat(51);
            nuovoUtente.setNome(nomeLungo);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Cognome null -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_CognomeNull() {
            // 1. SETUP SPECIFICO
            // Impostiamo il nome a null (o stringa vuota) violando la precondizione
            nuovoUtente.setCognome(null);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Cognome vuoto (Empty String) -> Lancia IllegalArgumentException")
        void testAddAccount_CognomeVuoto() {
            // 1. SETUP SPECIFICO

            nuovoUtente.setCognome("");

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Cognome troppo lungo (>50 caratteri) -> Lancia IllegalArgumentException")
        void testAddAccount_CognomeTroppoLungo() {
            // 1. SETUP SPECIFICO

            String cognomeLungo = "a".repeat(51);
            nuovoUtente.setCognome(cognomeLungo);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: ruolo null -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_RuoloNull() {

            nuovoUtente.setRuolo(null);

            String password = "GoodPwd12!";


            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: DataDiNascita null -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_DataDiNascitaNull() {
            // 1. SETUP SPECIFICO

            nuovoUtente.setDataNasc(null);

            String password = "GoodPwd12!";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            // Il DB non deve essere toccato
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: Password null -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_PasswordNull() {
            // 1. SETUP SPECIFICO


            String password = null;

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            // Il DB non deve essere toccato
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF: BadPassword -> Lancia IllegalArgumentException (Violazione Precondizione)")
        void testAddAccount_BadPassword() {
            // 1. SETUP SPECIFICO


            String password = "nonbuonapassword";

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.addAccount(nuovoUtente, password);
            });

            // 3. VERIFICA ISOLAMENTO
            // Il DB non deve essere toccato
            verifyNoInteractions(persistenceServiceMock);
        }


    }

    @Nested
    @DisplayName("PMST_2: Classe test per metodo getAccount")
    class Test_getAccount {
        private Utente nuovoUtente;

        @BeforeEach
        void setup() {
            nuovoUtente = new Utente();
            nuovoUtente.setNome("Mario");
            nuovoUtente.setCognome("Rossi");
            nuovoUtente.setEmail("mario.rossi@azienda.it");
            nuovoUtente.setDataNasc(Date.valueOf("1984-02-18"));
            nuovoUtente.setRuolo(Tipi.ruolo.GESTORE);
            nuovoUtente.setMatricola(1);
        }


        @Test
        @DisplayName("TF1: Matricola valida (>0) e presente nel DB -> Restituisce Utente")
        void testGetAccount_Presente() throws SQLException {
            // 1. SETUP INPUT
            int matricola = 123;
            nuovoUtente.setMatricola(matricola);

            // 2. CONFIGURAZIONE MOCK
            // Colleghiamo il service al DAO
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Istruiamo il DAO: quando cerchi la matricola, restituisci l'oggetto creato nel @BeforeEach
            when(utenteDAOMock.findByMatricola(nuovoUtente.getMatricola())).thenReturn(nuovoUtente);

            // 3. ESECUZIONE
            Utente risultato = profileService.getAccount(matricola);

            // 4. ORACOLO
            assertNotNull(risultato, "Il metodo deve restituire un oggetto Utente se presente nel DB.");

            // Verifichiamo che l'oggetto restituito sia esattamente quello preparato nel setup
            assertEquals(nuovoUtente, risultato, "L'utente restituito deve coincidere con quello fornito dal DAO.");

            // Verifica chiamata al DAO
            verify(utenteDAOMock).findByMatricola(matricola);

        }

        @Test
        @DisplayName("TF2: Matricola non esistente (999) -> Restituisce null")
        void testGetAccount_NonPresente() throws SQLException {
            // 1. SETUP
            int matricolaNonEsistente = 999;

            // (Nota: qui non serve configurare nuovoUtente.setMatricola perché il DB non lo troverà)

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Istruiamo il DAO: cercando questa matricola, non trovi nulla
            when(utenteDAOMock.findByMatricola(matricolaNonEsistente)).thenReturn(null);

            // 3. ESECUZIONE
            Utente risultato = profileService.getAccount(matricolaNonEsistente);

            // 4. ORACOLO
            assertNull(risultato, "Il metodo deve restituire null se la matricola non esiste nel DB.");

            // Verifica interazione
            verify(utenteDAOMock).findByMatricola(matricolaNonEsistente);

        }

        @Test
        @DisplayName("TF3: Matricola non valida (<= 0) -> Lancia IllegalArgumentException")
        void testGetAccount_MatricolaNonValida() {
            // 1. SETUP
            int matricolaInvalida = -1;

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                profileService.getAccount(matricolaInvalida);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }


    }

    @Nested
    @DisplayName("PMST_3: Classe test per metodo getAccount")
    class Test_deleteAccount {


        private Utente nuovoUtente;

        @BeforeEach
        void setup() {
            nuovoUtente = new Utente();
            nuovoUtente.setNome("Mario");
            nuovoUtente.setCognome("Rossi");
            nuovoUtente.setEmail("mario.rossi@azienda.it");
            nuovoUtente.setDataNasc(Date.valueOf("1984-02-18"));
            nuovoUtente.setRuolo(Tipi.ruolo.GESTORE);
            nuovoUtente.setMatricola(1);
        }


        @Test
        @DisplayName("TF1: Utente valido (Matricola > 0) -> Chiama DAO delete")
        void testDeleteAccount_Valido() throws SQLException {
            // 1. SETUP

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // 3. ESECUZIONE
            profileService.deleteAccount(nuovoUtente);

            // 4. ORACOLO
            // Verifichiamo che il service abbia chiamato il metodo delete del DAO
            // passando l'oggetto corretto.
            verify(utenteDAOMock).delete(nuovoUtente);
        }

        @Test
        @DisplayName("TF2: Utente null -> Lancia IllegalArgumentException")
        void testDeleteAccount_UtenteNull() {


            // 2. ESECUZIONE & ORACOLO
            // Ci aspettiamo l'eccezione perché l'argomento utente è null
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.deleteAccount(null);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF3: Utente valido ma Matricola <= 0 -> Lancia IllegalArgumentException")
        void testDeleteAccount_MatricolaNonValida() {
            // 1. SETUP SPECIFICO

            nuovoUtente.setMatricola(0);

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                profileService.deleteAccount(nuovoUtente);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }


    }


}

