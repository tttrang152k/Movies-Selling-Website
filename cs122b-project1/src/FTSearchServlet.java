import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/MovieSearch"
@WebServlet(name = "FTSearchServlet", urlPatterns = "/api/fulltext-search")
public class FTSearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        long TSstartTime = System.currentTimeMillis();

        response.setContentType("application/json"); // Response mime type


        HttpSession session = request.getSession();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();


        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()){

            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String title_query = request.getParameter("ft_movie_title");
            System.out.println("ft title: " + title_query);

            // return the empty json array if query is null or empty
            if (title_query == null || title_query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }
            String[] tokens = title_query.split(" ");

            String query = SQLQueryTemplate.FULL_TEXT_MOVIE_SEARCH_;
            PreparedStatement statement = conn.prepareStatement(query);

            String ft_tokens = "";
            for (String token : tokens)
                ft_tokens += " +" + token + "*";

            System.out.println("tokens list: " + ft_tokens);
            statement.setString(1, ft_tokens);

           // String contextPath = request.getServletContext().getRealPath("/");
            //SyncAppend timer = new SyncAppend(contextPath);
            //String xmlFilePath = contextPath + "log.txt";
            //File TJoutFile = new File(xmlFilePath);
            //if (!TJoutFile.exists())
            //    TJoutFile.createNewFile();

            // Time the total JDBC parts per query (TJ)
            long TJstartTime = System.currentTimeMillis();

            // Perform the query
            ResultSet rs = statement.executeQuery();

            long TJendTime = System.currentTimeMillis();
            long TJelapsedTime = TJendTime - TJstartTime; // in nano seconds
            //System.out.println("TJ time for search: " + TJelapsedTime);
            //timer.syncAppend(TJelapsedTime, 0);

            // Append new TS values to TSoutFile
            //FileWriter fwriter = new FileWriter(TJoutFile, true);
            //fwriter.write(String.valueOf(TJelapsedTime) + " ");
            //fwriter.flush();
            //fwriter.close();

            String queryString = statement.toString();
            queryString = queryString.substring( queryString.indexOf( ": " ) + 2 );

            session.setAttribute("previousQuery", queryString);
            session.setAttribute("page", "1");

            // Log to localhost log
            request.getServletContext().log("query：" + query);

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_name = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_dir= rs.getString("director");
                String movie_gen = rs.getString("genres");

                String movie_star = rs.getString("stars");
                String movie_star_ids = rs.getString("starID");
                String movie_rating= rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_name", movie_name);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_dir", movie_dir);
                jsonObject.addProperty("movie_gen", movie_gen);
                jsonObject.addProperty("movie_star", movie_star);
                jsonObject.addProperty("movie_star_ids", movie_star_ids);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }

            // Close all structures
            rs.close();
            statement.close();
            conn.close();

            long TSendTime = System.currentTimeMillis();
            long TSelapsedTime = TSendTime - TSstartTime; // in nano seconds

            String contextPath = request.getServletContext().getRealPath("/");
            String xmlFilePath = contextPath + "log.txt";
            System.out.println(xmlFilePath);
            File TSoutFile = new File(xmlFilePath);
            if (!TSoutFile.exists())
                TSoutFile.createNewFile();

            // Append new TS values to TSoutFile
            FileWriter fwriter = new FileWriter(TSoutFile, true);
            fwriter.write(String.valueOf(TJelapsedTime) + " " + String.valueOf(TSelapsedTime) + "\n");
            fwriter.flush();
            fwriter.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
