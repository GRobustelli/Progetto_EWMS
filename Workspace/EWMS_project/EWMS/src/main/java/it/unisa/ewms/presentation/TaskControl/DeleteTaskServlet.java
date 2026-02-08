package it.unisa.ewms.presentation.TaskControl;

import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.Supervisore;
import it.unisa.ewms.model.beans.Tipi;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeleteTaskServlet", value = "/delete-task")
public class DeleteTaskServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Supervisore utente = (Supervisore) session.getAttribute("utente");

        if (utente != null) {
            if (utente.getRuolo() == Tipi.ruolo.SUPERVISORE) {
                TaskSupervisoreService service = new TaskSupervisoreServiceImpl();
                long id = Long.parseLong(request.getParameter("idTask"));
                try {
                    if (service.deleteTask(id)){
                        response.sendRedirect(request.getContextPath() + "/homepage");
                    }
                    else{
                        request.setAttribute("error", "Impossibile eliminare il task");
                        request.setAttribute("viewPath", "/WEB-INF/views/taskDetail.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                    }

                } catch (SQLException e) {
                    request.setAttribute("error", e.getMessage());
                    request.setAttribute("viewPath", "/WEB-INF/views/taskDetail.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                }

            }
        }
    }
}