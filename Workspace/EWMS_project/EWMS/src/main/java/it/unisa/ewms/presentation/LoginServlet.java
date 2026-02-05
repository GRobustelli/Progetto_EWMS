package it.unisa.ewms.presentation;

import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.application.TaskManagement.TaskDipendenteServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Supervisore;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Utente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "LoginServlet", value = "/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        SessionService sessionService = new SessionServiceImpl();

        if (sessionService.login(username, password)){
            Utente utente = sessionService.getUtente(username);
            HttpSession session = request.getSession();
            if (utente != null){
                switch(utente.getRuolo()){
                    case DIPENDENTE:
                        TaskDipendenteService dipService = new TaskDipendenteServiceImpl();
                        Dipendente diptmp = (Dipendente) utente;
                        try {
                            List<Task> taskList = dipService.getAllTaskDip(diptmp);
                            request.setAttribute("taskList", taskList);
                        } catch (SQLException e) {
                            request.setAttribute("error", "Errore durante il caricamento della homepage");
                            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        }

                        session.setAttribute("utente", utente);
                        request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        break;

                    case SUPERVISORE:
                        TaskSupervisoreService supService = new TaskSupervisoreServiceImpl();
                        Supervisore suptmp = (Supervisore) utente;
                        try{
                            List<Task> taskList = supService.getAllTaskSup(suptmp);
                            request.setAttribute("taskList", taskList);
                        }catch (SQLException e){
                            request.setAttribute("error", "Errore durante il caricamento della homepage");
                            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        }

                        session.setAttribute("utente", utente);
                        request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        break;

                    case GESTORE:
                        ProfileManagementService gestoreService = new ProfileManagementServiceImpl();
                        try{
                            List<Utente> userList = gestoreService.getAllAccount();
                            request.setAttribute("userList", userList);
                        } catch (SQLException e) {
                            request.setAttribute("error", "Errore durante il caricamento della homepage");
                            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        }

                        session.setAttribute("utente", utente);
                        request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        break;
                }
            }
            else{
               request.setAttribute("error", "Errore durante il login, prova più tardi");
               request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
               request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            }

        }else{
            request.setAttribute("error", "Credenziali errate");
            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
        }
    }
}