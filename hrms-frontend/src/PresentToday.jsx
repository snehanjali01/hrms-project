import { useState, useEffect } from "react";
import api from "./api";

function PresentToday() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchPresentToday() {
      setLoading(true);
      setError("");
      try {
        const response = await api.get("/attendance");
        const today = new Date().toISOString().split("T")[0];
        setRecords(
          response.data.filter(
            (a) => a.attendanceDate === today && a.status === "PRESENT"
          )
        );
      } catch (err) {
        setError("Failed to load today's attendance");
      } finally {
        setLoading(false);
      }
    }
    fetchPresentToday();
  }, []);

  function formatTime(value) {
    if (!value) return "-";
    return new Date(value).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  }

  return (
    <div>
      {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
      <div
        style={{
          background: "#ffffff",
          borderRadius: "12px",
          overflow: "hidden",
          boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
        }}
      >
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr style={{ background: "#0a1f44" }}>
              <th style={thStyle}>Employee</th>
              <th style={thStyle}>Check In</th>
              <th style={thStyle}>Check Out</th>
              <th style={thStyle}>Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} style={emptyCellStyle}>Loading...</td>
              </tr>
            ) : records.length === 0 ? (
              <tr>
                <td colSpan={4} style={emptyCellStyle}>No one has checked in yet today</td>
              </tr>
            ) : (
              records.map((r) => (
                <tr key={r.id} style={{ borderBottom: "1px solid #eee" }}>
                  <td style={tdStyle}>{r.employeeName || r.employeeId}</td>
                  <td style={tdStyle}>{formatTime(r.checkInTime)}</td>
                  <td style={tdStyle}>{formatTime(r.checkOutTime)}</td>
                  <td style={tdStyle}>
                    <span
                      style={{
                        padding: "4px 10px",
                        borderRadius: "999px",
                        fontSize: "12px",
                        fontWeight: "600",
                        color: "#166534",
                        background: "#dcfce7",
                      }}
                    >
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

const thStyle = { padding: "14px 16px", textAlign: "left", color: "#ffffff", fontSize: "13px", fontWeight: "600" };
const tdStyle = { padding: "12px 16px", fontSize: "14px", color: "#333" };
const emptyCellStyle = { padding: "24px", textAlign: "center", color: "#888" };

export default PresentToday;