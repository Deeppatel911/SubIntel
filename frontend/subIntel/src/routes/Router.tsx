import { Routes, Route } from "react-router-dom";
import { Register } from "../pages/register";
import { Login } from "../pages/login";
import { Dashboard } from "../pages/dashboard";
import ProtectedRoutes from "./ProtectedRoutes";
import GuestRoutes from "./GuestRoutes";

export default function Router() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<GuestRoutes />}>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
      </Route>

      {/* Protected Routes */}
      <Route element={<ProtectedRoutes />}>
        <Route path="/dashboard" element={<Dashboard />} />
      </Route>
    </Routes>
  );
}
