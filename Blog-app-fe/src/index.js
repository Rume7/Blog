import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; // Assuming you have an index.css for global styles/Tailwind imports
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
