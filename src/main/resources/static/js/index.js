// TASK: Make success rate td green, orange or red depending on percentage
// #1: When page loads, get all sr td elements
// #2: For each element, read values. Assign colour based on value

window.addEventListener("load", assignSuccessRateColours, 0);

function assignSuccessRateColours() {
    var successRateElements = document.getElementsByName("successRateTableData");
    console.log("COUNT -  " + successRateElements.length);
    successRateElements.forEach(element => {
        var successRate = getSuccessRate(element);
        console.log("SR - " + successRate);
        if (successRate < 40.0) 
            element.innerHTML = '<span style="color:red">' + successRate + '%</span>';
        else if (successRate > 39.99 && successRate < 80.0)
            element.innerHTML = '<span style="color:darkorange">' + successRate + '%</span>';
        else
            element.innerHTML = '<span style="color:green">' + successRate + '%</span>';
    });
}

function getSuccessRate(element) {
    var elText = element.innerText;
    var valText = elText.substring(0, elText.length-1);
    var value = parseFloat(valText);
    return value;
}