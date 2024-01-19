/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function addToCartAlert(){
    alert('Added to shopping cart!');
}


function cartButtonHandler(target, movie_id){

    alert('Added to shopping cart!');
    console.log(target.toString());

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "POST", // Setting request method
        url: "api/cart?item=" + movie_id, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => addToCartAlert() // Setting callback function to handle data returned successfully by the StarsServlet
    });

}

function changePage(p) {
    // Get the url parameter values
    const url = window.location.search;
    const urlParams = new URLSearchParams(url);

    // default page number is 1
    let page_num = 1;

    if (urlParams.has('page')) {
        page_num = parseInt(urlParams.get('page'));
    }

    let page = 1;
    if (p == "prev" && page_num > 1) {
        page = page_num - 1;
    } else if (p == "next") {
        page = page_num + 1;
    } else {
        return // Dont do anything
    }


    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies-filter?page=" + page, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


/**
 * Handles the data returned by the shopping cart API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleCartResult(resultData) {

    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]["movie_id"]);
    }

}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleStarResult: populating movies table from resultData");

    // Populate the movies table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Get the number to limit restults by
    let l = document.getElementById("limit-select");
    let limit_by = l.value;

    // Get the url parameter values
    const url = window.location.search;
    const urlParams = new URLSearchParams(url);

    // default page number is 1
    let page_num = "1";

    if (urlParams.has('page')) {
        page_num = urlParams.get('page');
    }

    //let limit_by = parseInt(urlParams.get("result_limit"));

    //let offset_results = limit_by * (parseInt(page_num)-1);

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_name"] +     // display star_name for the link text
            '</a>' +
            "</th>";

        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_dir"] + "</th>";

        genre_array = resultData[i]["movie_gen"].split(",");
        rowHTML += "<th>";
        for (let i = 0; i < Math.min(5, genre_array.length); i++) {
            if (i == genre_array.length - 1){
                rowHTML +=
                    '<a href="MovieList.html?genre=' + genre_array[i] + '">'
                    + genre_array[i] +     // display movie_name for the link text
                    '</a>';
            }
            else {
                rowHTML +=
                    '<a href="MovieList.html?genre=' + genre_array[i] + '">'
                    + genre_array[i] +     // display movie_name for the link text
                    '</a>, ';
            }
        }
        rowHTML += "</th>";

        stars_array = resultData[i]["movie_star"].split(","); // Length 3
        stars_ids_array = resultData[i]["movie_star_ids"].split(","); // Length 3

        rowHTML += "<th>";
        for (let i = 0; i < Math.min(3, stars_array.length); i++) {
            if (i == stars_array.length - 1){
                rowHTML +=
                    '<a href="single-star.html?id=' + stars_ids_array[i] + '">'
                    + stars_array[i] +     // display star_name for the link text
                    '</a>';
            }
            else {
                rowHTML +=
                    '<a href="single-star.html?id=' + stars_ids_array[i] + '">'
                    + stars_array[i] +     // display star_name for the link text
                    '</a>, ';
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        //String: let mltitle = resultData[i]["movie_name"];
        //String: let movie_id = resultData[i]["movie_id"];
        rowHTML += "<th><button id='add-to-cart-btn' onclick='cartButtonHandler(\"" + resultData[i]["movie_name"] + "\",\"" + resultData[i]["movie_id"] + "\")'>+</button></th>";
        rowHTML += "</tr>";

            // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    let paginationElement = jQuery("#pages");
    let currWindow = window.location.href;

    let lim = "100";
    if (urlParams.has('result_limit')) {
        lim = urlParams.get('result_limit');
    }

    for (let i= 1; i <= Math.ceil(resultData.length / parseFloat(lim)); i++) {
        if (urlParams.has('page')) {
            page_num = urlParams.get('page');

            paginationElement.append(
                "<li class=\"page-item\">" +
                "<a class=\"page-link\" onclick=\"changePage(\"" + i + "\")\">" + i + "</a>" + "</li>");
        } else {
            paginationElement.append(
                "<li class=\"page-item\">" +
                "<a class=\"page-link\" onclick=\"changePage(\"" + i + "\")\">" + i + "</a>" + "</li>");
        }
    }
    paginationElement.append("<li class=\"page-item\"><a class=\"page-link\" onclick=\"changePage(\"next\")\">Next</a></li>");

}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Get parameters from URL
let title = getParameterByName('movie_title');
let year = getParameterByName('year');
let director_name = getParameterByName('director_name');
let star_name = getParameterByName('star_name');

let genre = getParameterByName('genre');
let prefix = getParameterByName('prefix');

let sort = getParameterByName('sort');
let limit = getParameterByName('result_limit');
let page = getParameterByName('page');

let ft_title = getParameterByName('ft_movie_title');
/*
if (ft_title != null) {
    console.log("getting param ft_movie_title");
    console.log(ft_title);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/fulltext-search?ft_movie_title=" + ft_title, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
*/
if (sort != null || limit != null){
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies-filter?sort=" + sort + "&result_limit=" + limit, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
else if (ft_title != null) {
    console.log("getting param ft_movie_title");
    console.log(ft_title);
    console.log("GET request to FTSearch");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/fulltext-search?ft_movie_title=" + ft_title, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
else {
    //console.log("else in MovieList");
    //console.log("title: " + title + " year: " + year + " director: " + director_name + " star: " + star_name);
    //console.log("genre: " + genre + " prefix: " + prefix);

    if (title == null && year == null && director_name == null && star_name == null) {
// Makes another HTTP GET request to MBrowse
        console.log("GET request to Mbrowse");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/movies-browse?genre=" + genre + "&prefix=" + prefix, // Setting request url, which is mapped by MoviesServlet in Movies.java
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
    else {
// Makes the HTTP GET request and registers on success callback function handleStarResult
        console.log("GET request to MSearch");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/movies-search?movie_title=" + title + "&director_name=" + director_name + "&year=" + year + "&star_name=" + star_name, // Setting request url, which is mapped by MoviesServlet in Movies.java
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        })
    }
};

/*
document.getElementById("ft_search_form").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent the form from submitting and refreshing the page
    // Get the form input values
    let ft_m_title = document.getElementsByName("ft_movie_title")[0].value;

    // Do something with the input values (e.g., display them)
    console.log("Ft m title:", ft_m_title);
    // Clear the form after processing
    document.getElementById("ft_search_form").reset();
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/fulltext-search?ft_movie_title=" + ft_m_title, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });

});

 */