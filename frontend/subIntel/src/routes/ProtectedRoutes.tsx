import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoutes=() =>{
  const token = localStorage.getItem("jwtToken");

  return token ? <Outlet /> : <Navigate to="/login" replace/>;
}

export default ProtectedRoutes;