package XMLParse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DomParser implements Parameters {

    private static Connection connection;

    /*
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
     */

    public DomParser() throws IOException {
    }

    // Array list of movies
    ArrayList<Movie> movies = new ArrayList<>();

    // HashMap movie to list of cats -- movieId : list(genreId)
    HashMap movie_to_stars = new HashMap<String, ArrayList<String>>();

    // HashMap genre to its id -- genre_name : genreId (INT)
    HashMap genres = new HashMap<String, Integer>();

    // HashMap stars to their birth_year -- star_name : list(birth_year)
    // List of birth year in case same star name with different birth year
    HashMap stars = new HashMap<String, ArrayList<Integer>>();

    HashMap stars_to_starID = new HashMap<String, String>();

    // Use to keep track of added movies -- by movie ids
    HashSet<String> inserted_movies = new HashSet<String>();


    int dup_movie = 0;
    int dup_actor = 0;
    int inconsistent_cast = 0;
    int inconsistent_movie = 0;
    int null_movie = 0;     // null movieId or title
    int movies_add = 0;
    int actors_parsed = 0;
    int genres_add = 0;

    int link_genre_movie = 0;

    int link_star_movie = 0;

    int star_not_found = 0;

    Document dom_movie;
    Document dom_actor;
    Document dom_cast;

    public void runExample() {

        // parse the main xml file and get the dom movie object
        parseMainXmlFile();

        parseMainDocument();

        // parse the actor xml file and get the dom actor object
        parseActorXML();

        parseActorDocument();

        // parse the cast xml file and get the dom cast object
        parseCastXML();

        parseCastDocument();

        // iterate through the list and print the data

        System.out.println("Parsed: " + movies_add + " movies");
        System.out.println("Parsed: " + stars.size() + " stars");
        System.out.println("Parsed: " + genres_add + " genres");

    }

    private void parseMainXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom_movie = documentBuilder.parse("stanford-movies/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseMainDocument(){
        // get the document root Element
        Element documentElement = dom_movie.getDocumentElement();

        // Using HashSet for better search
        Set<String> movieTitles = new HashSet<String>();
        Set<String> movieIds = new HashSet<String>();

        // get a nodelist of movie Elements, parse each into movie object
        NodeList nodeList = documentElement.getElementsByTagName("film");
        for (int i = 0; i < nodeList.getLength(); i++) {
            // get the movie element
            Element element = (Element) nodeList.item(i);
            // get the movie object
            Movie new_movie = parseMovie(element);
            // check for duplicated or invalid here
            if (new_movie.getFid() == null || new_movie.getTitle() == null || new_movie.getCategory().isEmpty())
                null_movie++;
            else if (movieTitles.contains(new_movie.getTitle()) && movieIds.contains(new_movie.getFid()))
                dup_movie++;
            else {      // brand new movie
                movieTitles.add(new_movie.getTitle());
                movieIds.add(new_movie.getFid());
                movies.add(new_movie);
                movies_add++;
            }
            //System.out.println(new_movie.toString());
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Movie parseMovie(Element element) {
        // for each <movie> element get text or int values of
        // fid ,year, title, dir, and cats
        String fid = getTextValue(element, "fid");
        int year = getIntValue(element, "year");
        String title = getTextValue(element, "t");
        String dir = getTextValue(element,"dirn");

        // Store set of genres to put in this movie
        HashSet<Integer> genreIds = new HashSet<Integer>();

        PreparedStatement statement = null;

        try {
            // A movie might have multiple <cat*>
            NodeList cats = element.getElementsByTagName("cat");

            for (int i = 0; i < cats.getLength(); i++) {
                if (cats.item(i).getFirstChild() != null) {

                    String cat_name = cats.item(i).getFirstChild().getNodeValue();

                    if (cat_name == null) {
                        //System.out.println("cat name is null");
                        continue;
                    }
                    else {
                        cat_name = cat_name.trim();     // there are some genre with same name but abundant whitespace
                        if (!genres.containsKey(cat_name)){     // new genre is not in genres list yet
                            // Add new genre to genres table
                            String query = SQLQueryTemplateParse.INSERT_TO_GENRES_;
                            statement = connection.prepareStatement(query);
                            statement.setString(1, cat_name);
                            //System.out.println("Statement before execution: " + statement);
                            ResultSet rs = statement.executeQuery();
                            genres_add++;
                            while(rs.next()){
                                int cat_id = rs.getInt("genre_id");
                                genres.put(cat_name, cat_id);
                                genreIds.add(cat_id);
                            }
                            rs.close();
                        }
                        else {      // if genre is already in genres list. add this genre id to movie
                            genreIds.add((Integer.parseInt(genres.get(cat_name).toString())));
                        }
                        if (statement != null)
                            statement.close();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        // create a new Movie with the value read from the xml nodes
        return new Movie(fid,  title,  year,  genreIds,  dir);
    }

    private void parseActorXML() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom_actor = documentBuilder.parse("stanford-movies/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseActorDocument() {
        // get the document root Element
        Element documentElement = dom_actor.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                // get the employee element
                Element actor = (Element) nodeList.item(i);
                //parse and add entries into map
                parseActor(actor);
            }
        }
    }

    private void parseActor(Element element) {
        String star_name = getTextValue(element, "stagename");
        int new_birth_year = getIntValue(element, "dob");
        //System.out.println("star name: " + star_name + " byear: " + new_birth_year);
        int duplicated = 0;     // check if a star is duplicated -- no add in that case

        ArrayList<Integer> append_by = new ArrayList<Integer>();
        append_by.add(new_birth_year);

        if (stars.containsKey(star_name)) {     // if a star already in stars. check birth year
            ArrayList<Integer> curr_by = (ArrayList<Integer>) stars.get(star_name);
            if (curr_by.contains(new_birth_year)) {
                dup_actor++;
                duplicated = 1;
            }
            else {    // new birth year of the same star
                curr_by.add(new_birth_year);
                stars.put(star_name, curr_by);
            }
        }
        else {      // add brand new star into stars
            stars.put(star_name, append_by);
        }

        // Insert new star into SQL
        try {

            // if not duplicated star. insert into stars table
            if (duplicated == 0) {

                String query = SQLQueryTemplateParse.INSERT_TO_STARS_;
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, star_name);
                // check if year is null
                if (new_birth_year == -1)
                    statement.setNull(2, Types.INTEGER);
                else
                    statement.setInt(2, new_birth_year);

                //System.out.println("query before execution: " + statement);
                ResultSet rs = statement.executeQuery();
                //actors_parsed++;

                if (rs.next()) {
                    String new_star_id = rs.getString("star_id");
                    stars_to_starID.put(star_name, new_star_id);
                }
                rs.close();
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseCastXML() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom_cast = documentBuilder.parse("stanford-movies/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseCastDocument() {
        // get the document root Element
        Element documentElement = dom_cast.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("filmc");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                // get the employee element
                Element cast = (Element) nodeList.item(i);
                //parse and add entries into map
                parseCast(cast);
            }
        }
    }

    private void parseCast(Element element) {
        String fid = getTextValue(element, "f");
        // Store list of stars associated with this fid
        ArrayList<String> star_names = new ArrayList<String>();

        NodeList casts = element.getElementsByTagName("a");
        HashSet<String> curr_casts = new HashSet<String>();
        for (int i = 0; i < casts.getLength(); i++) {
            if (casts.item(i).getFirstChild() != null && !curr_casts.contains(casts.item(i).getFirstChild().getNodeValue())) {
                String star_name = casts.item(i).getFirstChild().getNodeValue();
                curr_casts.add(star_name);
                star_names.add(star_name);
            }
            else {
                inconsistent_cast++;
            }
        }
        movie_to_stars.put(fid, star_names);
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            if (nodeList.item(0).getFirstChild() != null) {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            }

        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        int return_int = 0;
        try {
            return_int = Integer.parseInt(getTextValue(ele, tagName));
        } catch(NumberFormatException e){
            return -1;
        }
        return return_int;
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {

        System.out.println("Total parsed " + movies.size() + " movies");

        for (Movie movie : movies) {
            System.out.println("\t" + movie.toString());
        }
    }

    private void insertToMovies() {
        try {
            // duplicated movie will not be added -- implemented in the stored procedure
            String query = SQLQueryTemplateParse.INSERT_TO_MOVIES_;
            //PreparedStatement statement = connection.prepareStatement(query);
            CallableStatement statement = connection.prepareCall(query);
            // set for batch insert
            connection.setAutoCommit(false);
            int batchCount = 0;

            for (int i = 0; i < movies.size(); i++) {
                String movieId = movies.get(i).getFid();
                String m_title = movies.get(i).getTitle();
                int m_year = movies.get(i).getYear();
                String m_director = movies.get(i).getDirector();

                // m_year is a required field. if year is null then pass
                if (movieId == null || m_title == null || m_year == -1 || m_director == null || inserted_movies.contains(movieId)) {
                    inconsistent_movie++;
                    continue;
                }
                inserted_movies.add(movieId);
                // set values to statement
                statement.setString(1, movieId);
                statement.setString(2, m_title);
                statement.setInt(3, m_year);
                statement.setString(4, m_director);
                statement.addBatch();

                batchCount++;
                if (batchCount % 2000 == 0)  {  // execute every 2000 queries
                    statement.executeBatch();
                    connection.commit();
                }
            }
            // execute the rest of remaining queries in the batch
            statement.executeBatch();
            connection.commit();

            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void linkGenreToMovie() {
        try {
            // duplicated movie will not be added -- implemented in the stored procedure
            String query = SQLQueryTemplateParse.LINK_GENRES_TO_MOVIE_;
            //PreparedStatement statement = connection.prepareStatement(query);
            CallableStatement statement = connection.prepareCall(query);
            // set for batch insert
            connection.setAutoCommit(false);
            int batchCount = 0;

            for (int i = 0; i < movies.size(); i++) {
                String movieId = movies.get(i).getFid();

                // if movie is not in the already inserted movies list. then it would not appear in the movies table
                if (!inserted_movies.contains(movieId)) {
                    inconsistent_movie++;
                    continue;
                }

                HashSet<Integer> genres_set = (HashSet<Integer>) movies.get(i).getCategory();

                for (Integer genre_id : genres_set) {
                    statement.setInt(1, genre_id);
                    statement.setString(2, movieId);
                    link_genre_movie++;
                    statement.addBatch();
                    batchCount++;
                    if (batchCount % 2000 == 0) {
                        statement.executeBatch();
                        connection.commit();
                    }
                }
                if (batchCount % 2000 == 0)  {  // execute every 2000 queries
                    statement.executeBatch();
                    connection.commit();
                }
            }
            // execute the rest of remaining queries in the batch
            statement.executeBatch();
            connection.commit();

            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void linkStarToMovie() {

        // HashMap starId to movieIds
        HashMap<String, HashSet<String>> starId_to_movieIds = new HashMap<String, HashSet<String>>();

        try {
            // duplicated movie will not be added -- implemented in the stored procedure
            String query = SQLQueryTemplateParse.LINK_STARS_TO_MOVIES_;
            CallableStatement statement = connection.prepareCall(query);
            // set for batch insert
            connection.setAutoCommit(false);
            int batchCount = 0;

            // Get the inserted movies list in movies table
            Iterator<String> iter = inserted_movies.iterator();
            while (iter.hasNext()) {
                String movieId = iter.next();
                // get list of stars from this movieId
                ArrayList<String> stars_list = (ArrayList<String>) movie_to_stars.get(movieId);

                // avoid NullPointerException !!!!
                if (stars_list == null) {
                    star_not_found++;
                    continue;
                }

                for (int i = 0; i < stars_list.size(); i++) {
                    // get starId from HashMap star_to_starId
                    String starId = (String) stars_to_starID.get(stars_list.get(i));

                    if (starId == null) {
                        inconsistent_cast++;
                        continue;
                    }
                    if (starId_to_movieIds.containsKey(starId) && starId_to_movieIds.get(starId).contains(movieId)) {
                        System.out.println("starId map to movie ids: movieId already in");
                        continue;
                    }

                    if (!starId_to_movieIds.containsKey(starId)) {  // add new movieId to new starId
                        HashSet<String> new_set = new HashSet<String>();
                        new_set.add(movieId);
                        starId_to_movieIds.put(starId, new_set);
                    }
                    else {  // starId already in HashMap but movieId is new
                        starId_to_movieIds.get(starId).add(movieId);
                    }

                    // add each starId with distinct movieId into execute batch
                    statement.setString(1, starId);
                    statement.setString(2, movieId);
                    link_star_movie++;
                    statement.addBatch();
                    batchCount++;

                    if (batchCount % 2000 == 0)  {  // execute every 2000 queries
                        statement.executeBatch();
                        connection.commit();
                    }
                }
            }
            // execute the rest of remaining queries in the batch
            statement.executeBatch();
            connection.commit();

            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] arg) throws Exception {

        // create an instance
        DomParser dp = new DomParser();

        // Incorporate mySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Connect to the test database
        connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                Parameters.username, Parameters.password);

        if (connection != null) {
            System.out.println("Connection established!!");

            dp.runExample();
            dp.insertToMovies();
            dp.linkGenreToMovie();
            dp.linkStarToMovie();

            System.out.println("Inserted " + dp.genres.size() + " genres");
            System.out.println("Inserted " + dp.stars_to_starID.size() + " stars");
            System.out.println("Inserted " + dp.inserted_movies.size() + " movies");
            System.out.println("Inserted " + dp.link_genre_movie + " into genres_in_movie");
            System.out.println("Inserted " + dp.link_star_movie + " into stars_in_movie");
            System.out.println("Inconsistent movies: " + dp.inconsistent_movie);
            System.out.println("Dup movie: " + dp.dup_movie);
            System.out.println("Null movie: " + dp.null_movie);
            System.out.println("Inconsistent stars: " + dp.inconsistent_cast);
            System.out.println("Dup star: " + dp.dup_movie);
            System.out.println("Stars not found: " + dp.star_not_found);


        } else {
            System.out.println("no connection");
        }
    }
}
