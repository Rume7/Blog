import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Import Context
import { AuthProvider, useAuth } from './context/AuthContext';

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

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }
  
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

// Main App Layout
const AppLayout = () => {
  return (
    <div className="min-h-screen flex flex-col font-inter">
      <Header />
      <div className="flex-grow">
        <Routes>
          <Route path="/" element={<PostList />} />
          <Route path="/posts/:id" element={<PostDetail />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route 
            path="/profile" 
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/posts/create" 
            element={
              <ProtectedRoute>
                <PostEditor />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/posts/:id/edit" 
            element={
              <ProtectedRoute>
                <PostEditor />
              </ProtectedRoute>
            } 
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
      <Footer />
    </div>
  );
};

// Main App Component
const App = () => {
  return (
    <Router>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </Router>
  );
};

export default App;
