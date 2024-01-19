import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/cart")
public class cartItems extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        Map<String, Integer> previousItems = (Map<String, Integer>) session.getAttribute("cartItems");
        if (previousItems == null) {
            previousItems = new HashMap<>();
            session.setAttribute("cartItems", previousItems);
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonArray previousItemsJsonArray = new JsonArray();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Construct a query to fetch movies and its price
            String query = SQLQueryTemplate.CART_PAGE_;

            for (String m_id : previousItems.keySet()) {
                query = query + "m.id = ? or ";
            }

            if (previousItems.isEmpty()) {
                request.getServletContext().log("getting NO results");
                // Set response status to 200 (OK)
                response.setStatus(200);
                return;// NO items in cart
            }

            query = query.substring(0, query.length() - 4);     // Eliminate the last 'or '

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // fill in the movie id
            int index = 1;
            for (String m_id : previousItems.keySet()) {
                statement.setString(index, m_id);
                index++;
            }

            System.out.println(statement);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            float checkout_price = 0;
            // Iterate through each row of rs. rs should be in format title|price
            while (rs.next()) {

                String m_title = rs.getString("title");
                String m_id = rs.getString("id");
                String m_price = rs.getString("price");
                checkout_price +=  previousItems.get(m_id) *  Integer.parseInt(m_price);

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", m_title);
                jsonObject.addProperty("movie_price", m_price);
                jsonObject.addProperty("amount", previousItems.get(m_id));

                previousItemsJsonArray.add(jsonObject);

            }
            session.setAttribute("checkoutTotal", checkout_price);
            rs.close();
            statement.close();
            conn.close();

            // Log to localhost log
            request.getServletContext().log("getting " + previousItemsJsonArray.size() + " results");

            // Write JSON string to output
            out.write(previousItemsJsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

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

        // add items to json array
        //previousItems.forEach(previousItemsJsonArray::add);
        //responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        //response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        System.out.println("Item Added: " + item);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        //ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        Map<String, Integer> cartItems = (Map<String, Integer>) session.getAttribute("cartItems");
        //JsonObject cartItems = (JsonObject) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new HashMap<>();
            session.setAttribute("cartItems", cartItems);
        }
        if (cartItems.get(item) == null) {
            cartItems.put(item, 1);
            session.setAttribute("cartItems", cartItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            //cartItems = new HashMap<String, Integer>();
            synchronized (cartItems) {
                if (cartItems.get(item) == null)
                    cartItems.put(item, 1);
                else {
                    cartItems.put(item, cartItems.get(item) + 1);
                }
                session.setAttribute("cartItems", cartItems);
            }
        }
        /*
        JsonObject responseJsonObject = new JsonObject();
        JsonArray responseJsonArray = new JsonArray();
        //previousItems.forEach(previousItemsJsonArray::add);

        for (Map.Entry<String,Integer> entry : cartItems.entrySet()) {
            responseJsonObject.addProperty("movie_id", entry.getKey());
            responseJsonObject.addProperty("amount", entry.getValue());
            responseJsonArray.add(responseJsonObject);
        }
        response.getWriter().write(responseJsonObject.toString());

         */
        Map<String, Integer> cartItems2 = (Map<String, Integer>) session.getAttribute("cartItems");
        System.out.println("Session Item Added: " + cartItems2.get(item));

        //response.setStatus(200);
    }

}
