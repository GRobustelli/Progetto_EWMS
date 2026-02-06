package it.unisa.ewms.presentation;

import it.unisa.ewms.application.TaskManagement.TaskCommonServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "TaskDetailServlet", value = "/TaskDetailServlet")
public class TaskDetailServlet extends HttpServlet {
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
                Utente utente = (Utente) session.getAttribute("utente");
                Long taskId = (Long) request.getParameter("id");

                try {
                    Task tmp = taskService.getTask(taskId);
                    request.setAttribute("task", tmp);
                    request.setAttribute("viewPath", "/WEB-INF/views/detailTask.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                } catch (SQLException e) {
                    request.setAttribute("error", "Errore durante il caricamento della pagina");
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