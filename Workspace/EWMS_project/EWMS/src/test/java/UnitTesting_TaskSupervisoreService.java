import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskCommonServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTesting_TaskSupervisoreService {

    @Mock
    private PersistenceService persistenceServiceMock;

    @Mock
    private TaskDAO taskDAOMock;

    private TaskSupervisoreServiceImpl taskSupervisoreService;


    @BeforeEach
    void setup() {
        // Garantiamo l'invariante service <> null tramite costruttore
        taskSupervisoreService= new TaskSupervisoreServiceImpl(persistenceServiceMock);
    }



    @Test
    void testCostruttore_ConNull_LanciaException() {
        assertThrows(IllegalArgumentException.class, () -> new TaskSupervisoreServiceImpl(null));
    }

    @Nested
    @DisplayName("Classe per il test del metodo")
    class Test_getAllTaskSup{
        private Supervisore supervisore;
        private List<Task> listaTaskAttesa;

        @BeforeEach
        void setup() {
            // 1. SETUP DATI COMUNI
            // Creiamo un supervisore che rispetta le pre-condizioni
            supervisore = new Supervisore();
            supervisore.setMatricola(10); // Matricola > 0
            supervisore.setRuolo(Tipi.ruolo.SUPERVISORE); // Ruolo corretto

            // Creiamo una lista di task simulata (non vuota)
            Task t1 = new Task();
            t1.setId(1);
            t1.setTitolo("Task Test");

            listaTaskAttesa = new ArrayList<>();
            listaTaskAttesa.add(t1);
        }


        @Test
        @DisplayName("TF1: Supervisore valido e Tasks presenti -> Restituisce lista non vuota")
        void testGetAllTaskSup_Presenti() throws Exception {
            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            // Istruiamo il DAO a restituire la lista creata nel setup
            when(taskDAOMock.findByUtente(supervisore)).thenReturn(listaTaskAttesa);

            // 3. ESECUZIONE
            List<Task> risultato = taskSupervisoreService.getAllTaskSup(supervisore);

            // 4. ORACOLO
            Assertions.assertNotNull(risultato, "Il metodo non deve restituire null");
            Assertions.assertFalse(risultato.isEmpty(), "La lista restituita non deve essere vuota");
            Assertions.assertEquals(listaTaskAttesa.size(), risultato.size(), "Il numero di task deve coincidere");
            Assertions.assertEquals(listaTaskAttesa, risultato, "La lista restituita deve essere quella fornita dal DAO");

            // 5. VERIFICA INTERAZIONI
            verify(taskDAOMock).findByUtente(supervisore);
        }

        @Test
        @DisplayName("TF2: Supervisore valido ma nessun Task -> Restituisce lista vuota")
        void testGetAllTaskSup_ListaVuota() throws Exception {
            // Mock: Il DAO restituisce una lista vuota
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            when(taskDAOMock.findByUtente(supervisore)).thenReturn(new ArrayList<>());

            // Esecuzione
            List<Task> risultato = taskSupervisoreService.getAllTaskSup(supervisore);

            // Oracolo
            Assertions.assertNotNull(risultato, "Il metodo non deve restituire null anche se non ci sono task");
            Assertions.assertTrue(risultato.isEmpty(), "La lista deve essere vuota");

            // Verifica
            verify(taskDAOMock).findByUtente(supervisore);
        }

        @Test
        @DisplayName("TF3: Supervisore null -> Lancia IllegalArgumentException")
        void testGetAllTaskSup_UtenteNull() {
            // 1. INPUT
            Supervisore supervisoreNull = null;

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.getAllTaskSup(supervisoreNull);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF4: Utente con ruolo errato (DIPENDENTE) -> Lancia IllegalArgumentException")
        void testGetAllTaskSup_RuoloErrato() {
            // 1. SETUP
            // Impostiamo un ruolo non valido per questa operazione
            supervisore.setRuolo(Tipi.ruolo.DIPENDENTE);

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.getAllTaskSup(supervisore);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF5: Matricola non valida (<= 0) -> Lancia IllegalArgumentException")
        void testGetAllTaskSup_MatricolaNonValida() {
            // 1. SETUP
            // Impostiamo una matricola non valida (violazione pre-condizione)
            supervisore.setMatricola(0);

            // 2. ESECUZIONE & ORACOLO
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.getAllTaskSup(supervisore);
            });

            // 3. VERIFICA ISOLAMENTO
            // Il DB non deve essere interrogato se l'input è invalido
            verifyNoInteractions(persistenceServiceMock);
        }



    }

    @Nested
    @DisplayName("")
    class Test_createTask{

        private String titolo;
        private String istruzioni;
        private int matricolaDip;
        private int matricolaSup;
        private Date dataCreazione;
        private Date dataScadenza;
        private Allegato allegato;
        @Mock
        private UtenteDAO utenteDAOMock;

            @BeforeEach
            void setup()
            {
                titolo = "Sviluppo Backend";
                istruzioni = "Implementare le API REST per il login. (Lungh > 10)";
                matricolaDip = 5;
                matricolaSup = 10;
                dataCreazione = Date.valueOf("2026-01-05");
                dataScadenza = Date.valueOf("2026-01-07");
                allegato = new Allegato();
            }

            @Test
            @DisplayName("TF1: Input validi, Date corrette e Utenti esistenti -> Chiama create() del DAO")
            void testCreateTask_HappyPath() throws Exception {

                // 1. ARRANGE (SETUP DATI)
                // Supponiamo un allegato vuoto o mockato

                // 2. CONFIGURAZIONE MOCK
                // Il service deve ottenere i DAO

                Supervisore supervisore = new Supervisore();
                supervisore.setRuolo(Tipi.ruolo.SUPERVISORE);

                Dipendente dipendente = new Dipendente();
                dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

                when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
                when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);
                when(utenteDAOMock.findByMatricola(matricolaDip)).thenReturn(dipendente);
                when(utenteDAOMock.findByMatricola(matricolaSup)).thenReturn(supervisore);
                // 3. ACT (ESECUZIONE)
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaSup,
                        matricolaDip,
                        Tipi.priorita.ALTA,
                        allegato
                );

                // 4. ASSERT (ORACOLO)
                // Usiamo ArgumentCaptor per catturare l'oggetto Task passato al DAO.
                // Il metodo è void e crea l'oggetto internamente.
                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

                verify(taskDAOMock).create(taskCaptor.capture());

                Task taskCreato = taskCaptor.getValue();

                // Verifichiamo che i campi siano stati settati correttamente
                Assertions.assertEquals(titolo, taskCreato.getTitolo());
                Assertions.assertEquals(istruzioni, taskCreato.getIstruzioni());
                Assertions.assertEquals(matricolaDip, taskCreato.getDipendente());
                Assertions.assertEquals(matricolaSup, taskCreato.getSupervisore());
                Assertions.assertEquals(dataScadenza, taskCreato.getDataDiScadenza());
                Assertions.assertEquals(Tipi.stato.DA_COMPLETARE, taskCreato.getStato());
            }

        @Test
        @DisplayName("TF2: Titolo Null -> Lancia IllegalArgumentException")
        void testCreateTask_TitoloNull() {
            // 1. ARRANGE SPECIFICO: Invalidiamo solo il titolo
            this.titolo = null;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo, dataCreazione, dataScadenza, istruzioni,
                        Tipi.stato.DA_COMPLETARE, matricolaDip, matricolaSup,
                        Tipi.priorita.ALTA, allegato
                );
            });

            // 3. VERIFY: Il DAO non deve mai essere chiamato
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF3: Titolo Vuoto -> Lancia IllegalArgumentException")
        void testCreateTask_TitoloVuoto() {
            // 1. ARRANGE SPECIFICO: Invalidiamo solo il titolo
            String titolovuoto = "";

            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);
            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolovuoto, dataCreazione, dataScadenza, istruzioni,
                        Tipi.stato.DA_COMPLETARE, matricolaDip, matricolaSup,
                        Tipi.priorita.ALTA, allegato
                );
            });

            // 3. VERIFY: Il DAO non deve mai essere chiamato
            verifyNoInteractions(taskDAOMock);
        }


        @Test
        @DisplayName("TF4: Istruzioni troppo brevi (< 10 caratteri) -> Lancia IllegalArgumentException")
        void testCreateTask_IstruzioniTroppoBrevi() {
            // 1. ARRANGE SPECIFICO
            // "Breve" ha 5 caratteri, violando la pre-condizione size >= 10
            this.istruzioni = "Breve";

            // 2. ACT & ASSERT
            // Ci aspettiamo che il service lanci IllegalArgumentException
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            // Garantiamo che non ci sia alcuna interazione con il persistence layer
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF5: Data Scadenza precedente alla Data Creazione -> Lancia IllegalArgumentException")
        void testCreateTask_DateIncoerenti() {
            // 1. ARRANGE SPECIFICO

            this.dataScadenza = Date.valueOf("2026-01-01");

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF6: Matricola Supervisore non valida (< 0) -> Lancia IllegalArgumentException")
        void testCreateTask_MatricolaSupNonValida() {
            // 1. ARRANGE SPECIFICO

            this.matricolaSup = 0;

            // 2. ACT & ASSERT

            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF7: Matricola Dipendente non valida (<= 0) -> Lancia IllegalArgumentException")
        void testCreateTask_MatricolaDipNonValida() {
            // 1. ARRANGE SPECIFICO

            this.matricolaDip = 0;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF8: Dipendente non esistente nel DB -> Lancia IllegalArgumentException")
        void testCreateTask_DipendenteNonEsistente() throws Exception {
            // 1. ARRANGE
            // Configurazione Mock: Il Persistence restituisce il DAO Utente
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            // Simuliamo lo scenario:
            // Il Dipendente NON viene trovato (ritorna null)
            //when(utenteDAOMock.findByMatricola(matricolaDip)).thenReturn(null);

            // Il Supervisore invece esiste (così siamo sicuri che l'errore sia causato dal dipendente)
            // Nota: Creiamo un mock o un oggetto vuoto per il supervisore
            when(utenteDAOMock.findByMatricola(matricolaSup)).thenReturn(new Supervisore());

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            // Verifica cruciale: il metodo create del TaskDAO NON deve essere mai chiamato
            verify(taskDAOMock, never()).create(any());
        }

        @Test
        @DisplayName("TF9: Supervisore non esistente nel DB -> Lancia IllegalArgumentException")
        void testCreateTask_SupervisoreNonEsistente() throws Exception {
            // 1. ARRANGE
            // Configurazione Mock per i DAO
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);




            // Simuliamo che il Supervisore NON ESISTA (ritorna null)
            when(utenteDAOMock.findByMatricola(matricolaSup)).thenReturn(null);

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY

            verify(taskDAOMock, never()).create(any());
        }


        @Test
        @DisplayName("TF10: Stato Null -> Lancia IllegalArgumentException")
        void testCreateTask_StatoNull() {
            // 1. ARRANGE SPECIFICO

            Tipi.stato statoNull = null;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        statoNull, // Passiamo null
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY

            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF11: Priorità Null -> Lancia IllegalArgumentException")
        void testCreateTask_PrioritaNull() {
            // 1. ARRANGE
            // Passiamo esplicitamente null per il parametro priorità
            Tipi.priorita prioritaNull = null;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        prioritaNull, // Input non valido
                        allegato
                );
            });

            // 3. VERIFY
            // Il DB non deve essere interrogato
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF12: Istruzioni Null -> Lancia IllegalArgumentException")
        void testCreateTask_IstruzioniNull() {
            // 1. ARRANGE
            String istruzioniNull = null;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenza,
                        istruzioniNull, // Passiamo null
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            // Verifica fail-fast: nessun accesso al DB
            verifyNoInteractions(persistenceServiceMock);
        }

        @Test
        @DisplayName("TF13: Data Scadenza Null -> Lancia IllegalArgumentException")
        void testCreateTask_DataScadenzaNull() {
            // 1. ARRANGE
            Date dataScadenzaNull = null;

            // 2. ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.createTask(
                        titolo,
                        dataCreazione,
                        dataScadenzaNull, // Passiamo null
                        istruzioni,
                        Tipi.stato.DA_COMPLETARE,
                        matricolaDip,
                        matricolaSup,
                        Tipi.priorita.ALTA,
                        allegato
                );
            });

            // 3. VERIFY
            // Il DB non deve essere interrogato
            verifyNoInteractions(persistenceServiceMock);
        }

        }

    @Nested
    @DisplayName("Classe per il testing del metodo getAllDipendenteInfo")
    class Test_getAllDipendenteInfo {

        @Mock
        private UtenteDAO utenteDAOMock;

        @Test
        @DisplayName("TF1: Matricola valida e dipendenti presenti -> Restituisce lista popolata")
        void testGetAllDipendenteInfo_Successo() throws SQLException {
            // 1. SETUP
            int supervisoreMat = 10;
            List<Informazioni> listaSimulata = new ArrayList<>();
            listaSimulata.add(new Informazioni());

            // 2. CONFIGURAZIONE MOCK

            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);
            when(utenteDAOMock.getAllDipendentiInfo(supervisoreMat)).thenReturn(listaSimulata);

            // 3. ESECUZIONE
            List<Informazioni> risultato = taskSupervisoreService.getAllDipendentiInfo(supervisoreMat);

            // 4. ORACOLO
            Assertions.assertNotNull(risultato, "Il risultato non deve essere null");
            Assertions.assertFalse(risultato.isEmpty(), "La lista non deve essere vuota");
            Assertions.assertEquals(listaSimulata, risultato, "La lista restituita deve coincidere con quella del DAO");

            // 5. VERIFICA INTERAZIONE
            verify(utenteDAOMock).getAllDipendentiInfo(supervisoreMat);
        }

        @Test
        @DisplayName("TF2: Matricola valida ma nessun dipendente trovato -> Restituisce lista vuota")
        void testGetAllDipendenteInfo_ListaVuota() throws SQLException {
            // 1. SETUP
            int supervisoreMat = 10;

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getUtenteDAO()).thenReturn(utenteDAOMock);

            when(utenteDAOMock.getAllDipendentiInfo(supervisoreMat)).thenReturn(new ArrayList<>());

            // 3. ESECUZIONE
            List<Informazioni> risultato = taskSupervisoreService.getAllDipendentiInfo(supervisoreMat);

            // 4. ORACOLO
            Assertions.assertNotNull(risultato, "Il metodo non deve restituire null");
            Assertions.assertTrue(risultato.isEmpty(), "La lista restituita deve essere vuota");

            // 5. VERIFICA INTERAZIONE
            verify(utenteDAOMock).getAllDipendentiInfo(supervisoreMat);

        }

        @Test
        @DisplayName("TF3: Matricola non valida (<= 0) -> Lancia IllegalArgumentException")
        void testGetAllDipendenteInfo_MatricolaNonValida() {
            // 1. SETUP
            int matricolaInvalida = -5;

            // 2. ESECUZIONE & ORACOLO
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.getAllDipendentiInfo(matricolaInvalida);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }
    }


    @Nested
    @DisplayName("Classe per il testing del metodo deleteTask")
    class Test_deleteTask {

        @Test
        @DisplayName("TF1: TaskID valido (> 0) e Task presente -> Chiama delete e restituisce true")
        void testDeleteTask_Successo() throws Exception {
            // 1. SETUP
            int taskId = 50; // ID Valido

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // 3. ESECUZIONE
            boolean risultato = taskSupervisoreService.deleteTask(taskId);

            // 4. ORACOLO
            Assertions.assertTrue(risultato, "Il metodo deve restituire true dopo una cancellazione avvenuta");

            // 5. VERIFICA INTERAZIONE
            verify(taskDAOMock).delete(taskId);
        }

        @Test
        @DisplayName("TF2: TaskID valido ma Task non presente (DAO lancia SQLException) -> Restituisce false")
        void testDeleteTask_TaskNonTrovato() throws Exception {
            // 1. SETUP
            int taskIdInesistente = 99;

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            doThrow(new SQLException("Nessun task con questo id")).when(taskDAOMock).delete(taskIdInesistente);

            // 3. ESECUZIONE

            boolean risultato = taskSupervisoreService.deleteTask(taskIdInesistente);

            // 4. ORACOLO
            Assertions.assertFalse(risultato, "Il metodo deve restituire false se il DAO lancia eccezione per task non trovato");

            // 5. VERIFICA INTERAZIONE
            verify(taskDAOMock).delete(taskIdInesistente);
        }

        @Test
        @DisplayName("TF3: TaskID non valido (<= 0) -> Lancia IllegalArgumentException")
        void testDeleteTask_IdNonValido() {
            // 1. SETUP
            int taskIdInvalido = -1; // Oppure 0

            // 2. ESECUZIONE & ORACOLO
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskSupervisoreService.deleteTask(taskIdInvalido);
            });

            // 3. VERIFICA ISOLAMENTO
            verifyNoInteractions(persistenceServiceMock);
        }
    }






}


