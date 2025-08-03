import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Heart, MessageSquare } from 'lucide-react';

// Post Card Component
const PostCard = ({ post }) => {
  const { user } = useAuth();
  const isAuthor = user && user.id === post.authorId;
  const isAdmin = user && user.role === 'ADMIN';

  // Only show draft posts to admin or the author
  if (post.status === 'DRAFT' && !isAdmin && !isAuthor) {
    return null;
  }

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 overflow-hidden flex flex-col">
      {post.imageUrl && (
        <img
          src={post.imageUrl}
          alt={post.title}
          className="w-full h-48 object-cover"
          onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/600x400/E0E0E0/333333?text=Image+Not+Found'; }}
        />
      )}
      <div className="p-6 flex flex-col flex-grow">
        <Link
          to={`/posts/${post.id}`}
          className="text-xl font-semibold text-gray-900 mb-2 font-serif cursor-pointer hover:text-green-600 hover:underline"
        >
          {post.title}
        </Link>
        <p className="text-gray-700 text-sm mb-4 line-clamp-3 flex-grow">{post.content}</p>
        {(isAdmin || isAuthor) && post.status === 'DRAFT' && (
          <span className="mb-2 inline-block bg-yellow-100 text-yellow-800 text-xs font-medium px-2.5 py-0.5 rounded-full self-start">
            Draft
          </span>
        )}
        <div className="flex items-center justify-between text-gray-500 text-xs mt-auto">
          <span>{post.author} &bull; {post.publishedAt || 'Draft'}</span>
          <div className="flex items-center space-x-4">
            <span className="flex items-center space-x-1">
              <Heart size={16} /> {post.claps || 0}
            </span>
            <span className="flex items-center space-x-1">
              <MessageSquare size={16} /> {post.comments || 0}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PostCard;
