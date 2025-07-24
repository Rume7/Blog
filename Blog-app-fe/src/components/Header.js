import React, { useContext, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Search, PenLine, LogIn, User, X } from 'lucide-react'; // Import X for close icon

// Header Component
const Header = ({ onNavigate, searchTerm, onSearchChange }) => {
  const { user, logout } = useContext(AuthContext);
  const [showSearchBar, setShowSearchBar] = useState(false);

  const handleSearchIconClick = () => {
    setShowSearchBar(!showSearchBar);
    if (showSearchBar) { // If closing the search bar, clear the search term
      onSearchChange('');
    }
  };

  return (
    <header className="bg-white border-b border-gray-200 py-4 px-6 md:px-12 shadow-sm sticky top-0 z-10">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <h1 className="text-3xl font-bold font-serif text-gray-800 cursor-pointer" onClick={() => { onNavigate('home'); setShowSearchBar(false); onSearchChange(''); }}>
          MyBlog
        </h1>
        <nav className="flex items-center space-x-4 sm:space-x-6">
          {showSearchBar && (
            <div className="relative flex items-center flex-grow mr-4"> {/* flex-grow and mr-4 added */}
              <input
                type="text"
                placeholder="Search posts..."
                className="w-full p-2 pl-8 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-green-500 text-sm"
                value={searchTerm}
                onChange={(e) => onSearchChange(e.target.value)}
              />
              <Search className="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
              <button
                onClick={handleSearchIconClick}
                className="ml-2 text-gray-600 hover:text-gray-900 p-1 rounded-full hover:bg-gray-100 flex-shrink-0" // flex-shrink-0 added
                aria-label="Close search"
              >
                <X size={20} />
              </button>
            </div>
          )}

          {!showSearchBar && ( // Only show search icon if search bar is not active
            <button
              onClick={handleSearchIconClick}
              className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0" // flex-shrink-0 added
              aria-label="Open search"
            >
              <Search size={20} />
              <span className="hidden sm:inline">Search</span>
            </button>
          )}

          {user && user.role === 'ADMIN' && (
            <button
              onClick={() => onNavigate('createPost')}
              className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0" // flex-shrink-0 added
            >
              <PenLine size={20} />
              <span className="hidden sm:inline">Write</span>
            </button>
          )}
          {user ? (
            <>
              <button
                onClick={() => onNavigate('profile')}
                className="text-gray-600 hover:text-gray-900 flex items-center space-x-1 flex-shrink-0" // flex-shrink-0 added
              >
                <User size={20} />
                <span className="hidden sm:inline">Profile</span>
              </button>
              <button
                onClick={logout}
                className="bg-red-500 text-white px-4 py-2 rounded-full text-sm font-semibold hover:bg-red-600 transition duration-200 flex-shrink-0" // flex-shrink-0 added
              >
                Logout
              </button>
            </>
          ) : (
            <button
              onClick={() => onNavigate('login')}
              className="bg-green-600 text-white px-4 py-2 rounded-full text-sm font-semibold hover:bg-green-700 transition duration-200 flex items-center space-x-1 flex-shrink-0" // flex-shrink-0 added
            >
              <LogIn size={18} />
              <span>Sign In</span>
            </button>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
