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

@WebServlet(name = "addStarServlet", urlPatterns = "/_dashboard/api/add_star")
public class addStarServlet extends HttpServlet {
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

        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("year");

        System.out.println("star_name: " + star_name + " birth year: " + birth_year);

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = SQLQueryTemplate.ADD_STAR_PAGE_;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, star_name);
            if (birth_year.isEmpty())
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2, Integer.parseInt(birth_year));

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

                String star_id = rs.getString("star_id");

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "New star with id: " + star_id + " is added");
            }
            rs.close();
            statement.close();
            conn.close();

            if (row_count == 0) {
                JsonObject jsonObject = new JsonObject();
                request.getServletContext().log("Add failed");
                // Login fail due to invalid username
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Add a star did not succeed");
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
