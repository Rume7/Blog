import React, { useState, useRef } from 'react';
import { Upload, X, Image as ImageIcon } from 'lucide-react';
import apiService from '../services/api';

const ImageUpload = ({ 
  onImageUploaded, 
  imageType = 'FEATURED_IMAGE', 
  currentImageUrl = '', 
  altText = '', 
  description = '',
  className = '',
  showPreview = true 
}) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(currentImageUrl);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setError('Please select a valid image file (JPEG, PNG, GIF, or WebP)');
      return;
    }

    // Validate file size (5MB limit)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      setError('File size must be less than 5MB');
      return;
    }

    setError('');
    setSelectedFile(file);

    // Create preview URL
    const reader = new FileReader();
    reader.onload = (e) => {
      setPreviewUrl(e.target.result);
    };
    reader.readAsDataURL(file);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    setUploadProgress(0);
    setError('');

    try {
      let response;
      
      if (imageType === 'PROFILE_PICTURE') {
        response = await apiService.uploadProfilePicture(selectedFile, altText, description);
      } else {
        response = await apiService.uploadImage(selectedFile, imageType, altText, description);
      }

      // Simulate upload progress
      for (let i = 0; i <= 100; i += 10) {
        setUploadProgress(i);
        await new Promise(resolve => setTimeout(resolve, 50));
      }

      // Call the callback with the uploaded image data
      if (onImageUploaded) {
        onImageUploaded(response);
      }

      // Clear the form
      setSelectedFile(null);
      setUploadProgress(0);
      
      // Keep the preview URL for the uploaded image
      setPreviewUrl(response.filePath || response.url);

    } catch (err) {
      setError(err.message || 'Failed to upload image');
      setUploadProgress(0);
    } finally {
      setIsUploading(false);
    }
  };

  const handleRemoveImage = () => {
    setSelectedFile(null);
    setPreviewUrl('');
    setError('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      const file = files[0];
      const event = { target: { files: [file] } };
      handleFileSelect(event);
    }
  };

  return (
    <div className={`space-y-4 ${className}`}>
      {/* File Input */}
      <div
        className={`border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-green-500 transition-colors cursor-pointer ${
          selectedFile ? 'border-green-500 bg-green-50' : ''
        }`}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleFileSelect}
          className="hidden"
          disabled={isUploading}
        />
        
        {!previewUrl && !selectedFile ? (
          <div className="space-y-2">
            <Upload className="mx-auto h-12 w-12 text-gray-400" />
            <div className="text-sm text-gray-600">
              <p className="font-medium">Click to upload or drag and drop</p>
              <p className="text-xs">PNG, JPG, GIF, WebP up to 5MB</p>
            </div>
          </div>
        ) : (
          <div className="space-y-2">
            <ImageIcon className="mx-auto h-8 w-8 text-green-500" />
            <p className="text-sm text-gray-600">
              {selectedFile ? 'File selected: ' + selectedFile.name : 'Image uploaded'}
            </p>
          </div>
        )}
      </div>

      {/* Preview */}
      {showPreview && previewUrl && (
        <div className="relative">
          <img
            src={previewUrl}
            alt="Preview"
            className="w-full h-48 object-cover rounded-lg border border-gray-200"
          />
          <button
            type="button"
            onClick={handleRemoveImage}
            className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
            disabled={isUploading}
          >
            <X size={16} />
          </button>
        </div>
      )}

      {/* Upload Progress */}
      {isUploading && (
        <div className="space-y-2">
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-green-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${uploadProgress}%` }}
            ></div>
          </div>
          <p className="text-sm text-gray-600 text-center">
            Uploading... {uploadProgress}%
          </p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="text-red-600 text-sm bg-red-50 p-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Upload Button */}
      {selectedFile && !isUploading && (
        <button
          type="button"
          onClick={handleUpload}
          className="w-full bg-green-600 text-white py-2 px-4 rounded-lg hover:bg-green-700 transition-colors flex items-center justify-center space-x-2"
        >
          <Upload size={16} />
          <span>Upload Image</span>
        </button>
      )}

      {/* Current Image URL (for forms that also accept URLs) */}
      {!selectedFile && previewUrl && (
        <div className="text-sm text-gray-600">
          <p>Current image: {previewUrl}</p>
        </div>
      )}
    </div>
  );
};

export default ImageUpload; 