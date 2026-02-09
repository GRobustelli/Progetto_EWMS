package it.unisa.ewms.presentation;

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

@WebServlet(name = "HomepageServlet", value = "/homepage")
public class HomepageServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utente utente = (Utente) session.getAttribute("utente");
        if (utente == null) {
            request.setAttribute("error", "Utente non loggato");
            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            return;
        }

        switch (utente.getRuolo()) {
            case DIPENDENTE:
                TaskDipendenteService dipService = new TaskDipendenteServiceImpl();
                Dipendente diptmp = (Dipendente) utente;
                try {
                    List<Task> taskList = dipService.getAllTaskDip(diptmp);
                    request.setAttribute("taskList", taskList);


                } catch (SQLException e) {
                    request.setAttribute("error", "Errore durante il caricamento della homepage");
                    session.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                    response.sendRedirect(request.getContextPath() + "/DispatcherServlet");
                }

                session.setAttribute("utente", utente);

                break;

            case SUPERVISORE:
                TaskSupervisoreService supService = new TaskSupervisoreServiceImpl();
                Supervisore suptmp = (Supervisore) utente;
                try {
                    List<Task> taskList = supService.getAllTaskSup(suptmp);
                    request.setAttribute("taskList", taskList);
                    System.out.println("Ho caricato i task supervisore: " + taskList.size() + taskList.get(0).getIstruzioni());

                } catch (SQLException e) {
                    request.setAttribute("error", "Errore durante il caricamento della homepage");
                    request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                }

                session.setAttribute("utente", utente);
                break;

            case GESTORE:
                ProfileManagementService gestoreService = new ProfileManagementServiceImpl();
                try {
                    List<Utente> userList = gestoreService.getAllAccount();
                    request.setAttribute("userList", userList);
                } catch (SQLException e) {
                    request.setAttribute("error", "Errore durante il caricamento della homepage");
                    request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                }

                session.setAttribute("utente", utente);
                break;
        }


        //gestire il passaggio della jsp per quanto riguarda i button nelle jsp
        request.setAttribute("viewPath", "//WEB-INF/views/homepage.jsp");
        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}