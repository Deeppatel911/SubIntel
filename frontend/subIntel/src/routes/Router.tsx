import { Routes, Route } from "react-router-dom";
import { Register } from "../pages/register";
import { Login } from "../pages/login";

export default function Router() {
  return (
    <Routes>
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<Login />} />
    </Routes>
  );
}
