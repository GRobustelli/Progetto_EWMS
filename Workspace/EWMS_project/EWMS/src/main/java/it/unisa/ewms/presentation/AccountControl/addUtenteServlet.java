package it.unisa.ewms.presentation.AccountControl;


import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistence.eccezioni.EmailGiaPresenteException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "addUtenteServlet", value = "/add-utente")
public class addUtenteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utente utente = (Utente) session.getAttribute("utente");

        String action = request.getParameter("action");


        if (utente != null) {
            if (utente.getRuolo() == Tipi.ruolo.GESTORE){
                if (action.equals("openForm"))
                {
                ProfileManagementService service = new ProfileManagementServiceImpl();

                try {

                    List<Informazioni> informazioniSup = service.getAllSupervisori();
                    request.setAttribute("informazioniSup", informazioniSup);
                    request.setAttribute("viewPath", "/WEB-INF/views/creaAccount.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
                    return;


                } catch (SQLException e) {

                    request.setAttribute("error", e.getMessage());
                    request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);

                }
            }
        }
            else{
                request.setAttribute("error", "Utente non autorizzato");
                request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
            }
        }else{
            request.setAttribute("error", "Utente non autenticato");
            request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
            request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        ProfileManagementService service = new ProfileManagementServiceImpl();

        if (action.equals("insertUtente")) {
            String nome =  request.getParameter("nome");
            String cognome = request.getParameter("cognome");
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            String dataInput = request.getParameter("data");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Date dataDiNascita = Date.valueOf(LocalDate.parse(dataInput, formatter));

            Tipi.ruolo ruolo = Tipi.ruolo.valueOf(request.getParameter("ruolo"));
            Utente utente = null;

            if (ruolo == Tipi.ruolo.DIPENDENTE){
                int supmatricola = Integer.parseInt(request.getParameter("supervisore"));
                Informazioni informazioni = new Informazioni(supmatricola,null,null);
                Dipendente dip = new Dipendente(ruolo,email,nome,cognome,dataDiNascita,true);
                dip.setSupervisoreInfo(informazioni);
                utente = dip;

            }else if (ruolo == Tipi.ruolo.SUPERVISORE){
                Supervisore sup = new Supervisore(ruolo,email,nome,cognome,dataDiNascita,true);
                utente = sup;
            }
            else {
                 utente = new Utente(ruolo,email,nome,cognome,dataDiNascita,true);
            }

            try {
                service.addAccount(utente, password);


            }catch (EmailGiaPresenteException e) {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("viewPath", "/WEB-INF/views/creaAccount.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);

            } catch (SQLException e) {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("viewPath", "/WEB-INF/views/homepage.jsp");
                request.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(request, response);

            }

            response.sendRedirect(request.getContextPath() + "/homepage");


        }
    }
}