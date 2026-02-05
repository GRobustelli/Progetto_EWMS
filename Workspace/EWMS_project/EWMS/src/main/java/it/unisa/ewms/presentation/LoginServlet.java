package it.unisa.ewms.presentation;

import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.model.beans.Utente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

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

                session.setAttribute("utente", utente);
                //faccio redirect a homepage
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