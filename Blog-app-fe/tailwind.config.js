/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}", // Scan all JS/JSX/TS/TSX files in src for Tailwind classes
    "./public/index.html",       // Also scan your public HTML file
  ],
  theme: {
    extend: {
      fontFamily: {
        // Define 'Inter' as a custom font family, falling back to sans-serif
        // You'll need to import Inter from Google Fonts or similar in your index.css
        inter: ['Inter', 'sans-serif'],
        // Add a serif font for titles, mimicking Medium
        serif: ['Georgia', 'serif'],
      },
      colors: {
        // Define custom colors if needed, e.g., a specific Medium green
        'medium-green': '#00B87C', // Example green, adjust as needed
      },
    },
  },
  plugins: [
    // Removed @tailwindcss/line-clamp as it's included by default in Tailwind CSS v3.3+
    // require('@tailwindcss/line-clamp'), // This line was removed
  ],
};
