/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleBrowseResult(resultData) {
    console.log("handleStarResult: populating browse table from resultData");

        let toHTML = document.getElementById("genre_list");
        let para = "<p style=\"text-align: center\">";
        for (let i = 0; i < resultData.length; i = i + 4) {
            para += "<p style=\"text-align: center\">";
            let k = i;
            for (let j = 0; j < Math.min(4, resultData.length - i); j++) {
                para += '<a href="MovieList.html?genre=' + resultData[k]["genre_name"] + '">' + resultData[k]["genre_name"] + '</a>';
                para += "\n";
                k++;
            }
            para += "</p>";
        }
        para += "</p>";
        toHTML.innerHTML = para;

}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleBrowseResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});