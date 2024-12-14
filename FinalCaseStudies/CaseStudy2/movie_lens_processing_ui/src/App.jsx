/*import React from "react";
import { Container, Typography, Grid } from "@mui/material";
import MovieList from "./components/MovieList";
import GenreChart from "./components/GenreChart";
import DemographicsChart from "./components/DemographicsChart";

function App() {
  return (
    <Container maxWidth="lg" style={{ marginTop: "20px" }}>
      <Typography variant="h3" align="center" gutterBottom>
        Movie Metrics Dashboard
      </Typography>
      
      <Grid container spacing={4}>
        {/* Top Rated Movies */ /*}
        <Grid item xs={12} md={6}>
          <Typography variant="h5" gutterBottom>
            Top-Rated Movies
          </Typography>
          <MovieList />
        </Grid>

        {/* Most Popular Genres */ /*}
        <Grid item xs={12} md={6}>
          <Typography variant="h5" gutterBottom>
            Most Popular Genres
          </Typography>
          <GenreChart />
        </Grid>

        {/* Ratings by Demographics */ /*}
        <Grid item xs={12}>
          <Typography variant="h5" gutterBottom>
            Ratings by Demographics
          </Typography>
          <DemographicsChart />
        </Grid>
      </Grid>
    </Container>
  );
}

export default App;*/

/*import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const App = () => {
  const [movieMetrics, setMovieMetrics] = useState([]);
  const [genreMetrics, setGenreMetrics] = useState([]);
  const [userDemoMetrics, setUserDemoMetrics] = useState([]);

  // Fetch data from the backend API
  const fetchMovieMetrics = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/movie-metrics');
      setMovieMetrics(JSON.parse(response.data)); // Assuming response is in JSON format
    } catch (error) {
      console.error('Error fetching movie metrics', error);
    }
  };

  const fetchGenreMetrics = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/genre-metrics');
      setGenreMetrics(JSON.parse(response.data)); // Assuming response is in JSON format
    } catch (error) {
      console.error('Error fetching genre metrics', error);
    }
  };

  const fetchUserDemoMetrics = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/demographics-metrics');
      setUserDemoMetrics(JSON.parse(response.data)); // Assuming response is in JSON format
    } catch (error) {
      console.error('Error fetching user demographic metrics', error);
    }
  };

  useEffect(() => {
    fetchMovieMetrics();
    fetchGenreMetrics();
    fetchUserDemoMetrics();
  }, []);

  // Render the Movie Metrics Table
  const renderMovieTable = (data) => (
    <div className="metrics-table">
      <h2>Movie Metrics</h2>
      <table>
        <thead>
          <tr>
            <th>Movie ID</th>
            <th>Title</th>
            <th>Genres</th>
            <th>Average Rating</th>
            <th>Total Ratings</th>
          </tr>
        </thead>
        <tbody>
          {data.map((movie, index) => (
            <tr key={index}>
              <td>{movie.movieId}</td>
              <td>{movie.title}</td>
              <td>{movie.genres}</td>
              <td>{movie.average_rating}</td>
              <td>{movie.total_ratings}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  // Render the Genre Metrics Table
  const renderGenreTable = (data) => (
    <div className="metrics-table">
      <h2>Genre Metrics</h2>
      <table>
        <thead>
          <tr>
            <th>Genre</th>
            <th>Average Rating</th>
            <th>Total Ratings</th>
          </tr>
        </thead>
        <tbody>
          {data.map((genre, index) => (
            <tr key={index}>
              <td>{genre.genre}</td>
              <td>{genre.average_rating}</td>
              <td>{genre.total_ratings}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  // Render the User Demographics Metrics Table
  const renderUserDemoTable = (data) => (
    <div className="metrics-table">
      <h2>User Demographic Metrics</h2>
      <table>
        <thead>
          <tr>
            <th>Age</th>
            <th>Gender</th>
            <th>Location</th>
            <th>Average Rating</th>
            <th>Total Ratings</th>
          </tr>
        </thead>
        <tbody>
          {data.map((user, index) => (
            <tr key={index}>
              <td>{user.age}</td>
              <td>{user.gender}</td>
              <td>{user.location}</td>
              <td>{user.average_rating}</td>
              <td>{user.total_ratings}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  return (
    <div className="App">
      <h1>Metrics Visualization</h1>
      {renderMovieTable(movieMetrics)}
      {renderGenreTable(genreMetrics)}
      {renderUserDemoTable(userDemoMetrics)}
    </div>
  );
};

export default App;*/

// src/App.js

import React from 'react';
import './App.css';
import MovieMetrics from './components/MovieMetrics';
import GenreMetrics from './components/GenreMetrics';
import DemographicsMetrics from './components/DemographicsMetrics';

const App = () => {
  return (
    <div className="App">
      <h1>Metrics Visualization</h1>
      <MovieMetrics />
      <GenreMetrics />
      <DemographicsMetrics />
    </div>
  );
};

export default App;
