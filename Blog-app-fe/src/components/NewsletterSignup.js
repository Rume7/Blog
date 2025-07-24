import React, { useState } from 'react';

const NewsletterSignup = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!email) {
      setMessage('Please enter your email address.');
      return;
    }

    setMessage('Subscribing...');
    // In a real application, you would send this email to your backend
    // For example:
    // fetch('/api/subscribe', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ email })
    // })
    // .then(response => response.json())
    // .then(data => {
    //   setMessage('Thank you for subscribing!');
    //   setEmail('');
    // })
    // .catch(error => {
    //   setMessage('Subscription failed. Please try again.');
    //   console.error('Subscription error:', error);
    // });

    // Simulate API call success
    setTimeout(() => {
      setMessage('Thank you for subscribing!');
      setEmail('');
    }, 1500);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-8 mb-8 text-center">
      <h3 className="text-2xl font-bold font-serif text-gray-900 mb-4">
        Subscribe to our Newsletter
      </h3>
      <p className="text-gray-700 mb-6">
        Get the latest blog posts and updates directly in your inbox.
      </p>
      <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-4 max-w-lg mx-auto">
        <input
          type="email"
          placeholder="Your email address"
          className="flex-grow w-full sm:w-auto p-3 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-green-500"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <button
          type="submit"
          className="bg-green-600 text-white px-6 py-3 rounded-full text-lg font-semibold hover:bg-green-700 transition duration-200 shadow-md"
        >
          Subscribe
        </button>
      </form>
      {message && (
        <p className="mt-4 text-sm text-gray-600">{message}</p>
      )}
    </div>
  );
};

export default NewsletterSignup;
