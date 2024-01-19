import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "SearchFilter", urlPatterns = {"/api/movies-search", "/api/fulltext-search"})
public class SearchFilter implements Filter {


    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
/*
        // Follow instructions from https://javajee.com/writing-to-a-file-from-a-servlet-in-a-java-ee-web-application
        String contextPath = request.getServletContext().getRealPath("/");
        String xmlFilePath = contextPath + "log.txt";
        System.out.println(xmlFilePath);
        File TSoutFile = new File(xmlFilePath);
        if (!TSoutFile.exists())
            TSoutFile.createNewFile();

        // Time the total search servlet to run entirely for a query (TS)
        long startTime = System.nanoTime();
        chain.doFilter(request, response);
        long endTime = System.nanoTime();
        long TSelapsedTime = endTime - startTime; // in nano seconds
        System.out.println("TS time for search: " + TSelapsedTime);



        // Append new TS values to TSoutFile
        FileWriter fwriter = new FileWriter(TSoutFile, true);
        fwriter.write(String.valueOf(TSelapsedTime) + "\n");
        fwriter.flush();
        fwriter.close();
*/
        // Time the total search servlet to run entirely for a query (TS)
        //String contextPath = request.getServletContext().getRealPath("/");
        //SyncAppend timer = new SyncAppend(contextPath);

        //long startTime = System.nanoTime();
        chain.doFilter(request, response);
        //long endTime = System.nanoTime();
        //long TSelapsedTime = endTime - startTime; // in nano seconds
        //timer.syncAppend(TSelapsedTime, 1);


    }

    public void destroy() {
        // ignored.
    }

}
