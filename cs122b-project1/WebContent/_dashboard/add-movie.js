let addMovie_form = $("#add_movie_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleAddMovieResult(resultDataString) {

    console.log(resultDataString["message"]);
    let resultDataJson = resultDataString;
    //let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add movie response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        $("#add_movie_message").text(resultDataJson["message"]);
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "checkout_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add_movie_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitAddMovieForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add_movie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: addMovie_form.serialize(),
            success: handleAddMovieResult
        }
    );
}

// Bind the submit action of the form to a handler function
addMovie_form.submit(submitAddMovieForm);

