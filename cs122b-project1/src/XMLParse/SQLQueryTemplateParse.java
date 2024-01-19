package XMLParse;

public class SQLQueryTemplateParse {
    public static final String INSERT_TO_STARS_ = "select add_star(?, ?) as star_id";
    public static final String INSERT_TO_MOVIES_ = "call insert_movie(?, ?, ?, ?)";
    public static final String LINK_GENRES_TO_MOVIE_ = "call link_genre_movie(?, ?);";

    public static final String LINK_STARS_TO_MOVIES_ = "call link_star_movie(?, ?)";

    public static final String INSERT_TO_GENRES_ = "select add_genre(?) as genre_id;";

}
