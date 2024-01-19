let cart = $("#cart");


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
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataJson) {
    //let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    //console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information
    //$("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    //$("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultData) {
    let cart_table = jQuery("#cart_table_body");
    // change it to html list

    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            resultData[i]["movie_title"] +     // display star_name for the link text
            "</th>";

        rowHTML += "<th>" + resultData[i]["movie_price"] + "</th>";
        rowHTML += "<th>" + resultData[i]["amount"] + "</th>";

        rowHTML += "<th>$" + parseInt(resultData[i]["amount"]) * parseFloat(resultData[i]["movie_price"]) + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        cart_table.append(rowHTML);
    }
}

function handleCartArray2(resultDataJson) {
    console.log('POST dumped');
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart",
    success: (resultData) => handleSessionData(resultData)
});

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();
    let item = getParameterByName('item')
    $.ajax("api/cart?item=" + item, {
        method: "POST",
        data: cart.serialize(),
    });

    // clear input form
    cart[0].reset();
}

/*
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET",
    url: "api/cart",
    success: (resultData) => handleCartArray(resultData)
});
*/

