import java.io.IOException;
import java.util.HashMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/hero-suggestion")
public class MovieSuggestion extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /*
     * populate the Super hero hash map.
     * Key is hero ID. Value is hero name.
     */
    public static HashMap<Integer, String> superHeroMap = new HashMap<>();
    static {
        superHeroMap.put(1, "Blade");
        superHeroMap.put(2, "Ghost Rider");
        superHeroMap.put(3, "Luke Cage");
        superHeroMap.put(4, "Silver Surfer");
        superHeroMap.put(5, "Beast");
        superHeroMap.put(6, "Thing");
        superHeroMap.put(7, "Black Panther");
        superHeroMap.put(8, "Invisible Woman");
        superHeroMap.put(9, "Nick Fury");
        superHeroMap.put(10, "Storm");
        superHeroMap.put(11, "Iron Man");
        superHeroMap.put(12, "Professor X");
        superHeroMap.put(13, "Hulk");
        superHeroMap.put(14, "Cyclops");
        superHeroMap.put(15, "Thor");
        superHeroMap.put(16, "Jean Grey");
        superHeroMap.put(17, "Wolverine");
        superHeroMap.put(18, "Daredevil");
        superHeroMap.put(19, "Captain America");
        superHeroMap.put(20, "Spider-Man");
        superHeroMap.put(101, "Superman");
        superHeroMap.put(102, "Batman");
        superHeroMap.put(103, "Wonder Woman");
        superHeroMap.put(104, "Flash");
        superHeroMap.put(105, "Green Lantern");
        superHeroMap.put(106, "Catwoman");
        superHeroMap.put(107, "Nightwing");
        superHeroMap.put(108, "Captain Marvel");
        superHeroMap.put(109, "Aquaman");
        superHeroMap.put(110, "Green Arrow");
        superHeroMap.put(111, "Martian Manhunter");
        superHeroMap.put(112, "Batgirl");
        superHeroMap.put(113, "Supergirl");
        superHeroMap.put(114, "Black Canary");
        superHeroMap.put(115, "Hawkgirl");
        superHeroMap.put(116, "Cyborg");
        superHeroMap.put(117, "Robin");
    }

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String title_query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (title_query == null || title_query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }
            String[] tokens = title_query.split(" ");

            String query = SQLQueryTemplate.FULL_TEXT_MOVIE_SUGGESTION_;
            PreparedStatement statement = conn.prepareStatement(query);

            String ft_tokens = "";

            for (String token : tokens)
                ft_tokens += " +" + token + "*";

            //statement.setString(1, "%" + title_query + "%");
            statement.setString(1, ft_tokens);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // Iterate through each row of rs
            int limit = 10;
            int count = 0;
            while (rs.next() && count < 10) {
                String movie_id = rs.getString("id");
                String movie_name = rs.getString("title");
                jsonArray.add(generateJsonObject(movie_id, movie_name));
                count++;
            }
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
        // Always remember to close db connection after usage. Here it's done by try-with-resources
        /*
        try {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars

            for (Integer id : superHeroMap.keySet()) {
                String heroName = superHeroMap.get(id);
                if (heroName.toLowerCase().contains(query.toLowerCase())) {
                    jsonArray.add(generateJsonObject(id, heroName));
                }
            }
            response.getWriter().write(jsonArray.toString());
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }

         */
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        //additionalDataJsonObject.addProperty("heroID", heroID);
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
