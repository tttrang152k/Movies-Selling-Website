import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {

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
        String card_number = request.getParameter("card_number");
        String first_name = request.getParameter("first_name");
        String last_name = request.getParameter("last_name");
        String exp_date = request.getParameter("exp_date");

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();

        // Demo
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = SQLQueryTemplate.CHECK_OUT_PAGE_;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, card_number);
            statement.setString(2, first_name);
            statement.setString(3, last_name);
            statement.setString(4, exp_date);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            int row_count = 0;
            // Iterate through each row of rs
            while (rs.next()) {
                // All credit card info correct
                row_count++;
                // Insert transaction into sale table
                HttpSession session = request.getSession();

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
            rs.close();
            statement.close();
            conn.close();

            if (row_count == 0) {
                JsonObject jsonObject = new JsonObject();

                request.getServletContext().log("Checkout failed");
                // Login fail due to invalid username
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Card Information Incorrect");

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
