import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

// Login Page
const LoginPage = ({ onNavigate }) => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const { login } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('Sending magic link to your email...');
    // In a real app, make an API call to your backend:
    // await fetch('/api/auth/login', { method: 'POST', body: JSON.stringify({ email }) });
    // Simulate success
    setTimeout(() => {
      setMessage('Magic link sent! Check your inbox to complete login.');
      // For demonstration, let's auto-login a mock admin or user after a delay
      if (email === 'admin@example.com') {
        login({ id: 'admin1', email: 'admin@example.com', role: 'ADMIN' });
        onNavigate('home');
      } else if (email === 'user@example.com') {
        login({ id: 'user1', email: 'user@example.com', role: 'USER' });
        onNavigate('home');
      } else {
        // If not a mock user, just show the message and stay on login page
        // In a real app, the user would click the link in their email
      }
    }, 2000);
  };

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-lg">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900 font-serif">
            Sign in to MyBlog
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Enter your email to receive a magic link.
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="email-address" className="sr-only">
                Email address
              </label>
              <input
                id="email-address"
                name="email"
                type="email"
                autoComplete="email"
                required
                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-green-500 focus:border-green-500 focus:z-10 sm:text-sm"
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
              Send Magic Link
            </button>
          </div>
          {message && (
            <p className="mt-2 text-center text-sm text-gray-600">{message}</p>
          )}
        </form>
        <div className="text-center text-sm">
          <p className="text-gray-600">Don't have an account? <button onClick={() => onNavigate('register')} className="font-medium text-green-600 hover:text-green-500">Register</button></p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
