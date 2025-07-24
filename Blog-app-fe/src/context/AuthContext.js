import React, { createContext, useState, useEffect } from 'react';

// --- Context for User and Authentication ---
export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // In a real application, this state would come from your backend after successful login
  // and would likely be stored in localStorage or sessionStorage.
  const [user, setUser] = useState(null); // { id: '123', email: 'admin@example.com', role: 'ADMIN' } or { id: '456', email: 'user@example.com', role: 'USER' }

  const login = (userData) => {
    setUser(userData);
    // In a real app, you'd store a token here (e.g., localStorage.setItem('token', userData.token))
    console.log('User logged in:', userData);
  };

  const logout = () => {
    setUser(null);
    // In a real app, you'd clear the token here (e.g., localStorage.removeItem('token'))
    console.log('User logged out');
  };

  // Simulate initial auth check (e.g., checking for a token in localStorage)
  useEffect(() => {
    // This is where you'd typically check for an existing token and validate it with your backend
    // For now, let's simulate a logged-out state initially.
    // setUser({ id: 'admin1', email: 'admin@example.com', role: 'ADMIN' }); // Uncomment to test as admin
    // setUser({ id: 'user1', email: 'user@example.com', role: 'USER' });   // Uncomment to test as regular user
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
