import { useEffect } from "react";

function Splash({ onFinish }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onFinish();
    }, 5500); // shows for 5.5 seconds

    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div
      style={{
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "'Poppins', 'Segoe UI', sans-serif",
        background: "linear-gradient(135deg, #0a1f44 0%, #14315c 100%)",
        color: "#ffffff",
      }}
    >
      <div
        style={{
          width: "90px",
          height: "90px",
          borderRadius: "20px",
          background: "#f5f0e6",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          marginBottom: "24px",
          boxShadow: "0 8px 24px rgba(0,0,0,0.35)",
        }}
      >
        <span style={{ fontSize: "36px", fontWeight: "700", color: "#0a1f44" }}>H</span>
      </div>

      <h1
        style={{
          fontSize: "52px",
          fontWeight: "700",
          letterSpacing: "4px",
          marginBottom: "10px",
        }}
      >
        HRMS
      </h1>
      <p
        style={{
          fontSize: "16px",
          letterSpacing: "1px",
          opacity: 0.75,
          color: "#f5f0e6",
        }}
      >
        Human Resource Management System
      </p>
    </div>
  );
}

export default Splash;