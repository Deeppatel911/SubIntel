import React, { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Alert,
} from "@mui/material";

export const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    setMessage(null);

    if (!token) {
      setError("Invalid or missing reset token.");
      setIsSubmitting(false);
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      setIsSubmitting(false);
      return;
    }

    try {
      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/reset-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token: token, newPassword: password }),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(
          data.message || "Password reset successfully! Redirecting to login..."
        );
        setTimeout(() => navigate("/login"), 3000);
      } else {
        setError(data.message || "An error occurred.");
      }
    } catch (err) {
      console.log(err);
      setError("An error occurred. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!token) {
    return (
      <Container maxWidth="xs">
        <Alert severity="error" sx={{ mt: 8 }}>
          Invalid or missing password reset token. Please request a new link.
        </Alert>
      </Container>
    );
  }

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Typography component="h1" variant="h5">
          Reset Your Password
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="New Password"
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isSubmitting || !!message}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="confirmPassword"
            label="Confirm New Password"
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isSubmitting || !!message}
          />

          {message && (
            <Alert severity="success" sx={{ mt: 2, width: "100%" }}>
              {message}
            </Alert>
          )}
          {error && (
            <Alert severity="error" sx={{ mt: 2, width: "100%" }}>
              {error}
            </Alert>
          )}

          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            disabled={isSubmitting || !!message}
          >
            {isSubmitting ? "Saving..." : "Reset Password"}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};
