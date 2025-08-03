import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Search, PenLine, LogIn, User, X } from 'lucide-react';

// Header Component
const Header = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const [showSearchBar, setShowSearchBar] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  const handleSearchIconClick = () => {
    setShowSearchBar(!showSearchBar);
    if (showSearchBar) {
      setSearchTerm('');
    }
  };

  const handleSearch = (e) => {
    const value = e.target.value;
    setSearchTerm(value);
    // Navigate to home with search term if not already there
    if (location.pathname !== '/') {
      navigate(`/?search=${encodeURIComponent(value)}`);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="bg-white border-b border-gray-200 py-4 px-6 md:px-12 shadow-sm sticky top-0 z-10">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <Link to="/" className="text-3xl font-bold font-serif text-gray-800 hover:text-gray-600">
          MyBlog
        </Link>
        <nav className="flex items-center space-x-4 sm:space-x-6">
          {showSearchBar && (
            <div className="relative flex items-center flex-grow mr-4">
              <input
                type="text"
                placeholder="Search posts..."
                className="w-full p-2 pl-8 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-green-500 text-sm"
                value={searchTerm}
                onChange={handleSearch}
              />
              <Search className="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
              <button
                onClick={handleSearchIconClick}
                className="ml-2 text-gray-600 hover:text-gray-900 p-1 rounded-full hover:bg-gray-100 flex-shrink-0"
                aria-label="Close search"
              >
                <X size={20} />
              </button>
            </div>
          )}

          {!showSearchBar && (
            <button
              onClick={handleSearchIconClick}
              className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0"
              aria-label="Open search"
            >
              <Search size={20} />
              <span className="hidden sm:inline">Search</span>
            </button>
          )}

          {isAuthenticated && user?.role === 'ADMIN' && (
            <Link
              to="/posts/create"
              className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0"
            >
              <PenLine size={20} />
              <span className="hidden sm:inline">Write</span>
            </Link>
          )}
          
          {isAuthenticated ? (
            <>
              <Link
                to="/profile"
                className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0"
              >
                <User size={20} />
                <span className="hidden sm:inline">Profile</span>
              </Link>
              <button
                onClick={handleLogout}
                className="bg-red-500 text-white px-4 py-2 rounded-full text-sm font-semibold hover:bg-red-600 transition duration-200 flex-shrink-0"
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="bg-green-600 text-white px-4 py-2 rounded-full text-sm font-semibold hover:bg-green-700 transition duration-200 flex items-center space-x-1 flex-shrink-0"
            >
              <LogIn size={18} />
              <span>Sign In</span>
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
