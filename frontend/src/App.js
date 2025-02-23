import './App.css';
import React from 'react';
import Navbar from './Components/Navbar/Navbar'
import { BrowserRouter as Router } from 'react-router-dom';
import Routes from './routes';

function App() {

  return (
    <>
      <div className='App'>
        <Router>
          <Navbar />
          <Routes />
        </Router>
      </div>
    </>
  );
}

export default App;