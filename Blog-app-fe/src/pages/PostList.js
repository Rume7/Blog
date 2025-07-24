import React, { useContext } from 'react'; // Removed useState as searchTerm is now from props
import { AuthContext } from '../context/AuthContext';
import PostCard from '../components/PostCard';
import NewsletterSignup from '../components/NewsletterSignup';

// Removed Search import as it's no longer needed here
import { mockPosts } from '../mockData'; // Import mock data

// Post List Component (Home Page)
const PostList = ({ onNavigate, searchTerm }) => { // searchTerm passed as prop
  const { user } = useContext(AuthContext);
  // Removed local searchTerm state

  const filteredPosts = mockPosts.filter(post => {
    const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          post.content.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          post.author.toLowerCase().includes(searchTerm.toLowerCase());
    const isVisible = post.status === 'PUBLISHED' || (user && (user.role === 'ADMIN' || user.id === post.authorId));
    return matchesSearch && isVisible;
  });

  return (
    <main>
      {/* Landing Banner */}
      <section className="bg-green-600 text-white py-16 md:py-24 px-6 md:px-12">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between">
          <div className="md:w-1/2 mb-8 md:mb-0">
            <h2 className="text-5xl md:text-6xl font-bold font-serif leading-tight mb-4">
              Stay curious.
            </h2>
            <p className="text-xl md:text-2xl mb-6">
              Discover stories, thinking, and expertise from writers on any topic.
            </p>
            <button
              onClick={() => onNavigate('register')}
              className="bg-white text-green-600 px-6 py-3 rounded-full text-lg font-semibold hover:bg-gray-100 transition duration-200 shadow-lg"
            >
              Start reading
            </button>
          </div>
          <div className="md:w-1/2 flex justify-center">
            {/* Placeholder for an illustration or graphic */}
            <img
              src="https://placehold.co/300x300/60A5FA/FFFFFF?text=Blog+Illustration"
              alt="Blog Illustration"
              className="w-64 h-64 md:w-80 md:h-80 rounded-full object-cover shadow-xl"
            />
          </div>
        </div>
      </section>

      <div className="max-w-7xl mx-auto px-6 md:px-12 py-8">
        {/* Newsletter Signup Section */}
        <NewsletterSignup />

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {filteredPosts.length > 0 ? (
            filteredPosts.map(post => (
              <PostCard key={post.id} post={post} onNavigate={onNavigate} />
            ))
          ) : (
            <p className="col-span-full text-center text-gray-600 text-lg">No posts found.</p>
          )}
        </div>
      </div>
    </main>
  );
};

export default PostList;
