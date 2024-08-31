let sessionToken = null;

function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    fetch(`http://localhost:5000/login?username=${username}&password=${password}`)
        .then(response => response.text())
        .then(data => {
            if (data.startsWith("Login successful")) {
                sessionToken = data.split("token: ")[1];
                document.getElementById('loginResult').innerText = "Logged in successfully";
                showAuthenticatedSections();
            } else {
                document.getElementById('loginResult').innerText = data;
            }
        })
        .catch(error => console.error('Error:', error));
}

function showAuthenticatedSections() {
    document.getElementById('balance-section').style.display = 'block';
    document.getElementById('deposit-section').style.display = 'block';
    document.getElementById('withdraw-section').style.display = 'block';
}

function checkBalance() {
    fetch(`http://localhost:5000/checkBalance`, {
        headers: {
            'Authorization': sessionToken
        }
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById('balanceResult').innerText = data;
    })
    .catch(error => console.error('Error:', error));
}

function deposit() {
    const amount = document.getElementById('depositAmount').value;
    fetch(`http://localhost:5000/deposit?amount=${amount}`, {
        headers: {
            'Authorization': sessionToken
        }
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById('depositResult').innerText = data;
    })
    .catch(error => console.error('Error:', error));
}

function withdraw() {
    const amount = document.getElementById('withdrawAmount').value;
    fetch(`http://localhost:5000/withdraw?amount=${amount}`, {
        headers: {
            'Authorization': sessionToken
        }
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById('withdrawResult').innerText = data;
    })
    .catch(error => console.error('Error:', error));
}
