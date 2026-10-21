let currentUser = "";

function toggleAuthMode() {
    let loginBox = document.getElementById('login-box');
    let regBox = document.getElementById('register-box');
    let msg = document.getElementById('auth-msg');
    msg.innerText = "";

    if (loginBox.style.display === "none") {
        loginBox.style.display = "block";
        regBox.style.display = "none";
    } else {
        loginBox.style.display = "none";
        regBox.style.display = "block";
    }
}

function formatName(text) {
    if (!text) return "";
    text = text.trim().toLowerCase();
    return text.charAt(0).toUpperCase() + text.slice(1);
}

function toggleAccount(id) {
    let box = document.getElementById(id);
    box.style.display = box.style.display === "block" ? "none" : "block";
}
