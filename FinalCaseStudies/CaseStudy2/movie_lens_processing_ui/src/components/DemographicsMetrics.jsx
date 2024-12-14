/*import React, { useEffect, useState } from "react";
import { Pie } from "react-chartjs-2";
import { CircularProgress } from "@mui/material";

const DemographicsChart = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch demographics data from API
    fetch("/api/demographics-metrics")
      .then((response) => response.json())
      .then((demographics) => {
        setData({
          labels: demographics.map((group) => group.demographic),
          datasets: [
            {
              label: "Average Rating",
              data: demographics.map((group) => group.averageRating),
              backgroundColor: [
                "rgba(255, 99, 132, 0.6)",
                "rgba(54, 162, 235, 0.6)",
                "rgba(255, 206, 86, 0.6)",
                "rgba(75, 192, 192, 0.6)",
                "rgba(153, 102, 255, 0.6)",
              ],
            },
          ],
        });
        setLoading(false);
      })
      .catch((error) => console.error("Error fetching demographics:", error));
  }, []);

  if (loading) return <CircularProgress />;

  return <Pie data={data} />;
};

export default DemographicsChart;*/

// src/components/DemographicsMetrics.js
import React, { useEffect, useState } from 'react';
import axios from 'axios';

const DemographicsMetrics = () => {
  const [userDemoMetrics, setUserDemoMetrics] = useState([]);

  useEffect(() => {
    const fetchUserDemoMetrics = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/demographics-metrics');
        setUserDemoMetrics(response.data);
      } catch (error) {
        console.error('Error fetching user demographic metrics', error);
      }
    };

    fetchUserDemoMetrics();
  }, []);

  const renderUserDemoTable = (data) => (
    <div className="metrics-table">
      <h2>Ratings By User Demographics</h2>
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
    <div>
      {userDemoMetrics && userDemoMetrics.length > 0
        ? renderUserDemoTable(userDemoMetrics)
        : <p>Loading user demographics metrics...</p>}
    </div>
  );
};

export default DemographicsMetrics;

