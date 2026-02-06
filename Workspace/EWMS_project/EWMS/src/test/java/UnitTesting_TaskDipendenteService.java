import it.unisa.ewms.application.TaskManagement.TaskDipendenteServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTesting_TaskDipendenteService {


        @Mock
        private PersistenceService persistenceServiceMock;

        @Mock
        private TaskDAO taskDAOMock;

        @InjectMocks
        private TaskDipendenteServiceImpl taskDipendenteService;

        private Dipendente dipendente;

        @BeforeEach
        void setUp() {
            // Inizializzazione di un dipendente valido per i test frame positivi
            dipendente = new Dipendente();
            dipendente.setMatricola(5);
            dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);
        }

        @Nested
        @DisplayName("Test del metodo getAllTaskDip")
        class Test_getAllTaskDip {

            @Test
            @DisplayName("TF1: Dipendente valido e task presenti -> Ritorna lista popolata")
            void testGetAllTaskDip_Successo() throws Exception {
                // 1. ARRANGE
                List<Task> listaTaskSimulata = new ArrayList<>();
                listaTaskSimulata.add(new Task());
                listaTaskSimulata.add(new Task());

                // Configurazione del comportamento dei mock
                when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
                when(taskDAOMock.findByUtente(dipendente)).thenReturn(listaTaskSimulata);

                // 2. ACT
                List<Task> risultato = taskDipendenteService.getAllTaskDip(dipendente);

                // 3. ASSERT (ORACOLO)
                Assertions.assertNotNull(risultato);
                Assertions.assertEquals(2, risultato.size());
                Assertions.assertEquals(listaTaskSimulata, risultato);

                // 4. VERIFY
                verify(taskDAOMock).findByUtente(dipendente);
            }

            @Test
            @DisplayName("TF2: Dipendente valido ma nessun task nel DB -> Restituisce lista vuota")
            void testGetAllTaskDip_ListaVuota() throws Exception {
                // 1. SETUP


                // 2. CONFIGURAZIONE MOCK
                when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);


                when(taskDAOMock.findByUtente(dipendente)).thenReturn(new ArrayList<>());

                // 3. ESECUZIONE
                List<Task> risultato = taskDipendenteService.getAllTaskDip(dipendente);

                // 4. ORACOLO
                Assertions.assertNotNull(risultato, "Il risultato non deve mai essere null");
                Assertions.assertTrue(risultato.isEmpty(), "La lista deve essere vuota se non ci sono task");

                // 5. VERIFICA INTERAZIONE

                verify(taskDAOMock).findByUtente(dipendente);
            }

            @Test
            @DisplayName("TF3: Dipendente Null -> Lancia IllegalArgumentException")
            void testGetAllTaskDip_DipendenteNull() {
                // 1. SETUP
                Dipendente dipendenteNull = null;

                // 2. ESECUZIONE & ORACOLO
                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    taskDipendenteService.getAllTaskDip(dipendenteNull);
                });

                // 3. VERIFICA ISOLAMENTO

                verifyNoInteractions(persistenceServiceMock);
            }

            @Test
            @DisplayName("TF4: Utente con ruolo errato (SUPERVISORE) -> Lancia IllegalArgumentException")
            void testGetAllTaskDip_RuoloErrato() {
                // 1. SETUP

                dipendente.setRuolo(Tipi.ruolo.SUPERVISORE);

                // 2. ESECUZIONE & ORACOLO
                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    taskDipendenteService.getAllTaskDip(dipendente);
                });

                // 3. VERIFICA ISOLAMENTO

                verifyNoInteractions(persistenceServiceMock);
            }

            @Test
            @DisplayName("TF5: Matricola Dipendente non valida (<= 0) -> Lancia IllegalArgumentException")
            void testGetAllTaskDip_MatricolaNonValida() {
                // 1. SETUP

                dipendente.setMatricola(0);

                // 2. ESECUZIONE & ORACOLO
                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    taskDipendenteService.getAllTaskDip(dipendente);
                });

                // 3. VERIFICA ISOLAMENTO

                verifyNoInteractions(persistenceServiceMock);
            }
        }

    @Nested
    @DisplayName("Test del metodo inizializzaTask")
    class Test_inizializzaTask {

            private Task taskSimulato;
            @BeforeEach
            void setUp() {
                // 1. SETUP
                long taskId = 2L;

                // Creiamo un task simulato campi necessarri per inizializzatask
                taskSimulato = new Task();
                taskSimulato.setId(taskId);
                taskSimulato.setStato(Tipi.stato.DA_COMPLETARE);
            }


        @Test
        @DisplayName("TF1: TaskID valido, Task presente e stato 'DA_COMPLETARE' -> Aggiorna a 'IN_ESECUZIONE' e ritorna true")
        void testInizializzaTask_Successo() throws Exception {
            // 1. SETUP
            int taskId = 20;

            // Creiamo un task simulato che si trova nello stato iniziale corretto
            Task taskSimulato = new Task();
            taskSimulato.setId(taskId);
            taskSimulato.setStato(Tipi.stato.DA_COMPLETARE);

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Simuliamo il ritrovamento del task nel DB
            when(taskDAOMock.findById(taskId)).thenReturn(taskSimulato);

            // Il metodo updateStatus è void, quindi non c'è bisogno di configurare il return (doNothing è default)

            // 3. ESECUZIONE
            boolean risultato = taskDipendenteService.inizializzaTask(taskId);

            // 4. ORACOLO
            Assertions.assertTrue(risultato, "Il metodo deve restituire true dopo l'aggiornamento riuscito");

            // 5. VERIFICA INTERAZIONE
            // Verifichiamo che sia stato chiamato updateStatus con lo stato corretto
            verify(taskDAOMock).updateStatus(taskId, Tipi.stato.IN_ESECUZIONE);
        }

        @Test
        @DisplayName("TF2: TaskID valido e stato 'IN_SOSPENSIONE' -> Aggiorna a 'IN_ESECUZIONE' e ritorna true")
        void testInizializzaTask_StatoInSospensione() throws Exception {
            // 1. ARRANGE

            taskSimulato.setStato(Tipi.stato.IN_SOSPENSIONE);

            // Configurazione dei Mock
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Il service deve prima recuperare il task per verificare la pre-condizione sullo stato
            when(taskDAOMock.findById(taskSimulato.getId())).thenReturn(taskSimulato);

            // 2. ACT
            boolean risultato = taskDipendenteService.inizializzaTask(taskSimulato.getId());

            // 3. ASSERT
            Assertions.assertTrue(risultato, "Il metodo deve restituire true per task esistenti e non completati");

            // 4. VERIFY
            // Verifichiamo che venga invocato l'update verso lo stato corretto
            verify(taskDAOMock).updateStatus(taskSimulato.getId(), Tipi.stato.IN_ESECUZIONE);
        }

        @Test
        @DisplayName("TF3: TaskID valido e stato già 'IN_ESECUZIONE' -> Rimane 'IN_ESECUZIONE' e ritorna true")
        void testInizializzaTask_GiaInEsecuzione() throws Exception {
            // 1. SETUP SPECIFICO

            taskSimulato.setStato(Tipi.stato.IN_ESECUZIONE);
            long taskId = taskSimulato.getId();

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Il service recupera il task, che ora ha stato IN_ESECUZIONE
            when(taskDAOMock.findById(taskId)).thenReturn(taskSimulato);

            // 3. ESECUZIONE
            boolean risultato = taskDipendenteService.inizializzaTask(taskId);

            // 4. ORACOLO
            Assertions.assertTrue(risultato, "Il metodo deve restituire true anche se il task è già in esecuzione (idempotenza logica)");

            // 5. VERIFICA INTERAZIONE
            verify(taskDAOMock).updateStatus(taskId, Tipi.stato.IN_ESECUZIONE);
        }

        @Test
        @DisplayName("TF4: TaskID valido ma non presente nel DB -> Restituisce false")
        void testInizializzaTask_TaskNonTrovato() throws Exception {
            // 1. SETUP SPECIFICO


            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            when(taskDAOMock.findById(taskSimulato.getId())).thenReturn(null);

            // 3. ESECUZIONE
            boolean risultato = taskDipendenteService.inizializzaTask(taskSimulato.getId());

            // 4. ORACOLO
            Assertions.assertFalse(risultato, "Il metodo deve restituire false se il task non esiste");

            // 5. VERIFICA INTERAZIONE

            verify(taskDAOMock, never()).updateStatus(anyLong(), any());
        }

        @Test
        @DisplayName("TF5: TaskID valido ma stato 'COMPLETATO' -> Lancia IllegalStateException")
        void testInizializzaTask_StatoCompletato() throws Exception {
            // 1. SETUP SPECIFICO

            taskSimulato.setStato(Tipi.stato.COMPLETATO);


            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Il service recupera il task dal DB e trova che è COMPLETATO
            when(taskDAOMock.findById(taskSimulato.getId())).thenReturn(taskSimulato);

            // 3. ESECUZIONE & ORACOLO
            // Ci aspettiamo che il service lanci un'eccezione bloccante
            Assertions.assertThrows(IllegalStateException.class, () -> {
                taskDipendenteService.inizializzaTask(taskSimulato.getId());
            });

            // 4. VERIFICA INTERAZIONE
            // Verifica fondamentale: il DB non deve essere aggiornato
            verify(taskDAOMock, never()).updateStatus(anyLong(), any());
        }

        @Test
        @DisplayName("TF6: TaskID non valido (<= 0) -> Lancia IllegalArgumentException")
        void testInizializzaTask_IdNonValido() {
            // 1. SETUP
            int taskIdInvalido = -1;

            // 2. ESECUZIONE & ORACOLO
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDipendenteService.inizializzaTask(taskIdInvalido);
            });

            // 3. VERIFICA

            verifyNoInteractions(persistenceServiceMock);
        }
    }

    @Nested
    @DisplayName("Test del metodo completeTask")
    class Test_completeTask {
        private Task taskSimulato;
        @BeforeEach
        void setUp() {
            // 1. SETUP
            long taskId = 2L;

            // Creiamo un task simulato campi necessarri per inizializzatask
            taskSimulato = new Task();
            taskSimulato.setId(taskId);
            taskSimulato.setStato(Tipi.stato.IN_ESECUZIONE);
        }

        @Test
        @DisplayName("TF1: TaskID valido e stato 'IN_ESECUZIONE' -> Aggiorna a 'COMPLETATO' e ritorna true")
        void testCompleteTask_Successo() throws Exception {
            // 1. SETUP SPECIFICO


            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Il service recupera il task e verifica che sia IN_ESECUZIONE
            when(taskDAOMock.findById(taskSimulato.getId())).thenReturn(taskSimulato);

            // 3. ESECUZIONE
            boolean risultato = taskDipendenteService.completeTask(taskSimulato.getId());

            // 4. ORACOLO
            Assertions.assertTrue(risultato, "Il metodo deve restituire true dopo il completamento");

            // 5. VERIFICA INTERAZIONE

            verify(taskDAOMock).updateStatus(taskSimulato.getId(), Tipi.stato.COMPLETATO);
        }


        @Test
        @DisplayName("TF2: TaskID valido ma non presente nel DB -> Restituisce false")
        void testCompleteTask_TaskNonTrovato() throws Exception {
            // 1. SETUP SPECIFICO
            long taskId = taskSimulato.getId();

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Simuliamo che il task NON venga trovato nel DB
            when(taskDAOMock.findById(taskId)).thenReturn(null);

            // 3. ESECUZIONE

            boolean risultato = taskDipendenteService.completeTask(taskId);

            // 4. ORACOLO
            Assertions.assertFalse(risultato, "Il metodo deve restituire false se il task non viene trovato");

            // 5. VERIFICA INTERAZIONE
            // Fondamentale: updateStatus NON deve essere mai chiamato
            verify(taskDAOMock, never()).updateStatus(anyLong(), any());
        }

        @Test
        @DisplayName("TF3: TaskID valido ma stato 'COMPLETATO' -> Lancia IllegalStateException")
        void testCompleteTask_StatoErrato() throws Exception {
            // 1. SETUP SPECIFICO

            taskSimulato.setStato(Tipi.stato.COMPLETATO);
            long taskId = taskSimulato.getId();

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Il service recupera il task dal DB e trova lo stato non valido
            when(taskDAOMock.findById(taskId)).thenReturn(taskSimulato);

            // 3. ESECUZIONE & ORACOLO
            Assertions.assertThrows(IllegalStateException.class, () -> {
                taskDipendenteService.completeTask(taskId);
            });

            // 4. VERIFICA INTERAZIONE
            // Nessun aggiornamento deve essere inviato al database
            verify(taskDAOMock, never()).updateStatus(anyLong(), any());
        }

        @Test
        @DisplayName("TF4: TaskID non valido (<= 0) -> Lancia IllegalArgumentException")
        void testCompleteTask_IdNonValido() {
            // 1. SETUP
            int taskIdInvalido = -1;

            // 2. ESECUZIONE & ORACOLO
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDipendenteService.completeTask(taskIdInvalido);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }
    }


    }


