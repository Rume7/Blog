import React, { useState } from 'react';

// Import Context
import { AuthProvider } from './context/AuthContext';

// Import Components
import Header from './components/Header';
import Footer from './components/Footer';

// Import Pages
import PostList from './pages/PostList';
import PostDetail from './pages/PostDetail';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import PostEditor from './pages/PostEditor';


// Main App Component
const App = () => {
  const [currentPage, setCurrentPage] = useState('home');
  const [currentPostId, setCurrentPostId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const handleNavigate = (page, postId = null) => {
    setCurrentPage(page);
    setCurrentPostId(postId);
    // When navigating away from home/search, clear the search term
    if (page !== 'home' && page !== 'search') {
      setSearchTerm('');
    }
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
      case 'search':
        return <PostList onNavigate={handleNavigate} searchTerm={searchTerm} />;
      case 'postDetail':
        return <PostDetail postId={currentPostId} onNavigate={handleNavigate} />;
      case 'login':
        return <LoginPage onNavigate={handleNavigate} />;
      case 'register':
        return <RegisterPage onNavigate={handleNavigate} />;
      case 'profile':
        return <ProfilePage onNavigate={handleNavigate} />;
      case 'createPost':
        return <PostEditor onNavigate={handleNavigate} />;
      case 'editPost':
        return <PostEditor onNavigate={handleNavigate} postId={currentPostId} />;
      default:
        return <PostList onNavigate={handleNavigate} searchTerm={searchTerm} />;
    }
  };

  return (
    <AuthProvider>
      <div className="min-h-screen flex flex-col font-inter">
        <Header onNavigate={handleNavigate} searchTerm={searchTerm} onSearchChange={setSearchTerm} />
        <div className="flex-grow">
          {renderPage()}
        </div>
        <Footer />
      </div>
    </AuthProvider>
  );
};

export default App;
