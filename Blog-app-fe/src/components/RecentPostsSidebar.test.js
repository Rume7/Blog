import '@testing-library/jest-dom';
import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RecentPostsSidebar from '../../src/components/RecentPostsSidebar'; // Adjust path
import { mockPosts } from '../../src/mockData'; // Adjust path

// Mock the onNavigate function passed as a prop
const mockOnNavigate = jest.fn();

describe('RecentPostsSidebar Component', () => {
  // Clear mocks before each test
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Test Case 1: Renders only published posts
  test('renders only published posts', () => {
    render(<RecentPostsSidebar posts={mockPosts} onNavigate={mockOnNavigate} />);

    // Assert that published post titles are in the document
    expect(screen.getByText('The Future of AI: A Deeper Dive')).toBeInTheDocument();
    expect(screen.getByText('Mastering React Hooks: A Guide')).toBeInTheDocument();
    expect(screen.getByText('My Journey into Web3 Development')).toBeInTheDocument();

    // Assert that draft post title is NOT in the document
    expect(screen.queryByText('Draft Post Example (Admin Only)')).not.toBeInTheDocument();
  });

  // Test Case 2: Renders a limited number of recent posts (e.g., 3 if more are available)
  // This test assumes mockPosts has more than 7 published posts to verify the slice(-7)
  // For the current mockData, it will just show all 3 published posts.
  test('renders a limited number of recent posts (max 7)', () => {
    // Create more mock posts to exceed the limit
    const extendedMockPosts = [
      { id: '7', title: 'Old Post 1 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 1, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '8', title: 'Old Post 2 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 2, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '9', title: 'Old Post 3 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 3, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '10', title: 'Old Post 4 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 4, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '11', title: 'Old Post 5 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 5, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '12', title: 'Old Post 6 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 6, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '13', title: 'Old Post 7 (Should not be shown)', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 7, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      ...mockPosts,
      { id: '14', title: 'Recent Post 1', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 8, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '15', title: 'Recent Post 2', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 9, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '16', title: 'Recent Post 3', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 10, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '17', title: 'Recent Post 4', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 11, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '18', title: 'Recent Post 5', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 12, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '19', title: 'Recent Post 6', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 13, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
      { id: '20', title: 'Recent Post 7', content: '...', author: 'Test', authorId: 'test1', publishedAt: 'Aug 14, 2025', claps: 1, comments: 1, status: 'PUBLISHED', imageUrl: '' },
    ];
    render(<RecentPostsSidebar posts={extendedMockPosts} onNavigate={mockOnNavigate} />);

    // Query all list items or elements that represent a post title
    const postTitles = screen.getAllByRole('button'); // Assuming each post title is a button
    expect(postTitles).toHaveLength(7); // Expecting exactly 7 recent posts

    // Check if the latest post (Recent Post 7) is visible
    expect(screen.getByText('Recent Post 7')).toBeInTheDocument();
    // Check if the old posts that should be excluded are NOT visible
    expect(screen.queryByText('Old Post 1 (Should not be shown)')).not.toBeInTheDocument();
  });


  // Test Case 3: Displays a message when no recent posts are available
  test('displays a message when no recent posts are available', () => {
    render(<RecentPostsSidebar posts={[]} onNavigate={mockOnNavigate} />); // Pass an empty array
    expect(screen.getByText('No recent posts available.')).toBeInTheDocument();
  });

  // Test Case 4: Calls onNavigate with correct arguments when a post title is clicked
  test('calls onNavigate with correct arguments when a post title is clicked', async () => {
    const publishedPost = mockPosts.find(p => p.id === '1');
    render(<RecentPostsSidebar posts={mockPosts} onNavigate={mockOnNavigate} />);

    const postTitleButton = screen.getByText(publishedPost.title);
    await userEvent.click(postTitleButton);

    expect(mockOnNavigate).toHaveBeenCalledTimes(1);
    expect(mockOnNavigate).toHaveBeenCalledWith('postDetail', publishedPost.id);
  });
});
