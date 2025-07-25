import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { PlusCircle } from 'lucide-react';
import { mockPosts } from '../mockData';

// Post Editor Page (for Admin/Author)
const PostEditor = ({ onNavigate, postId = null }) => {
  const { user } = useContext(AuthContext);
  const isEditMode = postId !== null;
  const existingPost = isEditMode ? mockPosts.find(p => p.id === postId) : null;

  const [title, setTitle] = useState(existingPost ? existingPost.title : '');
  const [content, setContent] = useState(existingPost ? existingPost.content : '');
  const [status, setStatus] = useState(existingPost ? existingPost.status : 'DRAFT');
  const [imageUrl, setImageUrl] = useState(existingPost ? existingPost.imageUrl : '');
  const [message, setMessage] = useState('');

  // Restrict access
  useEffect(() => {
    if (!user || (user.role !== 'ADMIN' && (!isEditMode || user.id !== existingPost?.authorId))) {
      // In a real app, use a custom modal or toast notification instead of alert
      alert('You are not authorized to access this page.');
      onNavigate('home');
    }
  }, [user, isEditMode, existingPost, onNavigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('Saving post...');

    const newPostData = {
      title,
      content,
      status,
      imageUrl,
      // In a real app, authorId would come from authenticated user
      author: user.email, // Using email as author name for mock
      authorId: user.id,
      publishedAt: status === 'PUBLISHED' ? new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : null,
      claps: existingPost ? existingPost.claps : 0,
      comments: existingPost ? existingPost.comments : 0,
    };

    if (isEditMode) {
      // Simulate API call for update
      console.log('Updating post:', { ...newPostData, id: postId });
      // In a real app: await fetch(`/api/posts/${postId}`, { method: 'PUT', body: JSON.stringify(newPostData) });
      setMessage('Post updated successfully! (Simulated)');
    } else {
      // Simulate API call for create
      const newId = (mockPosts.length + 1).toString(); // Simple ID generation
      console.log('Creating new post:', { ...newPostData, id: newId });
      // In a real app: await fetch('/api/posts', { method: 'POST', body: JSON.stringify(newPostData) });
      setMessage('Post created successfully! (Simulated)');
    }

    setTimeout(() => {
      setMessage('');
      onNavigate('home'); // Go back to home after saving
    }, 1500);
  };

  if (!user || (user.role !== 'ADMIN' && (!isEditMode || user.id !== existingPost?.authorId))) {
    return null; // Render nothing while redirecting or unauthorized
  }

  return (
    <div className="max-w-4xl mx-auto px-6 md:px-12 py-8">
      <h1 className="text-3xl font-bold font-serif text-gray-900 mb-6">
        {isEditMode ? 'Edit Post' : 'Create New Post'}
      </h1>
      <form onSubmit={handleSubmit} className="space-y-6 bg-white p-8 rounded-lg shadow-lg">
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">Title</label>
          <input
            type="text"
            id="title"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">Content</label>
          <textarea
            id="content"
            rows="10"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          ></textarea>
        </div>
        <div>
          <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">Image URL (Optional)</label>
          <input
            type="text"
            id="imageUrl"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
            placeholder="e.g., https://example.com/image.jpg"
          />
        </div>
        <div>
          <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select
            id="status"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="DRAFT">Draft</option>
            <option value="PUBLISHED">Published</option>
          </select>
        </div>
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => onNavigate('home')}
            className="px-6 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition duration-200"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-6 py-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition duration-200 flex items-center space-x-2"
          >
            <PlusCircle size={20} />
            <span>{isEditMode ? 'Update Post' : 'Create Post'}</span>
          </button>
        </div>
        {message && (
          <p className="mt-4 text-center text-sm text-gray-600">{message}</p>
        )}
      </form>
    </div>
  );
};

export default PostEditor;
