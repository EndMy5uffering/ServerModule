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

async function httpGetText(apiCall)
{
    var options = {
            method: "GET",
            mode: 'cors', // no-cors, *cors, same-origin
        };

    const result = await fetch(apiCall, options)
    .catch(rejected => {
        console.log(rejected);
    });
    return result.text();
}