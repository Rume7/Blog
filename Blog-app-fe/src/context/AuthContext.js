import React, { createContext, useState, useEffect } from 'react';
import apiService from '../services/api';

// --- Context for User and Authentication ---
export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const login = async (email, password) => {
    try {
      const response = await apiService.login(email, password);
      setUser(response.user);
      return { success: true, user: response.user };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: error.message };
    }
  };

  const register = async (userData) => {
    try {
      await apiService.register(userData);
      // After successful registration, log the user in
      const loginResponse = await apiService.login(userData.email, userData.password);
      setUser(loginResponse.user);
      return { success: true, user: loginResponse.user };
    } catch (error) {
      console.error('Registration error:', error);
      return { success: false, error: error.message };
    }
  };

  const logout = () => {
    setUser(null);
    apiService.logout();
    console.log('User logged out');
  };

  const updateProfile = async (userData) => {
    try {
      const response = await apiService.updateProfile(userData);
      setUser(response);
      return { success: true, user: response };
    } catch (error) {
      console.error('Profile update error:', error);
      return { success: false, error: error.message };
    }
  };

  // Check if user is authenticated on app load
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('authToken');
      if (token) {
        try {
          const userData = await apiService.getCurrentUser();
          setUser(userData);
        } catch (error) {
          console.error('Auth check error:', error);
          // Token is invalid, remove it
          apiService.logout();
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ 
      user, 
      login, 
      logout, 
      register, 
      updateProfile, 
      loading,
      isAuthenticated: !!user 
    }}>
      {children}
    </AuthContext.Provider>
  );
};
