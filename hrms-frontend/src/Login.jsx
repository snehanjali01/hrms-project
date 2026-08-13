import { useState } from "react";
import api from "./api";

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post("/auth/login", { username, password });
      onLoginSuccess(response.data);
    } catch (err) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError("Invalid username or password");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      style={{
        height: "100vh",
        width: "100%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "'Poppins', 'Segoe UI', sans-serif",
        background: "linear-gradient(135deg, #0a1f44 0%, #14315c 100%)",
      }}
    >
      <div
        style={{
          background: "#f5f0e6",
          borderRadius: "16px",
          padding: "48px 40px",
          width: "360px",
          boxShadow: "0 12px 32px rgba(0,0,0,0.35)",
        }}
      >
        <div style={{ textAlign: "center", marginBottom: "32px" }}>
          <div
            style={{
              width: "64px",
              height: "64px",
              borderRadius: "16px",
              background: "#0a1f44",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 16px",
            }}
          >
            <span style={{ fontSize: "26px", fontWeight: "700", color: "#f5f0e6" }}>
              H
            </span>
          </div>
          <h1 style={{ fontSize: "24px", fontWeight: "700", color: "#0a1f44", margin: 0 }}>
            Welcome back
          </h1>
          <p style={{ fontSize: "14px", color: "#555", marginTop: "6px" }}>
            Sign in to your HRMS account
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <label style={labelStyle}>Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={inputStyle}
            required
          />

          <label style={labelStyle}>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={inputStyle}
            required
          />

          {error && (
            <p style={{ color: "#b91c1c", fontSize: "13px", marginTop: "4px" }}>
              {error}
            </p>
          )}

          <button type="submit" disabled={loading} style={buttonStyle}>
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>
      </div>
    </div>
  );
}

const labelStyle = {
  display: "block",
  fontSize: "13px",
  fontWeight: "600",
  color: "#0a1f44",
  marginBottom: "6px",
  marginTop: "16px",
};

const inputStyle = {
  width: "100%",
  padding: "10px 12px",
  borderRadius: "8px",
  border: "1px solid #d6d0c4",
  fontSize: "14px",
  fontFamily: "inherit",
  boxSizing: "border-box",
  outline: "none",
};

const buttonStyle = {
  width: "100%",
  marginTop: "24px",
  padding: "12px",
  borderRadius: "8px",
  border: "none",
  background: "#0a1f44",
  color: "#f5f0e6",
  fontSize: "15px",
  fontWeight: "600",
  cursor: "pointer",
};

export default Login;