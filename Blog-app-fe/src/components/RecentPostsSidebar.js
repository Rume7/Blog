import React from 'react';

// Recent Posts Sidebar Component
const RecentPostsSidebar = ({ posts, onNavigate }) => {
  // Filter for published posts and sort by published date (descending)
  // For mock data, we'll just take the last 7 from the array as a proxy for "recent"
  const recentPublishedPosts = posts
    .filter(post => post.status === 'PUBLISHED')
    .slice(-7) // Take the last 7 posts from the mock array
    .reverse(); // Reverse to show most recent first if array order implies recency

  return (
    <div className="hidden lg:block w-full lg:w-1/4 pl-8 border-l border-gray-200">
      <h3 className="text-xl font-bold font-serif text-gray-900 mb-4">Recent Posts</h3>
      <ul className="space-y-3">
        {recentPublishedPosts.length > 0 ? (
          recentPublishedPosts.map(post => (
            <li key={post.id}>
              <button
                onClick={() => onNavigate('postDetail', post.id)}
                className="text-gray-700 hover:text-green-600 hover:underline text-left text-sm leading-tight"
              >
                {post.title}
              </button>
            </li>
          ))
        ) : (
          <p className="text-gray-500 text-sm">No recent posts available.</p>
        )}
      </ul>
    </div>
  );
};

export default RecentPostsSidebar;
