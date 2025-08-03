import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Header from './Header';
import { AuthProvider } from '../context/AuthContext';

// Mock the useAuth hook
jest.mock('../context/AuthContext', () => ({
  ...jest.requireActual('../context/AuthContext'),
  useAuth: jest.fn(),
}));

import { useAuth } from '../context/AuthContext';

// Define mock user objects for different roles
const mockAdminUser = { id: 'admin1', email: 'admin@example.com', role: 'ADMIN' };
const mockRegularUser = { id: 'user1', email: 'user@example.com', role: 'USER' };

// Helper function to render Header with mocked auth
const renderHeaderWithAuth = (authValue) => {
  useAuth.mockReturnValue(authValue);
  
  return render(
    <BrowserRouter>
      <Header />
    </BrowserRouter>
  );
};

describe('Header Component', () => {
  // Clear mocks before each test
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Test Case 1: Renders correctly for a logged-out user
  test('renders correctly for a logged-out user', () => {
    renderHeaderWithAuth({
      user: null,
      isAuthenticated: false,
      logout: jest.fn(),
    });

    // Assert that main elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Sign In/i })).toBeInTheDocument();

    // Assert that elements for logged-in users or admin are NOT present
    expect(screen.queryByRole('link', { name: /Write/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Profile/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Logout/i })).not.toBeInTheDocument();
  });

  // Test Case 2: Renders correctly for a regular logged-in user
  test('renders correctly for a regular logged-in user', () => {
    renderHeaderWithAuth({
      user: mockRegularUser,
      isAuthenticated: true,
      logout: jest.fn(),
    });

    // Assert that main elements and user-specific elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Profile/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Logout/i })).toBeInTheDocument();

    // Assert that admin-specific elements are NOT present
    expect(screen.queryByRole('link', { name: /Write/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Sign In/i })).not.toBeInTheDocument();
  });

  // Test Case 3: Renders correctly for an ADMIN user
  test('renders correctly for an ADMIN user', () => {
    renderHeaderWithAuth({
      user: mockAdminUser,
      isAuthenticated: true,
      logout: jest.fn(),
    });

    // Assert that admin-specific elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Write/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Profile/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Logout/i })).toBeInTheDocument();

    // Assert that Sign In button is NOT present
    expect(screen.queryByRole('link', { name: /Sign In/i })).not.toBeInTheDocument();
  });

  // Test Case 4: Search bar toggles visibility and handles input
  test('search bar toggles visibility and handles input', async () => {
    renderHeaderWithAuth({
      user: null,
      isAuthenticated: false,
      logout: jest.fn(),
    });

    // Initial state: search input is not visible
    expect(screen.queryByPlaceholderText(/Search posts.../i)).not.toBeInTheDocument();

    // Click search icon to show search bar
    const searchIcon = screen.getByRole('button', { name: /Open search/i });
    await userEvent.click(searchIcon);

    // Search input should now be visible
    const searchInput = screen.getByPlaceholderText(/Search posts.../i);
    expect(searchInput).toBeInTheDocument();

    // Type in the search input
    fireEvent.change(searchInput, { target: { value: 'test query' } });

    // Assert that the input field now contains the typed text
    expect(searchInput).toHaveValue('test query');

    // Click close icon to hide search bar
    const closeSearchIcon = screen.getByRole('button', { name: /Close search/i });
    await userEvent.click(closeSearchIcon);

    // Search input should be hidden again
    expect(screen.queryByPlaceholderText(/Search posts.../i)).not.toBeInTheDocument();
  });

  // Test Case 5: Logout button calls logout function
  test('logout button calls logout function', async () => {
    const mockLogout = jest.fn();
    renderHeaderWithAuth({
      user: mockRegularUser,
      isAuthenticated: true,
      logout: mockLogout,
    });

    const logoutButton = screen.getByRole('button', { name: /Logout/i });
    await userEvent.click(logoutButton);

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });
});
