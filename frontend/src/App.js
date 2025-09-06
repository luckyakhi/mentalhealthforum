import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  // State to store the message we get from the backend
  const [message, setMessage] = useState("Loading...");

  // useEffect hook to fetch data when the component mounts
  useEffect(() => {
    // We use a try-catch block to handle potential network errors
    const fetchMessage = async () => {
      try {
        // Fetch data from our Spring Boot backend endpoint
        const response = await fetch('http://localhost:8080/api/hello');
        const data = await response.json();
        setMessage(data.text);
      } catch (error) {
        setMessage("Failed to fetch message from backend.");
        console.error("Error fetching data: ", error);
      }
    };

    fetchMessage();
  }, []); // The empty array [] means this effect runs only once after the initial render

  return (
    <div className="App">
      <header className="App-header">
        <h1>Mental Health Forum</h1>
        <p>Message from our backend: <strong>{message}</strong></p>
      </header>
    </div>
  );
}

export default App;