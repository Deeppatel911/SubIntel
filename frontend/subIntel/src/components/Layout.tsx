import {
  AppBar,
  Button,
  CssBaseline,
  Toolbar,
  Typography,
} from "@mui/material";
import { Box } from "@mui/system";
import LogoutIcon from "@mui/icons-material/Logout";
import { useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

export const Layout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    setIsAuthenticated(!!token);
  }, [location]);

  const handleLogout = () => {
    localStorage.removeItem("jwtToken");
    navigate("/login");
  };

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        minHeight: "100vh",
        bgcolor: "#282c34",
      }}
    >
      <CssBaseline />
      <AppBar
        position="sticky"
        elevation={0}
        sx={{
          backgroundColor: "rgba(40, 44, 52, 0.7)",
          backdropFilter: "blur(10px)",
          borderBottom: "1px solid rgba(255,255,255,0.1)",
          zIndex: (theme) => theme.zIndex.drawer + 1,
        }}
      >
        <Toolbar>
          <Typography
            variant="h5"
            component="div"
            sx={{
              flexGrow: 1,
              fontWeight: "bold",
              letterSpacing: 1,
              color: "white",
            }}
          >
            SubIntel
          </Typography>

          {isAuthenticated && (
            <Button
              color="primary"
              variant="outlined"
              onClick={handleLogout}
              startIcon={<LogoutIcon />}
              sx={{ borderColor: "rgba(144, 202, 249, 0.5)" }}
            >
              Logout
            </Button>
          )}
        </Toolbar>
      </AppBar>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: "100%",
          p: 0,
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};
