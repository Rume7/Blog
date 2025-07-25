import { render } from '@testing-library/react';
import App from './App';

// Simplest test: just checks if the App component renders without throwing an error
test('App renders without crashing', () => {
  render(<App />);
  // If render() completes without throwing an error, the test passes.
  // No specific element assertion is needed for this basic check.
});

// You can add more specific tests later as your application grows
// For example, checking for specific text, buttons, or user interactions.
