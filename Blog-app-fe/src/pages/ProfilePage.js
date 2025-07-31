import React, { useState, useContext, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import { User, Camera, Save } from 'lucide-react';
import ImageUpload from '../components/ImageUpload';
import apiService from '../services/api';

// Profile Page
const ProfilePage = ({ onNavigate }) => {
  const { user, updateProfile } = useContext(AuthContext);
  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    bio: '',
    website: '',
  });
  const [profilePicture, setProfilePicture] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Load user profile data
  useEffect(() => {
    if (user) {
      setProfileData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        bio: user.bio || '',
        website: user.website || '',
      });
      setProfilePicture(user.profilePictureUrl || '');
    }
  }, [user]);

  const handleImageUploaded = (imageData) => {
    setProfilePicture(imageData.filePath || imageData.url);
    setMessage('Profile picture uploaded successfully!');
    setTimeout(() => setMessage(''), 3000);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProfile = async () => {
    setIsLoading(true);
    setError('');
    setMessage('Saving profile...');

    try {
      const result = await updateProfile({
        ...profileData,
        profilePictureUrl: profilePicture,
      });

      if (result.success) {
        setMessage('Profile updated successfully!');
        setIsEditing(false);
      } else {
        setError(result.error || 'Failed to update profile');
      }
    } catch (err) {
      setError('Failed to update profile: ' + err.message);
      console.error('Profile update error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="min-h-[calc(100vh-200px)] flex items-center justify-center text-center text-gray-700">
        <p>Please log in to view your profile.</p>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-200px)] bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-green-600 to-green-700 px-6 py-8 text-white">
            <div className="flex items-center space-x-4">
              <div className="relative">
                {profilePicture ? (
                  <img
                    src={profilePicture}
                    alt="Profile"
                    className="w-20 h-20 rounded-full object-cover border-4 border-white"
                  />
                ) : (
                  <div className="w-20 h-20 rounded-full bg-white/20 flex items-center justify-center border-4 border-white">
                    <User size={32} />
                  </div>
                )}
                {isEditing && (
                  <div className="absolute -bottom-1 -right-1 bg-green-500 rounded-full p-1">
                    <Camera size={16} className="text-white" />
                  </div>
                )}
              </div>
              <div>
                <h1 className="text-2xl font-bold">
                  {profileData.firstName && profileData.lastName 
                    ? `${profileData.firstName} ${profileData.lastName}`
                    : user.email
                  }
                </h1>
                <p className="text-green-100">{user.email}</p>
                <p className="text-sm text-green-200 capitalize">{user.role?.toLowerCase()}</p>
              </div>
            </div>
          </div>

          {/* Content */}
          <div className="p-6">
            {error && (
              <div className="mb-4 text-red-600 text-sm bg-red-50 p-3 rounded-lg">
                {error}
              </div>
            )}

            {message && (
              <div className="mb-4 text-green-600 text-sm bg-green-50 p-3 rounded-lg">
                {message}
              </div>
            )}

            {/* Profile Picture Upload */}
            {isEditing && (
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Profile Picture
                </label>
                <ImageUpload
                  onImageUploaded={handleImageUploaded}
                  imageType="PROFILE_PICTURE"
                  currentImageUrl={profilePicture}
                  altText={`Profile picture for ${user.email}`}
                  description={`Profile picture for ${user.email}`}
                  showPreview={false}
                  className="max-w-xs"
                />
              </div>
            )}

            {/* Profile Form */}
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                    First Name
                  </label>
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={profileData.firstName}
                    onChange={handleInputChange}
                    disabled={!isEditing || isLoading}
                    className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 disabled:bg-gray-50"
                  />
                </div>
                <div>
                  <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                    Last Name
                  </label>
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={profileData.lastName}
                    onChange={handleInputChange}
                    disabled={!isEditing || isLoading}
                    className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 disabled:bg-gray-50"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="bio" className="block text-sm font-medium text-gray-700 mb-1">
                  Bio
                </label>
                <textarea
                  id="bio"
                  name="bio"
                  rows="3"
                  value={profileData.bio}
                  onChange={handleInputChange}
                  disabled={!isEditing || isLoading}
                  className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 disabled:bg-gray-50"
                  placeholder="Tell us about yourself..."
                />
              </div>

              <div>
                <label htmlFor="website" className="block text-sm font-medium text-gray-700 mb-1">
                  Website
                </label>
                <input
                  type="url"
                  id="website"
                  name="website"
                  value={profileData.website}
                  onChange={handleInputChange}
                  disabled={!isEditing || isLoading}
                  className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 disabled:bg-gray-50"
                  placeholder="https://example.com"
                />
              </div>
            </div>

            {/* Action Buttons */}
            <div className="mt-8 flex justify-end space-x-4">
              <button
                onClick={() => onNavigate('home')}
                className="px-6 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition duration-200"
                disabled={isLoading}
              >
                Back to Home
              </button>
              
              {isEditing ? (
                <>
                  <button
                    onClick={() => setIsEditing(false)}
                    className="px-6 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition duration-200"
                    disabled={isLoading}
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveProfile}
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
                        <Save size={16} />
                        <span>Save Changes</span>
                      </>
                    )}
                  </button>
                </>
              ) : (
                <button
                  onClick={() => setIsEditing(true)}
                  className="px-6 py-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition duration-200"
                >
                  Edit Profile
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
