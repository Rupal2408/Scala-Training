/*import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import { CircularProgress } from "@mui/material";

const GenreChart = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch genres from API
    fetch("/api/genre-metrics")
      .then((response) => response.json())
      .then((genres) => {
        setData({
          labels: genres.map((genre) => genre.name),
          datasets: [
            {
              label: "Number of Movies",
              data: genres.map((genre) => genre.count),
              backgroundColor: "rgba(75, 192, 192, 0.6)",
            },
          ],
        });
        setLoading(false);
      })
      .catch((error) => console.error("Error fetching genres:", error));
  }, []);

  if (loading) return <CircularProgress />;

  return <Bar data={data} />;
};

export default GenreChart;*/


// src/components/GenreMetrics.js
import React, { useEffect, useState } from 'react';
import axios from 'axios';

const GenreMetrics = () => {
  const [genreMetrics, setGenreMetrics] = useState([]);

  useEffect(() => {
    const fetchGenreMetrics = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/genre-metrics');
        setGenreMetrics(response.data);
      } catch (error) {
        console.error('Error fetching genre metrics', error);
      }
    };

    fetchGenreMetrics();
  }, []);

  const renderGenreTable = (data) => (
    <div className="metrics-table">
      <h2>Most Popular Genres</h2>
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

  return (
    <div>
      {genreMetrics && genreMetrics.length > 0
        ? renderGenreTable(genreMetrics)
        : <p>Loading genre metrics...</p>}
    </div>
  );
};

export default GenreMetrics;
