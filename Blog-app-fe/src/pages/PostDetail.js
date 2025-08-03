import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Heart, MessageSquare, Edit, Trash2 } from 'lucide-react';
import apiService from '../services/api';

// Post Detail Component
const PostDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPost = async () => {
      try {
        setLoading(true);
        const postData = await apiService.getPost(id);
        setPost(postData);
      } catch (error) {
        console.error('Failed to fetch post:', error);
        setError('Failed to load post');
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [id]);

  const handleEdit = () => {
    navigate(`/posts/${id}/edit`);
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this post?')) {
      try {
        await apiService.deletePost(id);
        navigate('/');
      } catch (error) {
        console.error('Failed to delete post:', error);
        setError('Failed to delete post');
      }
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading post...</div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg text-red-600">{error || 'Post not found'}</div>
      </div>
    );
  }

  const isAuthor = user && user.id === post.authorId;
  const isAdmin = user && user.role === 'ADMIN';

  return (
    <main className="max-w-4xl mx-auto px-6 md:px-12 py-8">
      <article className="bg-white rounded-lg shadow-lg overflow-hidden">
        {post.imageUrl && (
          <img
            src={post.imageUrl}
            alt={post.title}
            className="w-full h-64 md:h-96 object-cover"
            onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/800x400/E0E0E0/333333?text=Image+Not+Found'; }}
          />
        )}
        
        <div className="p-8">
          <header className="mb-6">
            <h1 className="text-4xl font-bold text-gray-900 font-serif mb-4">{post.title}</h1>
            <div className="flex items-center justify-between text-gray-600">
              <div>
                <span className="font-medium">{post.author}</span>
                <span className="mx-2">•</span>
                <span>{post.publishedAt || 'Draft'}</span>
              </div>
              {(isAdmin || isAuthor) && (
                <div className="flex items-center space-x-2">
                  <button
                    onClick={handleEdit}
                    className="text-blue-600 hover:text-blue-800 flex items-center space-x-1"
                  >
                    <Edit size={16} />
                    <span>Edit</span>
                  </button>
                  <button
                    onClick={handleDelete}
                    className="text-red-600 hover:text-red-800 flex items-center space-x-1"
                  >
                    <Trash2 size={16} />
                    <span>Delete</span>
                  </button>
                </div>
              )}
            </div>
          </header>

          <div className="prose prose-lg max-w-none">
            <p className="text-gray-700 leading-relaxed">{post.content}</p>
          </div>

          <footer className="mt-8 pt-6 border-t border-gray-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-6">
                <button className="flex items-center space-x-2 text-gray-600 hover:text-red-600 transition-colors">
                  <Heart size={20} />
                  <span>{post.claps || 0}</span>
                </button>
                <button className="flex items-center space-x-2 text-gray-600 hover:text-blue-600 transition-colors">
                  <MessageSquare size={20} />
                  <span>{post.comments || 0}</span>
                </button>
              </div>
              {post.status === 'DRAFT' && (
                <span className="bg-yellow-100 text-yellow-800 text-sm font-medium px-3 py-1 rounded-full">
                  Draft
                </span>
              )}
            </div>
          </footer>
        </div>
      </article>
    </main>
  );
};

export default PostDetail;
