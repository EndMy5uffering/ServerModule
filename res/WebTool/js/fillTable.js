async function httpGet(apiCall)
{
    var options = {
            method: "GET",
            mode: 'cors', // no-cors, *cors, same-origin
        };

    const result = await fetch(apiCall, options)
    .catch(rejected => {
        console.log(rejected);
    });
    return result.json();
}

async function httpPost(apiCall, data)
{
    var options = {
            method: "POST",
            mode: 'cors', // no-cors, *cors, same-origin
            body: data
        };

    const result = await fetch(apiCall, options)
    .catch(rejected => {
        console.log(rejected);
    });
    return result.json();
}

async function httpPut(apiCall, data)
{
    var options = {
            method: "PUT",
            mode: 'cors', // no-cors, *cors, same-origin
            body: data
        };

    const result = await fetch(apiCall, options)
    .catch(rejected => {
        console.log(rejected);
    });
    return result.json();
}

function testRequest()
{
    var data = {
        "data" : "value",
        "moredata" :"even more value"
    };
    
    console.log(data);
    
    httpPost('/api/test?test=value', JSON.stringify(data)).then(data => {
        
        console.log("Got some responce: " + data.message);
        
    });
}