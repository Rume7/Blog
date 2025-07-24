# Stage 1: Build the React application
# Use a Node.js image with a specific version (e.g., 18-alpine for a smaller base)
FROM node:18-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock) to leverage Docker cache
# This step installs dependencies. If these files don't change, this layer is cached.
COPY package.json ./
COPY package-lock.json ./
RUN npm ci 

# Copy the rest of your application code
COPY . ./

# Build the React application for production
# This command typically runs 'react-scripts build' or 'vite build'
RUN npm run build

# Stage 2: Serve the built application with Nginx
# Use a lightweight Nginx image
FROM nginx:alpine

# Copy the built React app from the 'build' stage into Nginx's default static file directory
# The 'build' folder is where 'npm run build' or 'yarn build' outputs your static files.
COPY --from=build /app/build /usr/share/nginx/html

# Optional: Copy a custom Nginx configuration if you have one
# For most React SPAs, the default Nginx config is fine, but you might need it for routing.
# If you have a custom nginx.conf, place it in your frontend root and uncomment:
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80, which is the default for Nginx
EXPOSE 80

# Command to run Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
