import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { PlusCircle, Save } from 'lucide-react';
import ImageUpload from '../components/ImageUpload';
import apiService from '../services/api';

// Post Editor Page (for Admin/Author)
const PostEditor = ({ onNavigate, postId = null }) => {
  const { user } = useContext(AuthContext);
  const isEditMode = postId !== null;

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [status, setStatus] = useState('DRAFT');
  const [imageUrl, setImageUrl] = useState('');
  const [imageId, setImageId] = useState(null);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Load existing post data if editing
  useEffect(() => {
    if (isEditMode && postId) {
      loadPost();
    }
  }, [postId, isEditMode]);

  const loadPost = async () => {
    try {
      setIsLoading(true);
      const post = await apiService.getPost(postId);
      setTitle(post.title || '');
      setContent(post.content || '');
      setStatus(post.status || 'DRAFT');
      setImageUrl(post.featuredImageUrl || '');
      setImageId(post.featuredImageId || null);
    } catch (err) {
      setError('Failed to load post: ' + err.message);
      console.error('Load post error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // Restrict access
  useEffect(() => {
    if (!user) {
      onNavigate('login');
      return;
    }

    if (user.role !== 'ADMIN' && user.role !== 'MODERATOR') {
      // Regular users can only create posts, not edit others' posts
      if (isEditMode) {
        setError('You are not authorized to edit posts.');
        onNavigate('home');
      }
    }
  }, [user, isEditMode, onNavigate]);

  const handleImageUploaded = (imageData) => {
    setImageUrl(imageData.filePath || imageData.url);
    setImageId(imageData.id);
    setMessage('Image uploaded successfully!');
    setTimeout(() => setMessage(''), 3000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    setMessage('Saving post...');

    try {
      const postData = {
        title,
        content,
        status,
        featuredImageId: imageId,
        featuredImageUrl: imageUrl,
      };

      let response;
      if (isEditMode) {
        response = await apiService.updatePost(postId, postData);
        setMessage('Post updated successfully!');
      } else {
        response = await apiService.createPost(postData);
        setMessage('Post created successfully!');
      }

      console.log('Post saved:', response);
      
      setTimeout(() => {
        setMessage('');
        onNavigate('home');
      }, 1500);
    } catch (err) {
      setError('Failed to save post: ' + err.message);
      console.error('Save post error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="min-h-[calc(100vh-200px)] flex items-center justify-center text-center text-gray-700">
        <p>Please log in to create or edit posts.</p>
      </div>
    );
  }

  if (isLoading && isEditMode) {
    return (
      <div className="min-h-[calc(100vh-200px)] flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 md:px-12 py-8">
      <h1 className="text-3xl font-bold font-serif text-gray-900 mb-6">
        {isEditMode ? 'Edit Post' : 'Create New Post'}
      </h1>
      
      <form onSubmit={handleSubmit} className="space-y-6 bg-white p-8 rounded-lg shadow-lg">
        {error && (
          <div className="text-red-600 text-sm bg-red-50 p-3 rounded-lg">
            {error}
          </div>
        )}

        {message && (
          <div className="text-green-600 text-sm bg-green-50 p-3 rounded-lg">
            {message}
          </div>
        )}

        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
            Title *
          </label>
          <input
            type="text"
            id="title"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            disabled={isLoading}
          />
        </div>

        <div>
          <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">
            Content *
          </label>
          <textarea
            id="content"
            rows="10"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            disabled={isLoading}
            placeholder="Write your post content here..."
          ></textarea>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Featured Image
          </label>
          <ImageUpload
            onImageUploaded={handleImageUploaded}
            imageType="FEATURED_IMAGE"
            currentImageUrl={imageUrl}
            altText={`Featured image for: ${title}`}
            description={`Featured image for the post "${title}"`}
            showPreview={true}
          />
        </div>

        <div>
          <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
            Status
          </label>
          <select
            id="status"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            disabled={isLoading}
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
            disabled={isLoading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-6 py-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition duration-200 flex items-center space-x-2 disabled:opacity-50"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                <span>Saving...</span>
              </>
            ) : (
              <>
                {isEditMode ? <Save size={20} /> : <PlusCircle size={20} />}
                <span>{isEditMode ? 'Update Post' : 'Create Post'}</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default PostEditor;
