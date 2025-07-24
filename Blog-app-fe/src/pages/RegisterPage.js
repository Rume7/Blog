import React, { useState } from 'react';

// Register Page
const RegisterPage = ({ onNavigate }) => {
  const [username, setUsername] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('Registering account...');

    // In a real app, this would be the payload sent to your backend
    const registrationData = {
      username,
      firstName,
      lastName,
      email,
    };
    console.log('Attempting to register with:', registrationData);

    // In a real app, make an API call to your backend to register the user
    // await fetch('/api/auth/register', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(registrationData)
    // });
    // Simulate success
    setTimeout(() => {
      setMessage('Registration successful! You can now log in.');
      setUsername('');
      setFirstName('');
      setLastName('');
      setEmail(''); // Clear email field
      // Optionally navigate to login page after successful registration
      // onNavigate('login');
    }, 2000);
  };

  const inputClassName = "appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-green-500 focus:border-green-500 focus:z-10 sm:text-sm";

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-lg">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900 font-serif">
            Register for MyBlog
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Create your account to start reading and writing.
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px space-y-4"> {/* Added space-y-4 for spacing between new fields */}
            <div>
              <label htmlFor="username" className="sr-only">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                required
                className={inputClassName}
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="first-name" className="sr-only">
                First Name
              </label>
              <input
                id="first-name"
                name="firstName"
                type="text"
                autoComplete="given-name"
                required
                className={inputClassName}
                placeholder="First Name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="last-name" className="sr-only">
                Last Name
              </label>
              <input
                id="last-name"
                name="lastName"
                type="text"
                autoComplete="family-name"
                required
                className={inputClassName}
                placeholder="Last Name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="register-email" className="sr-only">
                Email address
              </label>
              <input
                id="register-email"
                name="email"
                type="email"
                autoComplete="email"
                required
                className={inputClassName}
                placeholder="Email address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
            >
              Register
            </button>
          </div>
          {message && (
            <p className="mt-2 text-center text-sm text-gray-600">{message}</p>
          )}
        </form>
        <div className="text-center text-sm">
          <p className="text-gray-600">Already have an account? <button onClick={() => onNavigate('login')} className="font-medium text-green-600 hover:text-green-500">Sign in</button></p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
