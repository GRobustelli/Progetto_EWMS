import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskCommonServiceImpl;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.PersistenceManagement.ClassiDAO.TaskDAO;
import it.unisa.ewms.PersistenceManagement.interfaces.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UnitTesting_TaskCommonService {
    @Mock
    private PersistenceService persistenceServiceMock;

    @Mock
    private TaskDAO taskDAOMock;

    private TaskCommonServiceImpl taskCommonService;


    @BeforeEach
    void setup() {
        // Garantiamo l'invariante service <> null tramite costruttore
        taskCommonService = new TaskCommonServiceImpl(persistenceServiceMock);
    }



    @Test
    void testCostruttore_ConNull_LanciaException() {
        assertThrows(IllegalArgumentException.class, () -> new SessionServiceImpl(null));
    }

    @Nested
    @DisplayName("TCST_1: Classe test per metodo getTask")
    class Test_getTask{
        private Task taskAtteso;
        private int  taskId;


        @BeforeEach
        void setup() {
            taskId = 101;
            taskAtteso = new Task();
            taskAtteso.setId(taskId);
            taskAtteso.setTitolo("Analisi Requisiti");
            taskAtteso.setIstruzioni("Controllare specifiche");
            taskAtteso.setStato(Tipi.stato.DA_COMPLETARE);
        }

        @Test
        @DisplayName("TF1: TaskID valido (>0) e presente nel DB -> Restituisce Task")
        void testGetTask_Presente() throws SQLException {
            // 1. SETUP DATI (ARRANGE)

            // 2. CONFIGURAZIONE MOCK

            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);

            // Istruiamo il taskDAO
            when(taskDAOMock.findById(taskId)).thenReturn(taskAtteso);

            // 3. ESECUZIONE (ACT)
            Task risultato = taskCommonService.getTask(taskId);

            // 4. ORACOLO (ASSERT)
            // Verifichiamo che il risultato non sia null
            assertNotNull(risultato, "Il service deve restituire un oggetto Task.");

            // Verifichiamo che sia esattamente l'oggetto restituito dal DAO
            assertEquals(taskAtteso, risultato, "Il task restituito deve coincidere con quello del DAO.");
            assertEquals(taskId, risultato.getId(), "L'ID del task deve corrispondere.");

            // 5. VERIFICA INTERAZIONI
            // Confermiamo che il service abbia effettivamente chiamato il metodo findById del DAO
            verify(taskDAOMock).findById(taskId);
        }

        @Test
        @DisplayName("TF2: TaskID valido (>0) ma non presente -> Restituisce null")
        void testGetTask_NonPresente() throws SQLException {
            // 1. SETUP DATI
            int taskIdNonEsistente = 999; // Un ID valido ma che assumiamo non esista

            // 2. CONFIGURAZIONE MOCK

            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);


            when(taskDAOMock.findById(taskIdNonEsistente)).thenReturn(null);

            // 3. ESECUZIONE
            Task risultato = taskCommonService.getTask(taskIdNonEsistente);

            // 4. ORACOLO

            assertNull(risultato, "Il metodo deve restituire null se l'ID non corrisponde a nessun task.");


            verify(taskDAOMock).findById(taskIdNonEsistente);
        }


        @Test
        @DisplayName("TF3: TaskID non valido (< 0) -> Lancia IllegalArgumentException")
        void testGetTask_IdNonValido() {
            // 1. INPUT
            int invalidId = -1; // Viola la pre-condizione taskID > 0

            // 2. ESECUZIONE & ORACOLO

            assertThrows(IllegalArgumentException.class, () -> {
                taskCommonService.getTask(invalidId);
            });

            // 3. VERIFICA ISOLAMENTO

            verifyNoInteractions(persistenceServiceMock);
        }
    }

    @Nested
    @DisplayName("TCST_2: Classe per il testing del metodo holdTask")
    class Test_holdTask{

        private Task taskAtteso;
        private int  taskId;
        @BeforeEach
        void setup() {
            taskId = 101;
            taskAtteso = new Task();
            taskAtteso.setId(taskId);
            taskAtteso.setTitolo("Analisi Requisiti");
            taskAtteso.setIstruzioni("Controllare specifiche");
            taskAtteso.setStato(Tipi.stato.DA_COMPLETARE);
        }

        @Test
        @DisplayName("TF1: Task presente e in stato IN_ESECUZIONE -> Restituisce true e aggiorna stato")
        void testHoldTask_InEsecuzione_Successo() throws Exception {

            // 1. SETUP SPECIFICO

            taskAtteso.setStato(Tipi.stato.IN_ESECUZIONE);

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            when(taskDAOMock.findById(taskId)).thenReturn(taskAtteso);

            // 3. ESECUZIONE
            boolean risultato = taskCommonService.holdTask(taskId);

            // 4. ORACOLO
            // Verifica valore di ritorno
            assertTrue(risultato, "Il metodo deve restituire true quando l'aggiornamento ha successo.");

            try {
                verify(taskDAOMock).updateStatus(taskId, Tipi.stato.IN_SOSPENSIONE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("TF2: Task già in IN_SOSPENSIONE -> Restituisce true")
        void testHoldTask_GiaSospeso() throws Exception {
            // 1. SETUP SPECIFICO
            // Impostiamo lo stato iniziale già su IN_SOSPENSIONE per soddisfare questo scenario
            taskAtteso.setStato(Tipi.stato.IN_SOSPENSIONE);

            // 2. CONFIGURAZIONE MOCK
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            when(taskDAOMock.findById(taskId)).thenReturn(taskAtteso);

            // 3. ESECUZIONE
            boolean risultato = taskCommonService.holdTask(taskId);

            // 4. ORACOLO
            // Deve restituire true come indicato dalla tua specifica
            assertTrue(risultato, "Il metodo deve restituire true anche se il task è già sospeso.");

            // 5. VERIFICA INTERAZIONI
            try {
                verify(taskDAOMock).updateStatus(taskId, Tipi.stato.IN_SOSPENSIONE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("TF3: Task non presente -> Restituisce false")
        void testHoldTask_NonPresente() throws Exception {
            // Mock: Task non trovato nel DB
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            when(taskDAOMock.findById(taskId)).thenReturn(null);

            // Esecuzione
            boolean risultato = taskCommonService.holdTask(taskId);

            // Oracolo
            assertFalse(risultato);

            // Verifica che NON sia stato chiamato l'update
            try {
                verify(taskDAOMock, never()).updateStatus(anyLong(), any());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Test
        @DisplayName("TF4: Stato non valido (DA_COMPLETARE) -> Lancia IllegalStateException")
        void testHoldTask_StatoNonValido() throws SQLException {
            // Setup: Stato non permesso dalla pre-condizione
            taskAtteso.setStato(Tipi.stato.DA_COMPLETARE);

            // Mock
            when(persistenceServiceMock.getTaskDAO()).thenReturn(taskDAOMock);
            when(taskDAOMock.findById(taskId)).thenReturn(taskAtteso);

            // Assert
            assertThrows(IllegalStateException.class, () -> {
                taskCommonService.holdTask(taskId);
            });

            // Verify: Nessun update su DB
            try {
                verify(taskDAOMock, never()).updateStatus(anyLong(), any());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("TF5: TaskID non valido (<= 0) -> Lancia IllegalArgumentException")
        void testHoldTask_IdNonValido() {
            // Input non valido
            taskAtteso.setId(-1L);

            // Esecuzione & Oracolo
            assertThrows(IllegalArgumentException.class, () -> {
                taskCommonService.holdTask(taskAtteso.getId());
            });

            // Verifica isolamento: Il DB non deve essere interrogato se l'input è invalido
            verifyNoInteractions(persistenceServiceMock);
        }


    }
}
