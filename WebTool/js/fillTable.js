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

function fillSideTable(items){
    document.getElementById("sideMenu1").innerHTML = "";
    switch(items){
        case "players":
            httpGet('api/eco/players')
            .then(data => data.forEach(el => {
                document.getElementById("sideMenu1").innerHTML = document.getElementById("sideMenu1").innerHTML + "<div class='sideMenuItem'>" +
                        "<input type='radio' name='SideSelection' onClick=\"playerData(\'" + el.uuid + "\');\">" +
                       "<span>" + el.display_name + "</span>" +
                    "</div>";
            }));
            break;
        case "shops":
            httpGet('api/eco/shops')
            .then(data => data.forEach(el => {
                document.getElementById("sideMenu1").innerHTML = document.getElementById("sideMenu1").innerHTML + "<div class='sideMenuItem'>" +
                        "<input type='radio' name='SideSelection' onClick=\"playerData(\'" + el.uuid + "\');\">" +
                       "<span>" + el.display_name + "</span>" +
                    "</div>";
            }));
            break;
        case "items":
            httpGet('api/eco/items')
            .then(data => data.forEach(el => {
                document.getElementById("sideMenu1").innerHTML = document.getElementById("sideMenu1").innerHTML + "<div class='sideMenuItem'>" +
                        "<input type='radio' name='SideSelection' onClick=\"itemData(\'" + el.item_id + "\');\">" +
                       "<span>" + el.display_name + "</span>" +
                    "</div>";
            }));
            break;
        default:
            break;
    }
}

function playerData(uuid){
    document.getElementById("topmenu1").innerHTML = "<img src='https://crafatar.com/avatars/"+uuid+"'>"

    let t1head = document.getElementById("table1_head");
    t1head.innerHTML = wrapHead(["Item","Buying price", "Buying price<br>single item", "Selling price", "Selling price<br>single item", "Ammount per<br>Transaction", "In stock"]);

    let t1body = document.getElementById("table1");
    t1body.innerHTML = "";

    httpGet('api/eco/player/' + uuid + '/trades')
    .then(trades => trades.forEach(el => {
        let fullBuy = el.trade_price.buy_price * el.trade_price.quantity;
        let fullSell = el.trade_price.sell_price * el.trade_price.quantity;

        document.getElementById("table1").innerHTML = document.getElementById("table1").innerHTML +
        wrapBody([el.item.minecraft_id,fullBuy,el.trade_price.buy_price,fullSell,el.trade_price.sell_price,el.trade_price.quantity,el.trade_price.in_stock]);
    }));

}

function itemData(itemID){
    document.getElementById("topmenu1").innerHTML = "";

    let t1head = document.getElementById("table1_head");
    t1head.innerHTML = wrapHead(["Player","Location","Buying price", "Buying price<br>single item", "Selling price", "Selling price<br>single item", "Ammount per<br>Transaction", "In stock"]);

    let t1body = document.getElementById("table1");
    t1body.innerHTML = "";


    httpGet('api/eco/item/' + itemID)
    .then(data => {
        document.getElementById("topmenu1").innerHTML = "<img src='static/assets/items/" + data[0].minecraft_id.toLowerCase() + ".png'>";
        data[0].trades.forEach(el => {
            let fullBuy = el.trade_price.buy_price * el.trade_price.quantity;
            let fullSell = el.trade_price.sell_price * el.trade_price.quantity;

            document.getElementById("table1").innerHTML = document.getElementById("table1").innerHTML +
            wrapBody([el.player.display_name,el.trade_loc,fullBuy,el.trade_price.buy_price,fullSell,el.trade_price.sell_price,el.trade_price.quantity,el.trade_price.in_stock]);
        });
    });

}

function wrapHead(data){
    let r = "";
    for(let i = 0; i < data.length; i++){
        r = r + "<span class='DataHeader_item'>" + data[i] + "</span>";
    }
    return "<div class='DataHeader_Container'>" + r + "</div>";
}

function wrapBody(data){
    let r = "";
    for(let i = 0; i < data.length; i++){
        r = r +  "<span class='DataItem'>" + data[i] + "</span>";
    }
    return "<div class='DataItems'>" + r + "</div>";
}

function testTable(){
    let t = document.getElementById("table1");
    for(i = 0; i < 100; i++){
        t.innerHTML = t.innerHTML + "<div class='DataItems'>"+
                                "<span class='DataItem'>item1 " + i + "</span>"+
                                "<span class='DataItem'>item2 " + i + "</span>"+
                                "<span class='DataItem'>item3 " + i + "</span>"+
                            "</div>";
    }
}