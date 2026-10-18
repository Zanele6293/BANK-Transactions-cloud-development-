let currentUser="";

function login(){
    let input = document.getElementById("username-input").value.trim();
    if(!input){ alert("Please enter a username");return;}
    currentUser=input;

    document.getElementById("display-user").innerText=currentUser;
    document.getElementById("login-section").style.display="none";
    document.getElementById("app-section").style.display="block";

    refreshData();
}

function logout() {
    currentUser = "";
    document.getElementById('username-input').value = "";
    document.getElementById('app-section').style.display = 'none';
    document.getElementById('login-section').style.display = 'block';
}

async function refreshData() {
    fetchBalance();
    fetchHistory();
}

async function fetchBalance() {
    let response = await fetch(`/api/transactions/balance?username=${currentUser}`);
    let balance = await response.text();
    document.getElementById('balance').innerText = "R " + balance;
}
