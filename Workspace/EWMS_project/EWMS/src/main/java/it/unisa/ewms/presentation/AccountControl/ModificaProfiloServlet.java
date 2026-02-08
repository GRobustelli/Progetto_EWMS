package it.unisa.ewms.presentation.AccountControl;

import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.model.beans.Dipendente;
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

import static it.unisa.ewms.model.beans.Tipi.ruolo.DIPENDENTE;

@WebServlet(name = "ModificaProfiloServlet", value = "/mod-profilo")
public class ModificaProfiloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            HttpSession session = request.getSession();
            Utente utente = (Utente)session.getAttribute("utente");
            int matricola = Integer.parseInt(request.getParameter("id"));

            if (utente != null) {
                if (utente.getRuolo() == Tipi.ruolo.GESTORE){
                    ProfileManagementService service = new ProfileManagementServiceImpl();

                    try {
                        Utente profilo = service.getAccount(matricola);
                        if (profilo != null) {
                            switch (profilo.getRuolo()) {
                                case DIPENDENTE:
                                    Dipendente profilo1 = (Dipendente) profilo;
                                    request.setAttribute("utenteTrovato", profilo1);
                                    request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                                    return;
                                case SUPERVISORE:
                                    Supervisore profilo2 = (Supervisore) profilo;
                                    request.setAttribute("utenteTrovato", profilo2);
                                    request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                                    return;
                                case GESTORE:
                                    request.setAttribute("utenteTrovato", profilo);
                                    request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                            }

                        } else{
                            request.setAttribute("error", "Errore durante il caricamento del profilo selezionato");
                            request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                        }

                    } catch (SQLException e) {
                        request.setAttribute("error", e.getMessage());
                        request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                    }


                }

            }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            doGet(request, response);
    }
}