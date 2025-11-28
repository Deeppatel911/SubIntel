import { useState, useEffect } from "react";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";
import { Box, Typography } from "@mui/material";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface TrendData {
  month: string;
  totalAmount: number;
}

const SpendingLineChart = () => {
  const [trendData, setTrendData] = useState<TrendData[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTrendData = async () => {
      setIsLoading(true);
      try {
        const jwtToken = localStorage.getItem("jwtToken");
        const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
        const response = await fetch(`${apiUrl}/api/transactions/trends`, {
          headers: {
            Authorization: `Bearer ${jwtToken}`,
          },
        });

        if (response.ok) {
          const data: TrendData[] = await response.json();
          setTrendData(data);
        } else {
          console.error("Failed to fetch trend data");
        }
      } catch (error) {
        console.error("Error fetching trend data:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchTrendData();
  }, []);

  const dataForChart = {
    labels: trendData.map((d) => d.month),
    datasets: [
      {
        label: "Total Spending ($)",
        data: trendData.map((d) => Math.abs(d.totalAmount)),
        fill: false,
        borderColor: "rgb(75, 192, 192)",
        tension: 0.1,
      },
    ],
  };

  if (isLoading) return <p>Loading chart data...</p>;
  if (trendData.length === 0) return <p>No spending data available.</p>;

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom>
        Monthly Spending Trend
      </Typography>
      <Line data={dataForChart} />
    </Box>
  );
};

export default SpendingLineChart;
