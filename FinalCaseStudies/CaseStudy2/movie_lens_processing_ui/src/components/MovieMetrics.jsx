/*import React, { useEffect, useState } from "react";
import { List, ListItem, ListItemText, CircularProgress } from "@mui/material";

const MovieList = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch top-rated movies from API
    fetch("/api/movie-metrics")
      .then((response) => response.json())
      .then((data) => {
        setMovies(data);
        setLoading(false);
      })
      .catch((error) => console.error("Error fetching movies:", error));
  }, []);

  if (loading) return <CircularProgress />;

  return (
    <List>
      {movies.map((movie) => (
        <ListItem key={movie.movieId}>
          <ListItemText
            primary={movie.title}
            secondary={`Rating: ${movie.total_ratings}`}
          />
        </ListItem>
      ))}
    </List>
  );
};

export default MovieList;*/

// src/components/MovieMetrics.js
import React, { useEffect, useState } from 'react';
import axios from 'axios';

const MovieMetrics = () => {
  const [movieMetrics, setMovieMetrics] = useState([]);

  useEffect(() => {
    const fetchMovieMetrics = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/movie-metrics');
        setMovieMetrics(response.data);
      } catch (error) {
        console.error('Error fetching movie metrics', error);
      }
    };

    fetchMovieMetrics();
  }, []);

  const renderMovieTable = (data) => (
    <div className="metrics-table">
      <h2>Top Rated Movies</h2>
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

  return (
    <div>
      {movieMetrics && movieMetrics.length > 0
        ? renderMovieTable(movieMetrics)
        : <p>Loading movie metrics...</p>}
    </div>
  );
};

export default MovieMetrics;
