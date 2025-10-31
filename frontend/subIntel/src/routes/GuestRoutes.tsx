import { Navigate, Outlet } from "react-router-dom";

const GuestRoutes = () => {
  const token = localStorage.getItem("jwtToken");

  return token ? <Navigate to="/dashboard" replace /> : <Outlet />;
};

export default GuestRoutes;
