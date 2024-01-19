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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies-filter")
public class MovieListServlet extends HttpServlet {

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
            Statement statement = conn.createStatement();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String sort_Option = request.getParameter("sort");
            String orderBy_sortOp = "";

            if (sort_Option != null) {
                orderBy_sortOp = " order by ";
                // Check sorting option
                if (sort_Option.equals("1"))
                    orderBy_sortOp = orderBy_sortOp + "rat.rating asc, m.title asc ";
                else if (sort_Option.equals("2"))
                    orderBy_sortOp = orderBy_sortOp + "rat.rating asc, m.title desc ";
                else if (sort_Option.equals("3"))
                    orderBy_sortOp = orderBy_sortOp + "rat.rating desc, m.title desc ";
                else if (sort_Option.equals("4"))
                    orderBy_sortOp = orderBy_sortOp + "rat.rating desc, m.title asc ";
                else if (sort_Option.equals("5"))
                    orderBy_sortOp = orderBy_sortOp + "m.title asc, rat.rating asc ";
                else if (sort_Option.equals("6"))
                    orderBy_sortOp = orderBy_sortOp + "m.title asc, rat.rating desc ";
                else if (sort_Option.equals("7"))
                    orderBy_sortOp = orderBy_sortOp + "m.title desc, rat.rating desc ";
                else
                    orderBy_sortOp = orderBy_sortOp + "m.title desc, rat.rating asc ";

                // Keep the latest sort option
                session.setAttribute("sort", orderBy_sortOp);
            }

            //Retrieve parameter "result_limit" from the http request
            String limit_Option = request.getParameter("result_limit");
            String limit_Op = "";
            if (limit_Option != null) {
                limit_Op = " limit ";

                if (limit_Option.equals("100")) {
                    limit_Op = limit_Op + "100 ";
                } else if (limit_Option.equals("50")) {
                    limit_Op = limit_Op + "50 ";
                } else if (limit_Option.equals("25")) {
                    limit_Op = limit_Op + "25 ";
                } else { // if limit_Option equals 10
                    limit_Op = limit_Op + "10 ";
                }

                // Keep the latest limit option
                session.setAttribute("result_limit", limit_Option);
            }

            //Retrieve parameter "page" from the http request, page number * limit by = offset returned value
            String page_Num = request.getParameter("page");
            String offset = "";
            if (page_Num != null) {

                String currLimit = (String) session.getAttribute("result_limit");

                if (currLimit != null && page_Num != "1") {
                    offset = "offset " + ((Integer.parseInt(page_Num) - 1) * Integer.parseInt(currLimit)) + " ";
                }

                // Keep the latest page number
                session.setAttribute("page", page_Num);
            } else {
                page_Num = (String) session.getAttribute("page");

                String currLimit = (String) session.getAttribute("result_limit");

                if (currLimit != null  && page_Num != "1") { // No need for OFFSET bc it will be zero
                    offset = "offset " + ((Integer.parseInt(page_Num) - 1) * Integer.parseInt(currLimit)) + " ";
                }
            }

            String baseQuery = (String) session.getAttribute("previousQuery");
            String query = baseQuery + orderBy_sortOp + limit_Op + offset;

            // Log to localhost log
            request.getServletContext().log("query：" + query);

            // Perform the query
            ResultSet rs = statement.executeQuery(query);


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
                if (movie_rating.equals("0.0"))
                    movie_rating = "N/A";

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
