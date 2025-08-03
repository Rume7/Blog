import React, { createContext, useContext, useState, useEffect } from 'react';
import apiService from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [magicLinkSent, setMagicLinkSent] = useState(false);

  const isAuthenticated = !!user;

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('authToken');
      if (token) {
        try {
          const userData = await apiService.getCurrentUser();
          setUser(userData);
        } catch (error) {
          console.error('Failed to get current user:', error);
          localStorage.removeItem('authToken');
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  const login = async (email) => {
    try {
      setLoading(true);
      const result = await apiService.login(email);
      setMagicLinkSent(true);
      return result;
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const verifyMagicLink = async (token) => {
    try {
      setLoading(true);
      const jwtToken = await apiService.verifyMagicLink(token);
      localStorage.setItem('authToken', jwtToken);
      
      // Get user data
      const userData = await apiService.getCurrentUser();
      setUser(userData);
      setMagicLinkSent(false);
      
      return { success: true };
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    try {
      setLoading(true);
      const result = await apiService.register(userData);
      return result;
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (userData) => {
    try {
      setLoading(true);
      const updatedUser = await apiService.updateProfile(userData);
      setUser(updatedUser);
      return updatedUser;
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    apiService.logout();
    setUser(null);
    setMagicLinkSent(false);
  };

  const value = {
    user,
    loading,
    isAuthenticated,
    magicLinkSent,
    login,
    verifyMagicLink,
    register,
    updateProfile,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
