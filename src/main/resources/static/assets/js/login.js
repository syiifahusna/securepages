document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();
    let isValid = validateForm();
    if (isValid) {
        this.submit();
    }
});

function validateForm() {
    let isValid = true;

    // Username validation
    const username = document.getElementById('username').value;
    if (username.trim() === "") {
        isValid = false;
        document.getElementById('usernameError').classList.remove('hidden');
    } else {
        document.getElementById('usernameError').classList.add('hidden');
    }

    // Password validation
    const password = document.getElementById('password').value;
    if (password.length < 6) {
        isValid = false;
        document.getElementById('passwordError').classList.remove('hidden');
    } else {
        document.getElementById('passwordError').classList.add('hidden');
    }

    return isValid;
}