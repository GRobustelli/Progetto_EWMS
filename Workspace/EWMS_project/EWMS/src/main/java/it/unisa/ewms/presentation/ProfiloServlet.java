package it.unisa.ewms.presentation;

import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "ProfiloServlet", value = "/ProfiloServlet")
public class ProfiloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        if(session!=null){
            if(session.getAttribute("utente")!=null) {
                request.setAttribute("viewPath", "/WEB-INF/views/login.jsp");
                request.getRequestDispatcher("/WEB-INF/views/profilo.jsp").forward(request, response);
            }else{
                request.setAttribute("error", "Utente non loggato");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        }else{
            request.setAttribute("error", "Credenziali errate");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}