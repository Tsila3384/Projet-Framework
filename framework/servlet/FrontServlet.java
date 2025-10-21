package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;

public class FrontServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
       
        String urlPath = req.getRequestURI();
        InputStream resource = getServletContext().getResourceAsStream(urlPath);
        
        if (resource != null) {
            resource.transferTo(resp.getOutputStream());
            resource.close();
        } else {
            System.out.println("Vous essayez d'acceder a : " + urlPath);
            resp.setContentType("text/html");
            resp.getWriter().write("<h1>Vous essayez d'acceder a : " + urlPath + "</h1>");
        }
    }
}
