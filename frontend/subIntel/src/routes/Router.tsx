import { Routes, Route } from "react-router-dom";
import ProtectedRoutes from "./ProtectedRoutes";
import GuestRoutes from "./GuestRoutes";
import React, { Suspense } from "react";
import CircularProgress from "@mui/material/CircularProgress";
import { Box } from "@mui/material";

const Dashboard = React.lazy(() =>
  import("../pages/dashboard/Dashboard").then((module) => ({ default: module.Dashboard }))
);
const Register = React.lazy(() =>
  import("../pages/auth/Register").then((module) => ({ default: module.Register }))
);
const Login = React.lazy(() =>
  import("../pages/auth/Login").then((module) => ({ default: module.Login }))
);
const ForgotPassword = React.lazy(() =>
  import("../pages/auth/ForgotPassword").then((module) => ({
    default: module.ForgotPassword,
  }))
);
const ResetPassword = React.lazy(() =>
  import("../pages/auth/ResetPassword").then((module) => ({
    default: module.ResetPassword,
  }))
);

const LoadingScreen = () => (
  <Box
    sx={{
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      height: "100vh",
      bgcolor: "#282c34",
    }}
  >
    <CircularProgress />
  </Box>
);

export default function Router() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        {/* Public Routes */}
        <Route element={<GuestRoutes />}>
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
        </Route>

        {/* Protected Routes */}
        <Route element={<ProtectedRoutes />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/" element={<Dashboard />} />
        </Route>
      </Routes>
    </Suspense>
  );
}
