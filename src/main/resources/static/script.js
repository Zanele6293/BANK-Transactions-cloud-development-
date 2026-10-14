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