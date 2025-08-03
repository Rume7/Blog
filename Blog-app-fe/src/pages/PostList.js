import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import PostCard from '../components/PostCard';
import NewsletterSignup from '../components/NewsletterSignup';
import apiService from '../services/api';

// Post List Component (Home Page)
const PostList = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const searchTerm = searchParams.get('search') || '';

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        setLoading(true);
        const response = await apiService.getPosts(0, 50, searchTerm);
        setPosts(response.content || response);
      } catch (error) {
        console.error('Failed to fetch posts:', error);
        setError('Failed to load posts');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [searchTerm]);

  const handleStartReading = () => {
    if (isAuthenticated) {
      navigate('/');
    } else {
      navigate('/register');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading posts...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg text-red-600">{error}</div>
      </div>
    );
  }

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
              onClick={handleStartReading}
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
          {posts.length > 0 ? (
            posts.map(post => (
              <PostCard key={post.id} post={post} />
            ))
          ) : (
            <p className="col-span-full text-center text-gray-600 text-lg">
              {searchTerm ? 'No posts found matching your search.' : 'No posts available.'}
            </p>
          )}
        </div>
      </div>
    </main>
  );
};

export default PostList;
