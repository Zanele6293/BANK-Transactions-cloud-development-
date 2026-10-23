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

async function registerUser() {
    let username = document.getElementById('reg-username').value.trim();
    let surname = document.getElementById('reg-surname').value.trim();
    let password = document.getElementById('reg-password').value;
    let deposit = document.getElementById('reg-deposit').value;
    let fileInput = document.getElementById('reg-image');
    let msgBox = document.getElementById('auth-msg');

    if (!username || !surname || !password || !deposit) {
        msgBox.innerText = "Please fill in all account fields.";
        return;
    }

    let defaultAvatar = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";
    let base64Image = defaultAvatar;

    if (fileInput.files && fileInput.files[0]) {
        let reader = new FileReader();
        reader.readAsDataURL(fileInput.files[0]);
        reader.onload = async function () {
            base64Image = reader.result;
            await sendRegistration(username, surname, password, deposit, base64Image);
        };
    } else {
        await sendRegistration(username, surname, password, deposit, base64Image);
    }
}

async function sendRegistration(username, surname, password, deposit, image) {
    let formData = new URLSearchParams();
    formData.append("username", username);
    formData.append("surname", surname);
    formData.append("password", password);
    formData.append("initialDeposit", deposit);
    formData.append("profileImage", image);

    let response = await fetch('/api/transactions/register', { method: 'POST', body: formData });
    let result = await response.text();

    let msgBox = document.getElementById('auth-msg');
    if (result.startsWith("ERROR:")) {
        msgBox.className = "error-msg";
        msgBox.innerText = result.replace("ERROR:", "");
    } else {
        msgBox.className = "success-msg";
        msgBox.innerText = "Account created! Please sign in.";
        toggleAuthMode();
    }
}

async function loginUser() {
    let username = document.getElementById('login-username').value.trim();
    let password = document.getElementById('login-password').value;
    let msgBox = document.getElementById('auth-msg');

    if (!username || !password) {
        msgBox.innerText = "Enter both username and password.";
        return;
    }

    let formData = new URLSearchParams();
    formData.append("username", username);
    formData.append("password", password);

    let response = await fetch('/api/transactions/login', { method: 'POST', body: formData });
    let result = await response.text();

    if (result.startsWith("ERROR:")) {
        msgBox.className = "error-msg";
        msgBox.innerText = result.replace("ERROR:", "");
    } else {
        currentUser = username;
        document.getElementById('auth-screen').style.display = 'none';
        document.getElementById('dashboard-screen').style.display = 'block';
        document.getElementById('app-header').style.display = 'flex';
        document.getElementById('app-nav').style.display = 'flex';

        loadUserProfile();
        switchView('overview');
    }
}

function logout() {
    currentUser = "";
    document.getElementById('dashboard-screen').style.display = 'none';
    document.getElementById('auth-screen').style.display = 'block';
    document.getElementById('app-header').style.display = 'none';
    document.getElementById('app-nav').style.display = 'none';
    document.getElementById('login-username').value = "";
    document.getElementById('login-password').value = "";
    document.getElementById('auth-msg').innerText = "";
}

function switchView(viewName) {
    document.getElementById('view-overview').style.display = 'none';
    document.getElementById('view-investments').style.display = 'none';
    document.getElementById('view-history').style.display = 'none';

    if (viewName === 'overview') {
        document.getElementById('view-overview').style.display = 'block';
        fetchBalances();
    } else if (viewName === 'investments') {
        document.getElementById('view-investments').style.display = 'block';
        fetchInvestments();
    } else if (viewName === 'history') {
        document.getElementById('view-history').style.display = 'block';
        fetchHistory();
    }
}

async function loadUserProfile() {
    let response = await fetch(`/api/transactions/profile?username=${currentUser}`);
    let profile = await response.json();

    document.getElementById('header-name').innerText = profile.username + " " + profile.surname;
    document.getElementById('welcome-name').innerText = profile.username + " " + profile.surname;
    document.getElementById('card-holder-name').innerText = (profile.username + " " + profile.surname).toUpperCase();

    if (profile.profileImage) {
        document.getElementById('header-avatar').src = profile.profileImage;
    }
    fetchBalances();
}

