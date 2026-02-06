package it.unisa.ewms.presentation.TaskControl;

import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.Informazioni;
import it.unisa.ewms.model.beans.Supervisore;
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
import java.util.List;

@WebServlet(name = "CreaTaskServlet", value = "/CreaTaskServlet")
public class CreaTaskServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action =  request.getParameter("action");

        if (action.equals("creaTask")) {
            HttpSession session = request.getSession();
            Supervisore utente = (Supervisore) session.getAttribute("utente");
            if (utente != null && utente.getRuolo() == Tipi.ruolo.SUPERVISORE){
                TaskSupervisoreService service = new TaskSupervisoreServiceImpl();

                try {
                    List<Informazioni> informazioni = service.getAllDipendentiInfo(utente.getMatricola());
                    request.setAttribute("userList", informazioni);
                    request.setAttribute("viewPath", "/WEB-INF/views/creaTask.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);

                } catch (SQLException e) {
                    request.setAttribute("error", "Errore durante il caricamento della pagina di creazione task");
                    request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                }


            } else{
                request.setAttribute("error", "Richiesta da utente non loggato, per utilizzare le funzionalità è obbligatorio essere autenticati");
                request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            }

        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action =  request.getParameter("action");

        if (action.equals("insertTask")){
            HttpSession session = request.getSession();
            Utente utente = (Utente) session.getAttribute("utente");
            if (utente.getRuolo() == Tipi.ruolo.SUPERVISORE){



            }else{
                request.setAttribute("error", "Solo un utente con ruolo supervisore può aggiungere un task");
                request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            }

        }
    }
}