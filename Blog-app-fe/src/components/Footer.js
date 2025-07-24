import React from 'react';

// Footer Component
const Footer = () => {
  return (
    <footer className="bg-gray-100 border-t border-gray-200 py-6 px-6 md:px-12 mt-12">
      <div className="max-w-7xl mx-auto text-center text-gray-600 text-sm">
        &copy; {new Date().getFullYear()} MyBlog. All rights reserved.
      </div>
    </footer>
  );
};

export default Footer;
