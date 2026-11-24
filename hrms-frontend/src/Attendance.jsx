import { useState, useEffect } from "react";
import api from "./api";

function Attendance({ user }) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  async function fetchAttendance() {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/attendance", {
        params: { employeeId: user.employeeId },
      });
      setRecords(response.data);
    } catch (err) {
      setError("Failed to load attendance records");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchAttendance();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleCheckIn() {
    setActionError("");
    setActionLoading(true);
    try {
      await api.post("/attendance/check-in");
      fetchAttendance();
    } catch (err) {
      setActionError(err.response?.data?.message || "Check-in failed");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleCheckOut() {
    setActionError("");
    setActionLoading(true);
    try {
      await api.put("/attendance/check-out");
      fetchAttendance();
    } catch (err) {
      setActionError(err.response?.data?.message || "Check-out failed");
    } finally {
      setActionLoading(false);
    }
  }

  function formatTime(value) {
    if (!value) return "-";
    const date = new Date(value);
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  }

  function statusColor(status) {
    if (status === "PRESENT") return "#166534";
    if (status === "ABSENT") return "#b91c1c";
    return "#92400e";
  }

  function statusBg(status) {
    if (status === "PRESENT") return "#dcfce7";
    if (status === "ABSENT") return "#fee2e2";
    return "#fef3c7";
  }

  return (
    <div>
      {/* Check in / out actions */}
      <div
        style={{
          background: "#ffffff",
          borderRadius: "12px",
          padding: "24px",
          marginBottom: "24px",
          boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
          display: "flex",
          gap: "12px",
        }}
      >
        <button onClick={handleCheckIn} disabled={actionLoading} style={checkInButtonStyle}>
          {actionLoading ? "Please wait..." : "Check In"}
        </button>
        <button onClick={handleCheckOut} disabled={actionLoading} style={checkOutButtonStyle}>
          {actionLoading ? "Please wait..." : "Check Out"}
        </button>
      </div>

      {actionError && (
        <p style={{ color: "#b91c1c", marginBottom: "16px" }}>{actionError}</p>
      )}

      {/* Records table */}
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
              <th style={thStyle}>Date</th>
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
                <td colSpan={4} style={emptyCellStyle}>No attendance records found</td>
              </tr>
            ) : (
              records.map((r) => (
                <tr key={r.id} style={{ borderBottom: "1px solid #eee" }}>
                  <td style={tdStyle}>{r.attendanceDate}</td>
                  <td style={tdStyle}>{formatTime(r.checkInTime)}</td>
                  <td style={tdStyle}>{formatTime(r.checkOutTime)}</td>
                  <td style={tdStyle}>
                    <span
                      style={{
                        padding: "4px 10px",
                        borderRadius: "999px",
                        fontSize: "12px",
                        fontWeight: "600",
                        color: statusColor(r.status),
                        background: statusBg(r.status),
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

const checkInButtonStyle = {
  padding: "12px 24px",
  borderRadius: "8px",
  border: "none",
  background: "#166534",
  color: "#fff",
  fontSize: "14px",
  fontWeight: "600",
  cursor: "pointer",
};

const checkOutButtonStyle = {
  padding: "12px 24px",
  borderRadius: "8px",
  border: "none",
  background: "#0a1f44",
  color: "#f5f0e6",
  fontSize: "14px",
  fontWeight: "600",
  cursor: "pointer",
};

const thStyle = {
  padding: "14px 16px",
  textAlign: "left",
  color: "#ffffff",
  fontSize: "13px",
  fontWeight: "600",
};

const tdStyle = {
  padding: "12px 16px",
  fontSize: "14px",
  color: "#333",
};

const emptyCellStyle = {
  padding: "24px",
  textAlign: "center",
  color: "#888",
};

export default Attendance;