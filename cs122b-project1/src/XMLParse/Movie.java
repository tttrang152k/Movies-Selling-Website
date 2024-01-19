package XMLParse;

import java.util.*;

public class Movie {

    private final String fid;

    private final String title;

    private final int year;

    // A movie can have more than one genre/cat
    //
    private final HashSet<Integer> categories;

    private final String director;

    public Movie(String fid, String title, int year, HashSet<Integer> categories, String director) {
        this.fid = fid;
        this.title = title;
        this.year = year;
        this.categories = categories;
        this.director = director;
    }

    public String getFid() {
        return fid;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public HashSet<Integer> getCategory() {
        return this.categories;
    }
    public String getDirector() {
        return director;
    }


    public String toString() {
        return "ID:" + getFid() + ", "+
                "Title:" + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Category:" + getCategory().toString() + ", "+
                "Director:" + getDirector() + ".";
    }

    public String[] getCSVFormat() {

        return new String[] {getFid(),getTitle(),Integer.toString(getYear()),getDirector()};
    }

    public boolean compareTo(Movie otherMovie) {
        if (this.fid == otherMovie.getFid() && this.title == otherMovie.getTitle() && this.year == otherMovie.getYear()) {
            // add other attr checks
            return true;
        }
        return false;
    }
}
