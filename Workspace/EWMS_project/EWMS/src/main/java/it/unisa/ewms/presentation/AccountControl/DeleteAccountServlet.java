package it.unisa.ewms.presentation.AccountControl;


import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
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
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet(name = "DeleteAccountServlet", value = "/delete-account")
public class DeleteAccountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utente utente= (Utente) session.getAttribute("utente");

        if (utente!=null){
            if (utente.getRuolo() == Tipi.ruolo.GESTORE){
                int matricola =  Integer.parseInt(request.getParameter("matricola"));

                ProfileManagementService service = new ProfileManagementServiceImpl();
                try {
                    Utente utente1 = service.getAccount(matricola);
                    if (utente1!=null){
                        if (utente.getMatricola() == matricola){

                            request.setAttribute("error", "Impossibile eliminare account in utilizzo");
                            request.getRequestDispatcher("/homepage").forward(request, response);
                            return;
                        }
                        service.deleteAccount(utente1);

                        response.sendRedirect(request.getContextPath()+"/homepage");

                    }else{
                        request.setAttribute("error", "Utente inesistente");

                        request.getRequestDispatcher("/homepage").forward(request, response);

                    }

                } catch (SQLException e) {
                    if (e instanceof SQLIntegrityConstraintViolationException){

                        request.setAttribute("error", e.getMessage());

                        request.getRequestDispatcher("/homepage").forward(request, response);

                    }else{
                        request.setAttribute("error", "Errore durante l'eliminazione dell'utente");
                        request.setAttribute("viewPath", "/WEB-INF/views/modProfile.jsp");
                        request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);

                    }

                }

            }else{
                response.sendRedirect(request.getContextPath()+ "/homepage");
            }
        }else{
            response.sendRedirect("/StartServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}