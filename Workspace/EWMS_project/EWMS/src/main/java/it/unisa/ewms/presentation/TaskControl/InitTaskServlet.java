package it.unisa.ewms.presentation.TaskControl;

import it.unisa.ewms.application.TaskManagement.TaskDipendenteServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "InitTaskServlet", value = "/InitTaskServlet")
public class InitTaskServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        TaskDipendenteService taskService = new TaskDipendenteServiceImpl();

        if(session!=null){
            if(session.getAttribute("utente")!=null) {
                long taskId = Long.parseLong(request.getParameter("id"));

                try {
                    taskService.inizializzaTask(taskId);

                    /*qui dovrebbe esserci il codice per l'invio dei warning,
                    non essendo arrivato all'implementazione non c'è*/

                    request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                } catch (Exception e) {
                    request.setAttribute("error", "Errore durante la sospensione del task");
                    request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                }
            }else{
                request.setAttribute("error", "Utente non loggato");
                request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            }
        }else{

            request.setAttribute("error", "Sessione invalidata");
            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
        }
    }
}