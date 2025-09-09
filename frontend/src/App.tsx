import React, { useState, useEffect } from 'react';
import './App.css';

// We define a 'type' for our expected API response.
// This tells TypeScript our data will be an object with a 'text' property that is a string.
type MessageResponse = {
  text: string;
};

function App() {
  // By using <string>, we tell useState that the 'message' state will always be a string.
  const [message, setMessage] = useState<string>("Loading...");

  useEffect(() => {
    const fetchMessage = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/hello');

        // We tell TypeScript to expect the JSON to match our MessageResponse type.
        const data: MessageResponse = await response.json();

        setMessage(data.text);
      } catch (error) {
        setMessage("Failed to fetch message from backend.");
        console.error("Error fetching data: ", error);
      }
    };

    fetchMessage();
  }, []);

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