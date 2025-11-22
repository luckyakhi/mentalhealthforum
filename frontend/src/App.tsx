// File: frontend/src/App.tsx

import { useState, useEffect } from 'react'; // <-- CORRECTED LINE
import './App.css';

type MessageResponse = {
  text: string;
};

function App() {
  const [message, setMessage] = useState<string>("Loading...");

  useEffect(() => {
    const fetchMessage = async () => {
      try {
        const response = await fetch('/api/hello');
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