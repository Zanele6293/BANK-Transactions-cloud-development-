let currentUser = "";

function toggleAuthMode() {
    let loginBox = document.getElementById('login-box');
    let regBox = document.getElementById('register-box');
    let msg = document.getElementById('auth-msg');
    if(msg) msg.innerText = "";

    if (loginBox.style.display === "none") {
        loginBox.style.display = "block";
        regBox.style.display = "none";
    } else {
        loginBox.style.display = "none";
        regBox.style.display = "block";
    }
}

function toggleAccount(id) {
    let box = document.getElementById(id);
    if(box) {
        box.style.display = box.style.display === "block" ? "none" : "block";
    }
}

function formatName(text) {
    if (!text) return "";
    text = text.trim().toLowerCase();
    return text.charAt(0).toUpperCase() + text.slice(1);
}

async function registerUser() {
    let usernameField = document.getElementById('reg-username');
    let surnameField = document.getElementById('reg-surname');
    let passwordField = document.getElementById('reg-password');
    let depositField = document.getElementById('reg-deposit');
    let fileInput = document.getElementById('reg-image');
    let msgBox = document.getElementById('auth-msg');

    let username = formatName(usernameField.value);
    let surname = formatName(surnameField.value);
    let password = passwordField.value;
    let deposit = depositField.value;

    if (!username || !surname || !password || !deposit) {
        msgBox.className = "error-msg";
        msgBox.innerText = "Please fill in all account fields.";
        return;
    }

    let formData = new FormData();
    formData.append("username", username);
    formData.append("surname", surname);
    formData.append("password", password);
    formData.append("initialDeposit", deposit);

    if (fileInput.files && fileInput.files[0]) {
        formData.append("profileImage", fileInput.files[0]);
    }

    // Disable button to prevent double-click duplication glitches
    let regButton = event.target;
    regButton.disabled = true;
    regButton.innerText = "Processing...";

    try {
        let response = await fetch('/api/transactions/register', {
            method: 'POST',
            body: formData
        });
        let result = await response.text();

        if (result.startsWith("ERROR:")) {
            msgBox.className = "error-msg";
            msgBox.innerText = result.replace("ERROR:", "");
            regButton.disabled = false;
            regButton.innerText = "Register Account";
        } else {
            msgBox.className = "success-msg";
            msgBox.innerText = "Account created successfully! Please sign in.";
            setTimeout(() => {
                toggleAuthMode();
                regButton.disabled = false;
                regButton.innerText = "Register Account";
            }, 1000);
        }
    } catch (err) {
        console.error("Registration error:", err);
        regButton.disabled = false;
        regButton.innerText = "Register Account";
    }
}


async function loginUser() {
    let username = formatName(document.getElementById('login-username').value);
    let password = document.getElementById('login-password').value;
    let msgBox = document.getElementById('auth-msg');

    if (!username || !password) {
        msgBox.innerText = "Enter both username and password.";
        return;
    }

    let formData = new URLSearchParams();
    formData.append("username", username);
    formData.append("password", password);

    try {
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
    } catch (err) {
        console.error(err);
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
    let msg = document.getElementById('auth-msg');
    if(msg) msg.innerText = "";
}

function switchView(viewName) {
    let ov = document.getElementById('view-overview');
    let inv = document.getElementById('view-investments');
    let hist = document.getElementById('view-history');

    if(ov) ov.style.display = 'none';
    if(inv) inv.style.display = 'none';
    if(hist) hist.style.display = 'none';

    if (viewName === 'overview') {
        if(ov) ov.style.display = 'block';
        fetchBalances();
    } else if (viewName === 'investments') {
        if(inv) inv.style.display = 'block';
        fetchInvestments();
    } else if (viewName === 'history') {
        if(hist) hist.style.display = 'block';
        fetchHistory();
    }
}

async function loadUserProfile() {
    try {
        let response = await fetch(`/api/transactions/profile?username=${currentUser}`);
        if (!response.ok) return;
        let profile = await response.json();
        if(!profile) return;

        let fullName = (profile.username || currentUser) + " " + (profile.surname || "");
        let headerName = document.getElementById('header-name');
        let welcomeName = document.getElementById('welcome-name');
        let cardHolder = document.getElementById('card-holder-name');

        if(headerName) headerName.innerText = fullName;
        if(welcomeName) welcomeName.innerText = fullName;
        if(cardHolder) cardHolder.innerText = fullName.toUpperCase();

        // Render the uploaded profile picture URL
        let headerAvatar = document.getElementById('header-avatar');
        if (headerAvatar) {
            if (profile.profileImage && profile.profileImage.trim() !== "") {
                headerAvatar.src = profile.profileImage;
            } else {
                headerAvatar.src = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";
            }
        }
    } catch (err) {
        console.error("Error loading profile:", err);
    }
    fetchBalances();
}

async function fetchBalances() {
    try {
        let resCurrent = await fetch(`/api/transactions/balance/current?username=${currentUser}`);
        let currentBal = await resCurrent.text();
        let curEl = document.getElementById('current-balance');
        if(curEl) curEl.innerText = "R " + parseFloat(currentBal || 0).toFixed(2);

        let resSavings = await fetch(`/api/transactions/balance/savings?username=${currentUser}`);
        let savingsBal = await resSavings.text();
        let savEl = document.getElementById('savings-balance');
        if(savEl) savEl.innerText = "R " + parseFloat(savingsBal || 0).toFixed(2);
    } catch (err) {
        console.error("Error fetching balances:", err);
    }
}

async function performCurrentAction(type) {
    let amountEl = document.getElementById('current-amount');
    let msgBox = document.getElementById('current-msg');
    let amount = amountEl ? amountEl.value : "";

    if (!amount) { if(msgBox) msgBox.innerText = "Enter amount"; return; }

    let formData = new URLSearchParams();
    formData.append("username", currentUser);
    formData.append("amount", amount);

    let endpoint = type === 'income' ? '/api/transactions/current/income' : '/api/transactions/current/expense';
    let response = await fetch(endpoint, { method: 'POST', body: formData });
    let result = await response.text();

    if (result.startsWith("ERROR:")) {
        if(msgBox) { msgBox.className = "error-msg"; msgBox.innerText = result.replace("ERROR:", ""); }
    } else {
        if(msgBox) msgBox.innerText = "";
        if(amountEl) amountEl.value = "";
        fetchBalances();
    }
}

async function performSavingsAction(type) {
    let amountEl = document.getElementById('savings-amount');
    let msgBox = document.getElementById('savings-msg');
    let amount = amountEl ? amountEl.value : "";

    if (!amount) { if(msgBox) msgBox.innerText = "Enter amount"; return; }

    let formData = new URLSearchParams();
    formData.append("username", currentUser);
    formData.append("amount", amount);

    let endpoint = type === 'income' ? '/api/transactions/savings/income' : '/api/transactions/savings/expense';
    let response = await fetch(endpoint, { method: 'POST', body: formData });
    let result = await response.text();

    if (result.startsWith("ERROR:")) {
        if(msgBox) { msgBox.className = "error-msg"; msgBox.innerText = result.replace("ERROR:", ""); }
    } else {
        if(msgBox) msgBox.innerText = "";
        if(amountEl) amountEl.value = "";
        fetchBalances();
    }
}

async function makeInvestment() {
    let amountEl = document.getElementById('invest-amount');
    let msgBox = document.getElementById('invest-msg');
    let amount = amountEl ? amountEl.value : "";
    if (!amount) { if(msgBox) msgBox.innerText = "Enter amount"; return; }

    let formData = new URLSearchParams();
    formData.append("username", currentUser);
    formData.append("amount", amount);

    let response = await fetch(`/api/transactions/invest`, { method: 'POST', body: formData });
    let result = await response.text();

    if (result.startsWith("ERROR:")) {
        if(msgBox) { msgBox.className = "error-msg"; msgBox.innerText = result.replace("ERROR:", ""); }
    } else {
        if(msgBox) { msgBox.className = "success-msg"; msgBox.innerText = "Investment locked successfully!"; }
        if(amountEl) amountEl.value = "";
        fetchBalances();
        fetchInvestments();
    }
}

async function fetchInvestments() {
    try {
        let response = await fetch(`/api/transactions/investments?username=${currentUser}`);
        let investments = await response.json();

        let container = document.getElementById('investment-list');
        if(!container) return;
        container.innerHTML = "";

        if (!investments || investments.length === 0) {
            container.innerHTML = "<p style='font-size:12px; color:#777;'>No active portfolios found.</p>";
            return;
        }

        investments.forEach(inv => {
            let status = inv.activeInvestment ? "🟢 Active (Growth in progress)" : "✅ Matured & Paid Out";
            let item = `<div style="background:#f1f3f5; padding:8px; border-radius:6px; margin-bottom:6px; font-size:12px;">
                <strong>R ${parseFloat(inv.amount || 0).toFixed(2)}</strong><br>
                Maturity: ${inv.maturityDate || 'N/A'}<br>
                <span>${status}</span>
            </div>`;
            container.innerHTML += item;
        });
    } catch (err) {
        console.error("Error fetching investments:", err);
    }
}

async function fetchHistory() {
    try {
        let response = await fetch(`/api/transactions/history?username=${currentUser}`);
        let history = await response.json();

        let tbody = document.getElementById('history-table-body');
        if(!tbody) return;
        tbody.innerHTML = "";

        if (!history) return;

        history.forEach(tx => {
            let row = `<tr>
                <td>${tx.date || ''}</td>
                <td><b>${tx.type || ''}</b></td>
                <td>R ${parseFloat(tx.amount || 0).toFixed(2)}</td>
            </tr>`;
            tbody.innerHTML += row;
        });
    } catch (err) {
        console.error("Error fetching history:", err);
    }
}