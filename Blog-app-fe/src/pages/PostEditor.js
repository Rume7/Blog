import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ImageUpload from '../components/ImageUpload';
import apiService from '../services/api';

const PostEditor = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [post, setPost] = useState({
    title: '',
    content: '',
    status: 'DRAFT',
    imageUrl: '',
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const isEditing = !!id;

  const loadPost = useCallback(async () => {
    if (!isEditing) return;
    
    try {
      setLoading(true);
      const postData = await apiService.getPost(id);
      setPost({
        title: postData.title || '',
        content: postData.content || '',
        status: postData.status || 'DRAFT',
        imageUrl: postData.imageUrl || '',
      });
    } catch (error) {
      console.error('Failed to load post:', error);
      setError('Failed to load post');
    } finally {
      setLoading(false);
    }
  }, [id, isEditing]);

  useEffect(() => {
    loadPost();
  }, [loadPost]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setPost(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleImageUploaded = (imageData) => {
    setPost(prev => ({
      ...prev,
      imageUrl: imageData.filePath || imageData.url
    }));
    setMessage('Image uploaded successfully!');
    setTimeout(() => setMessage(''), 3000);
  };

  const handleSave = async (status = 'DRAFT') => {
    if (!post.title.trim()) {
      setError('Title is required');
      return;
    }

    if (!post.content.trim()) {
      setError('Content is required');
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');

      const postData = {
        ...post,
        status,
      };

      let savedPost;
      if (isEditing) {
        savedPost = await apiService.updatePost(id, postData);
      } else {
        savedPost = await apiService.createPost(postData);
      }

      setMessage(`Post ${isEditing ? 'updated' : 'created'} successfully!`);
      setTimeout(() => {
        navigate(`/posts/${savedPost.id}`);
      }, 2000);
    } catch (error) {
      setError(error.message);
    } finally {
      setSaving(false);
    }
  };

  const handlePublish = () => {
    handleSave('PUBLISHED');
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Please log in to create or edit posts.</div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading post...</div>
      </div>
    );
  }

  return (
    <main className="max-w-4xl mx-auto px-6 md:px-12 py-8">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            {isEditing ? 'Edit Post' : 'Create New Post'}
          </h1>
          <div className="flex space-x-4">
            <button
              onClick={() => handleSave('DRAFT')}
              disabled={saving}
              className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition duration-200 disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save Draft'}
            </button>
            <button
              onClick={handlePublish}
              disabled={saving}
              className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition duration-200 disabled:opacity-50"
            >
              {saving ? 'Publishing...' : 'Publish'}
            </button>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {message && (
          <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
            <p className="text-green-700">{message}</p>
          </div>
        )}

        <div className="space-y-6">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
              Title
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={post.title}
              onChange={handleInputChange}
              disabled={saving}
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
              placeholder="Enter post title..."
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Featured Image
            </label>
            <ImageUpload
              onUploadSuccess={handleImageUploaded}
              uploadType="featured"
              currentImageUrl={post.imageUrl}
              buttonText={post.imageUrl ? 'Change Image' : 'Upload Image'}
            />
            {post.imageUrl && (
              <div className="mt-2">
                <img
                  src={post.imageUrl}
                  alt="Featured"
                  className="w-32 h-32 object-cover rounded-lg"
                />
              </div>
            )}
          </div>

          <div>
            <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-2">
              Content
            </label>
            <textarea
              id="content"
              name="content"
              value={post.content}
              onChange={handleInputChange}
              disabled={saving}
              rows={15}
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
              placeholder="Write your post content here..."
            />
          </div>

          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
              Status
            </label>
            <select
              id="status"
              name="status"
              value={post.status}
              onChange={handleInputChange}
              disabled={saving}
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
            >
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
            </select>
          </div>
        </div>
      </div>
    </main>
  );
};

export default PostEditor;
