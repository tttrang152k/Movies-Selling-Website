import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

@WebServlet(name = "addMovieServlet", urlPatterns = "/_dashboard/api/add_movie")
public class addMovieServlet extends HttpServlet {
    // Create a dataSource which registered in web.
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();

        response.setContentType("application/json"); // Response mime type

        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("year");
        String director = request.getParameter("director_name");
        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");
        String genre = request.getParameter("genre");

        System.out.println("title: " + movie_title + " year: " + movie_year + " director: " + director);
        System.out.println("star : " + star_name + " birth year: " + birth_year + " genre: " + genre);

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = SQLQueryTemplate.ADD_MOVIE_PAGE_;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Required fields: title, director, genre, star
            statement.setString(1, movie_title);
            if (movie_year.isEmpty())
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2, Integer.parseInt(movie_year));
            statement.setString(3, director);
            statement.setString(4, star_name);
            if (birth_year.isEmpty())
                statement.setNull(5, Types.INTEGER);
            else
                statement.setInt(5, Integer.parseInt(birth_year));
            statement.setString(6, genre);

            System.out.println("query before execution: " + statement);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            int row_count = 0;
            // Iterate through each row of rs
            while (rs.next()) {
                // All credit card info correct
                row_count++;
                // Insert transaction into sale table
                HttpSession session = request.getSession();

                String message = rs.getString("answer");
                if (message.equals("duplicated")) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Failed: The movie already exists");
                }
                else {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "New movie with: " + message + " is added");
                }
            }
            rs.close();
            statement.close();
            conn.close();

            if (row_count == 0) {
                JsonObject jsonObject = new JsonObject();
                request.getServletContext().log("Add failed");
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Adding new movie did not succeed");
                out.write(jsonObject.toString());
                // Set response status to 200
                response.setStatus(200);
            } else {
                // Write JSON string to output
                out.write(responseJsonObject.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}
