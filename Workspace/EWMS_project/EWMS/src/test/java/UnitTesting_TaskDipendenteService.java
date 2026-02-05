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


    }


