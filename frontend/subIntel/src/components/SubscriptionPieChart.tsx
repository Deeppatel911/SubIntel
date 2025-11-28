import { useState, useEffect } from "react";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { Pie } from "react-chartjs-2";

ChartJS.register(ArcElement, Tooltip, Legend);

interface SpendingData {
  [key: string]: number;
}

const SubscriptionPieChart = () => {
  const [chartData, setChartData] = useState<SpendingData | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchSpendingData = async () => {
      setIsLoading(true);
      try {
        const jwtToken = localStorage.getItem("jwtToken");
        const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
        const response = await fetch(
          `${apiUrl}/api/subscriptions/spending-summary`,
          {
            headers: { Authorization: `Bearer ${jwtToken}` },
          }
        );
        if (!response.ok) {
          console.error("Failed to fetch spending data");
          setChartData(null);
        } else {
          const data = await response.json();
          console.log("Spending data received:", data);
          setChartData(data);
        }
      } catch (error) {
        console.error("Error fetching spending data:", error);
        setChartData(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchSpendingData();
  }, []);

  const dataForChart = {
    labels: chartData ? Object.keys(chartData) : [],
    datasets: [
      {
        label: "Spending ($)",
        data: chartData ? Object.values(chartData) : [],
        backgroundColor: [
          // Add more colors if needed
          "rgba(255, 99, 132, 0.7)",
          "rgba(54, 162, 235, 0.7)",
          "rgba(255, 206, 86, 0.7)",
          "rgba(75, 192, 192, 0.7)",
          "rgba(153, 102, 255, 0.7)",
          "rgba(255, 159, 64, 0.7)",
        ],
        borderColor: [
          "rgba(255, 99, 132, 1)",
          "rgba(54, 162, 235, 1)",
          "rgba(255, 206, 86, 1)",
          "rgba(75, 192, 192, 1)",
          "rgba(153, 102, 255, 1)",
          "rgba(255, 159, 64, 1)",
        ],
        borderWidth: 1,
      },
    ],
  };

  if (isLoading) {
    return <p>Loading chart data...</p>;
  }

  if (!chartData || Object.keys(chartData).length === 0) {
    return <p>No spending data available to display chart.</p>;
  }

  return (
    <div style={{ maxWidth: "100%", margin: "20px auto" }}>
      <Pie data={dataForChart} />
    </div>
  );
};

export default SubscriptionPieChart;
