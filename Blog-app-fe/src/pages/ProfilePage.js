import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

// Profile Page
const ProfilePage = ({ onNavigate }) => {
  const { user } = useContext(AuthContext);

  if (!user) {
    return (
      <div className="min-h-[calc(100vh-200px)] flex items-center justify-center text-center text-gray-700">
        <p>Please log in to view your profile.</p>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-lg">
        <h2 className="mt-6 text-center text-3xl font-bold text-gray-900 font-serif">
          My Profile
        </h2>
        <div className="text-center">
          <p className="text-lg font-semibold text-gray-800">Email: {user.email}</p>
          <p className="text-md text-gray-600">Role: {user.role}</p>
          {/* Add more profile details here */}
        </div>
        <button
          onClick={() => onNavigate('home')}
          className="mt-4 w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
        >
          Back to Home
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;
