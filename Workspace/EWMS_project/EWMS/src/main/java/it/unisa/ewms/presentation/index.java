package it.unisa.ewms.presentation;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ProvaSerlet", value = "/ProvaServlet")
public class index extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = "/WEB-INF/views/login.jsp";
        try {
            request.getRequestDispatcher(path).forward(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}