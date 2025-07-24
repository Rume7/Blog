import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { User, Heart, MessageSquare, Trash2, Edit } from 'lucide-react';
import { mockPosts } from '../mockData'; // Import mock data
import RecentPostsSidebar from '../components/RecentPostsSidebar'; // Import the new sidebar component

// Post Detail Component
const PostDetail = ({ postId, onNavigate }) => {
  const { user } = useContext(AuthContext);
  const post = mockPosts.find(p => p.id === postId);

  if (!post) {
    return (
      <div className="max-w-3xl mx-auto px-6 md:px-12 py-8 text-center text-gray-700">
        <h2 className="text-2xl font-bold mb-4">Post Not Found</h2>
        <button
          onClick={() => onNavigate('home')}
          className="text-green-600 hover:underline"
        >
          Go back to home
        </button>
      </div>
    );
  }

  const isAuthor = user && user.id === post.authorId;
  const isAdmin = user && user.role === 'ADMIN';

  // Prevent unauthorized access to draft posts
  if (post.status === 'DRAFT' && !isAdmin && !isAuthor) {
    return (
      <div className="max-w-3xl mx-auto px-6 md:px-12 py-8 text-center text-gray-700">
        <h2 className="text-2xl font-bold mb-4">Unauthorized Access</h2>
        <p className="mb-4">This post is a draft and can only be viewed by the author or an administrator.</p>
        <button
          onClick={() => onNavigate('home')}
          className="text-green-600 hover:underline"
        >
          Go back to home
        </button>
      </div>
    );
  }

  const handleClap = () => {
    if (!user) {
      // In a real app, use a custom modal or toast notification instead of alert
      alert('Please log in to clap for a post.');
      return;
    }
    // In a real app, send a request to your backend to record the clap
    console.log(`User ${user.id} clapped for post ${postId}`);
    // In a real app, use a custom modal or toast notification instead of alert
    alert('Clap recorded! (Simulated)');
    // You'd typically update the claps count from the backend response
  };

  const handleDelete = () => {
    // In a real app, use a custom modal for confirmation instead of window.confirm
    if (window.confirm('Are you sure you want to delete this post?')) {
      // In a real app, send DELETE request to backend
      console.log(`Deleting post ${postId}`);
      // In a real app, use a custom modal or toast notification instead of alert
      alert('Post deleted! (Simulated)');
      onNavigate('home'); // Go back to home after deletion
    }
  };

  const handleEdit = () => {
    onNavigate('editPost', postId);
  };

  return (
    <main className="max-w-7xl mx-auto px-6 md:px-12 py-8 flex"> {/* Added flex container */}
      <div className="flex-grow lg:w-3/4 pr-8"> {/* Main content area */}
        <h1 className="text-4xl font-bold font-serif text-gray-900 mb-4">{post.title}</h1>
        <div className="flex items-center text-gray-600 text-sm mb-6">
          <User size={20} className="mr-2" />
          <span>{post.author} &bull; {post.publishedAt || 'Draft'}</span>
          {(isAdmin || isAuthor) && post.status === 'DRAFT' && (
            <span className="ml-4 bg-yellow-100 text-yellow-800 text-xs font-medium px-2.5 py-0.5 rounded-full">
              Draft
            </span>
          )}
        </div>

        {post.imageUrl && (
          <img
            src={post.imageUrl}
            alt={post.title}
            className="w-full h-80 object-cover rounded-lg mb-8"
            onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/800x400/E0E0E0/333333?text=Image+Not+Found'; }}
          />
        )}

        <div className="prose prose-lg max-w-none text-gray-800 leading-relaxed mb-8">
          <p>{post.content}</p>
          {/* Add more content here, perhaps from a rich text editor */}
          <p>This is a placeholder for the full blog post content. In a real application, this would be much longer and formatted with paragraphs, headings, images, etc.</p>
          <p>You can imagine more detailed explanations, code snippets, and embedded media here.</p>
        </div>

        <div className="flex items-center justify-between border-t border-gray-200 pt-6">
          <div className="flex items-center space-x-4">
            <button
              onClick={handleClap}
              className="flex items-center space-x-2 bg-green-100 text-green-800 px-4 py-2 rounded-full hover:bg-green-200 transition duration-200"
            >
              <Heart size={20} className="fill-current" />
              <span>Clap ({post.claps})</span>
            </button>
            <span className="flex items-center space-x-1 text-gray-600">
              <MessageSquare size={20} />
              <span>{post.comments} Comments</span>
            </span>
          </div>
          {(isAdmin || isAuthor) && (
            <div className="flex space-x-2">
              <button
                onClick={handleEdit}
                className="flex items-center space-x-1 bg-blue-100 text-blue-800 px-3 py-2 rounded-full hover:bg-blue-200 transition duration-200 text-sm"
              >
                <Edit size={16} />
                <span>Edit</span>
              </button>
              <button
                onClick={handleDelete}
                className="flex items-center space-x-1 bg-red-100 text-red-800 px-3 py-2 rounded-full hover:bg-red-200 transition duration-200 text-sm"
              >
                <Trash2 size={16} />
                <span>Delete</span>
              </button>
            </div>
          )}
        </div>
        <button
          onClick={() => onNavigate('home')}
          className="mt-8 text-green-600 hover:underline flex items-center space-x-1"
        >
          &larr; Back to all posts
        </button>
      </div>

      {/* Right Sidebar */}
      <RecentPostsSidebar posts={mockPosts} onNavigate={onNavigate} />
    </main>
  );
};

export default PostDetail;
