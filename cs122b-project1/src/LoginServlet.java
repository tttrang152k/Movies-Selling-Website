import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        JsonObject responseJsonObject = new JsonObject();

        response.setContentType("application/json"); // Response mime type

        // Verify reCAPTCHA
        try {
            // If the POST is coming from mobile app or from Apache Jmeter, do not verify
            if (!(gRecaptchaResponse.equals("mobile") || request.getHeader("User-Agent").contains("Apache"))) {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            }
        } catch (Exception e) {
            
            responseJsonObject.addProperty("status", "reCAPTCHA fail");
            // Log to localhost log
            request.getServletContext().log("reCAPTCHA verification failed");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed");

            out.write(responseJsonObject.toString());

            // Set response status to 200
            response.setStatus(200);

            out.close();
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = SQLQueryTemplate.LOGIN_PAGE_;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, username);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            int row_count = 0;

            // Iterate through each row of rs
            while (rs.next()) {

                String userUserName = rs.getString("email");
                String userPassword = rs.getString("password");

                // use the encryptor to compare the user input password with encrypted password stored in DB
                Boolean loginSuccess = new StrongPasswordEncryptor().checkPassword(password, userPassword);

                if (username.equals(userUserName)) {
                    row_count += 1;
                    // Login success:
                    if (loginSuccess) {
                        
                        request.getSession().setAttribute("user", new User(username));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    } 
                    // Login fail:
                    else {
                        responseJsonObject.addProperty("status", "fail");
                        // Log to localhost log
                        request.getServletContext().log("Login failed");
                        responseJsonObject.addProperty("message", "Incorrect Password");
                    }

                }

            }
            rs.close();
            statement.close();
            conn.close();

            if (row_count == 0) {
                JsonObject jsonObject = new JsonObject();
                //jsonObject.addProperty("errorMessage", "Error: Username or Password Incorrect");

                request.getServletContext().log("Login failed");
                // Login fail due to invalid username
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "User " + username + " doesn't exist");

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
