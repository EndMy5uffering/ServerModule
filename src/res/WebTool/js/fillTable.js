//var table = document.getElementById("mytable1");

//badading.ddnss.de:5566



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
            body: "test"
        };

    const result = await fetch(apiCall, options)
    .catch(rejected => {
        console.log(rejected);
    });
    return result.json();
}

function APICALL(){
    httpPost('api/testcall?test=asdf', { "test" : "someData"}).then((data) => {
        alert('got data');
        alert(data.message);
    });
}