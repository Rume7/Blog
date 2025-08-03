// API Service for backend communication
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  // Helper method to get auth headers
  getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  }

  // Helper method to handle API responses
  async handleResponse(response) {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    return response.json();
  }

  // Authentication endpoints
  async login(email) {
    try {
      const response = await fetch(`${this.baseURL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData);
      }

      const result = await response.text();
      return { success: true, message: result };
    } catch (error) {
      throw new Error(`Login failed: ${error.message}`);
    }
  }

  async verifyMagicLink(token) {
    try {
      const response = await fetch(`${this.baseURL}/auth/verify-magic-link?token=${token}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData);
      }

      const jwtToken = await response.text();
      return jwtToken;
    } catch (error) {
      throw new Error(`Magic link verification failed: ${error.message}`);
    }
  }

  async register(userData) {
    const response = await fetch(`${this.baseURL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });
    
    return this.handleResponse(response);
  }

  async getCurrentUser() {
    const response = await fetch(`${this.baseURL}/auth/me`, {
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  // Posts endpoints
  async getPosts(page = 0, size = 10, search = '') {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(search && { search }),
    });
    
    const response = await fetch(`${this.baseURL}/posts?${params}`, {
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  async getPost(id) {
    const response = await fetch(`${this.baseURL}/posts/${id}`, {
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  async createPost(postData) {
    const response = await fetch(`${this.baseURL}/posts`, {
      method: 'POST',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(postData),
    });
    
    return this.handleResponse(response);
  }

  async updatePost(id, postData) {
    const response = await fetch(`${this.baseURL}/posts/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(postData),
    });
    
    return this.handleResponse(response);
  }

  async deletePost(id) {
    const response = await fetch(`${this.baseURL}/posts/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  // Image endpoints
  async uploadImage(file, imageType, altText = '', description = '') {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('imageType', imageType);
    if (altText) formData.append('altText', altText);
    if (description) formData.append('description', description);

    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseURL}/images/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    
    return this.handleResponse(response);
  }

  async uploadProfilePicture(file, altText = '', description = '') {
    const formData = new FormData();
    formData.append('file', file);
    if (altText) formData.append('altText', altText);
    if (description) formData.append('description', description);

    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseURL}/images/profile-picture`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    
    return this.handleResponse(response);
  }

  async getImage(id) {
    const response = await fetch(`${this.baseURL}/images/${id}`, {
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  async getImageFile(id) {
    const response = await fetch(`${this.baseURL}/images/${id}/file`, {
      headers: this.getAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.blob();
  }

  async getUserProfilePicture(userId) {
    const response = await fetch(`${this.baseURL}/images/profile/${userId}`, {
      headers: this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  // User endpoints
  async updateProfile(userData) {
    const response = await fetch(`${this.baseURL}/users/profile`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(userData),
    });
    
    return this.handleResponse(response);
  }

  // Logout
  logout() {
    localStorage.removeItem('authToken');
  }
}

// Create and export a singleton instance
const apiService = new ApiService();
export default apiService; 