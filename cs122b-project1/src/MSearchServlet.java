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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/MovieSearch"
@WebServlet(name = "MSearchServlet", urlPatterns = "/api/movies-search")
public class MSearchServlet extends HttpServlet {

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

        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()){

            // Declare our statement
            //Statement statement = conn.createStatement();

            // An array to as flags for search conditions
            int[] flag = {0, 0, 0, 0};      // bit: 0 - not presented else presented
            // order: title, year, director, star

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String title = request.getParameter("movie_title");
            String year = request.getParameter("year");
            String director_name = request.getParameter("director_name");
            String star_name = request.getParameter("star_name");

            flag[0] = title.equals("") ? 0 : 1;
            flag[1] = year.equals("") ? 0 : 1;
            flag[2] = director_name.equals("") ? 0 : 1;
            flag[3] = star_name.equals("") ? 0 : 1;

            int num = 0;
            for (int i : flag){
                if (i == 1) num++;
            }

            String query = SQLQueryTemplate.U_MOVIE_SEARCH_PAGE_;

            // Generate base Query
            boolean not_Search = false;
            if (flag[0] == 0 && flag[1] == 0 && flag[2] == 0 && flag[3] == 0){
                not_Search = true;
                if (session.getAttribute("query") != null)
                    query = (String) session.getAttribute("query");
                if (session.getAttribute("sort") != null){
                    String sort_query = (String)session.getAttribute("sort");
                    query = query + " " + sort_query + " ";
                }
                if (session.getAttribute("result_limit") != null){
                    String limit_Option = (String)session.getAttribute("result_limit");
                    String limit_Op = "";
                    if (limit_Option.equals("100")) {
                        limit_Op = limit_Op + "100 ";
                    } else if (limit_Option.equals("50")) {
                        limit_Op = limit_Op + "50 ";
                    } else if (limit_Option.equals("25")) {
                        limit_Op = limit_Op + "25 ";
                    } else { // if limit_Option equals 10
                        limit_Op = limit_Op + "10 ";
                    }
                    query = query + limit_Op;
                }
            }
            else {
                if (flag[1] == 1) { // Single condition query search
                    query = query + "and m.year = ?";
                }
            }

            // Plug in parameters
            PreparedStatement statement = conn.prepareStatement(query);
            // At first set non-presented parameter to empty
            if (flag[0] == 0)
                statement.setString(1, "%%"); // 1: title
            else
                statement.setString(1, "%" + title + "%");

            if (flag[2] == 0)
                statement.setString(2, "%%"); // 2: director
            else
                statement.setString(2, "%" + director_name + "%");

            if (flag[3] == 0)
                statement.setString(3, "%%"); // 3: star
            else
                statement.setString(3, "%" + star_name + "%");
            // Check year
            if (flag[1] == 1) { // year
                int yearInteger = Integer.parseInt(year);
                statement.setInt(4, yearInteger);
            }

            String contextPath = request.getServletContext().getRealPath("/");
            String xmlFilePath = contextPath + "log.txt";
            File TJoutFile = new File(xmlFilePath);
            if (!TJoutFile.exists())
                TJoutFile.createNewFile();

            // Time the total JDBC parts per query (TJ)
            long startTime = System.nanoTime();

            // Perform the query
            ResultSet rs = statement.executeQuery();

            long endTime = System.nanoTime();
            long TJelapsedTime = endTime - startTime; // in nano seconds
            System.out.println("TJ time for search: " + TJelapsedTime);

            // Append new TS values to TSoutFile
            FileWriter fwriter = new FileWriter(TJoutFile, true);
            fwriter.write(String.valueOf(TJelapsedTime) + " ");
            fwriter.flush();
            fwriter.close();

            String queryString = statement.toString();
            queryString = queryString.substring( queryString.indexOf( ": " ) + 2 );

            session.setAttribute("previousQuery", queryString);
            session.setAttribute("page", "1");

            // Log to localhost log
            request.getServletContext().log("query：" + query);


            JsonArray jsonArray = new JsonArray();

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
