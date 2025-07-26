import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react'; // Ensure fireEvent is imported
import userEvent from '@testing-library/user-event';
import Header from '../../src/components/Header'; // Adjust path based on new test folder structure
import { AuthContext } from '../../src/context/AuthContext'; // Adjust path

// Mock the onNavigate and onSearchChange functions passed as props
const mockOnNavigate = jest.fn();
const mockOnSearchChange = jest.fn(); // Keep this mock to ensure it's called at all

// Define mock user objects for different roles
const mockAdminUser = { id: 'admin1', email: 'admin@example.com', role: 'ADMIN' };
const mockRegularUser = { id: 'user1', email: 'user@example.com', role: 'USER' };

describe('Header Component', () => {

  // Clear mocks before each test to prevent interference
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Test Case 1: Renders correctly for a logged-out user
  test('renders correctly for a logged-out user', () => {
    render(
      <AuthContext.Provider value={{ user: null, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm="" onSearchChange={mockOnSearchChange} />
      </AuthContext.Provider>
    );

    // Assert that main elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Sign In/i })).toBeInTheDocument();

    // Assert that elements for logged-in users or admin are NOT present
    expect(screen.queryByRole('button', { name: /Write/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Profile/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Logout/i })).not.toBeInTheDocument();
  });

  // Test Case 2: Renders correctly for a regular logged-in user
  test('renders correctly for a regular logged-in user', () => {
    render(
      <AuthContext.Provider value={{ user: mockRegularUser, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm="" onSearchChange={mockOnSearchChange} />
      </AuthContext.Provider>
    );

    // Assert that main elements and user-specific elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Profile/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Logout/i })).toBeInTheDocument();

    // Assert that admin-specific elements are NOT present
    expect(screen.queryByRole('button', { name: /Write/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Sign In/i })).not.toBeInTheDocument();
  });

  // Test Case 3: Renders correctly for an ADMIN user
  test('renders correctly for an ADMIN user', () => {
    render(
      <AuthContext.Provider value={{ user: mockAdminUser, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm="" onSearchChange={mockOnSearchChange} />
      </AuthContext.Provider>
    );

    // Assert that admin-specific elements are present
    expect(screen.getByText('MyBlog')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Write/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Profile/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Logout/i })).toBeInTheDocument();

    // Assert that Sign In button is NOT present
    expect(screen.queryByRole('button', { name: /Sign In/i })).not.toBeInTheDocument();
  });

  // Test Case 4: Navigates to home when 'MyBlog' title is clicked
  test('navigates to home when MyBlog title is clicked', async () => {
    render(
      <AuthContext.Provider value={{ user: null, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm="" onSearchChange={mockOnSearchChange} />
      </AuthContext.Provider>
    );

    const myBlogTitle = screen.getByText('MyBlog');
    await userEvent.click(myBlogTitle);

    expect(mockOnNavigate).toHaveBeenCalledTimes(1);
    expect(mockOnNavigate).toHaveBeenCalledWith('home');
    expect(mockOnSearchChange).toHaveBeenCalledWith(''); // Ensure search term is cleared
  });

  // Test Case 5: Search bar toggles visibility and handles input
  test('search bar toggles visibility and handles input', async () => {
    // We need a way to track the searchTerm state that Header receives.
    // For this test, we'll mock the onSearchChange to update a local variable
    // and pass that variable to searchTerm.
    let currentSearchTerm = '';
    const mockOnSearchChangeLocal = jest.fn((value) => {
      currentSearchTerm = value;
    });

    const { rerender } = render(
      <AuthContext.Provider value={{ user: null, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm={currentSearchTerm} onSearchChange={mockOnSearchChangeLocal} />
      </AuthContext.Provider>
    );

    // Initial state: search input is not visible
    expect(screen.queryByPlaceholderText(/Search posts.../i)).not.toBeInTheDocument();

    // Click search icon to show search bar
    const searchIcon = screen.getByRole('button', { name: /Open search/i });
    await userEvent.click(searchIcon);

    // Search input should now be visible
    const searchInput = screen.getByPlaceholderText(/Search posts.../i);
    expect(searchInput).toBeInTheDocument();

    // --- CRITICAL CHANGE HERE: Use fireEvent.change instead of userEvent.type ---
    // fireEvent.change directly sets the input's value, which is more reliable for controlled components.
    fireEvent.change(searchInput, { target: { value: 'test query' } });

    // Rerender the component with the updated searchTerm to reflect the input change
    rerender(
      <AuthContext.Provider value={{ user: null, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm={currentSearchTerm} onSearchChange={mockOnSearchChangeLocal} />
      </AuthContext.Provider>
    );

    // Assert that the input field now contains the full typed text
    expect(searchInput).toHaveValue('test query');
    // Optionally, you can still check that onSearchChange was called with the final value
    expect(mockOnSearchChangeLocal).toHaveBeenCalledWith('test query');


    // Click close icon to hide search bar
    const closeSearchIcon = screen.getByRole('button', { name: /Close search/i });
    await userEvent.click(closeSearchIcon);

    // Rerender to reflect the clearing of the search term
    rerender(
      <AuthContext.Provider value={{ user: null, logout: jest.fn() }}>
        <Header onNavigate={mockOnNavigate} searchTerm={currentSearchTerm} onSearchChange={mockOnSearchChangeLocal} />
      </AuthContext.Provider>
    );

    // Search input should be hidden again
    expect(screen.queryByPlaceholderText(/Search posts.../i)).not.toBeInTheDocument();
    // Assert that the onSearchChange was called to clear the term (it should be the last call)
    expect(mockOnSearchChangeLocal).toHaveBeenCalledWith('');
  });


  // Test Case 6: Logout button calls logout function
  test('logout button calls logout function', async () => {
    const mockLogout = jest.fn();
    render(
      <AuthContext.Provider value={{ user: mockRegularUser, logout: mockLogout }}>
        <Header onNavigate={mockOnNavigate} searchTerm="" onSearchChange={mockOnSearchChange} />
      </AuthContext.Provider>
    );

    const logoutButton = screen.getByRole('button', { name: /Logout/i });
    await userEvent.click(logoutButton);

    expect(mockLogout).toHaveBeenCalledTimes(1);
    expect(mockOnNavigate).not.toHaveBeenCalled(); // Logout doesn't navigate directly in Header
  });
});
