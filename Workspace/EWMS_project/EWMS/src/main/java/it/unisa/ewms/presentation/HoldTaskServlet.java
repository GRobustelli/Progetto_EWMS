package it.unisa.ewms.presentation;

import it.unisa.ewms.application.TaskManagement.TaskCommonServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "HoldTaskServlet", value = "/HoldTaskServlet")
public class HoldTaskServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        TaskCommonService taskService = new TaskCommonServiceImpl();

        if(session!=null){
            if(session.getAttribute("utente")!=null) {
                long taskId = Long.parseLong(request.getParameter("id"));

                try {
                    taskService.holdTask(taskId);

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