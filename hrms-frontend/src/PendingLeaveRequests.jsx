import { useState, useEffect } from "react";
import api from "./api";

function PendingLeaveRequests({ user }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function fetchPending() {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/leave-requests/pending");
      setRequests(response.data.filter((lr) => lr.status === "PENDING"));
    } catch (err) {
      setError("Failed to load leave requests");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchPending();
  }, []);

  async function handleDecision(id, decision) {
    try {
      await api.put(`/leave-requests/${id}/decision`, {
        decision,
      });
      fetchPending();
    } catch (err) {
      alert(err.response?.data?.message || "Failed to update decision");
    }
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
              <th style={thStyle}>Start</th>
              <th style={thStyle}>End</th>
              <th style={thStyle}>Reason</th>
              <th style={thStyle}>Status</th>
              <th style={thStyle}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} style={emptyCellStyle}>Loading...</td>
              </tr>
            ) : requests.length === 0 ? (
              <tr>
                <td colSpan={6} style={emptyCellStyle}>No pending leave requests</td>
              </tr>
            ) : (
              requests.map((lr) => (
                <tr key={lr.id} style={{ borderBottom: "1px solid #eee" }}>
                  <td style={tdStyle}>{lr.employeeName}</td>
                  <td style={tdStyle}>{lr.startDate}</td>
                  <td style={tdStyle}>{lr.endDate}</td>
                  <td style={tdStyle}>{lr.reason}</td>
                  <td style={tdStyle}>
                    <span
                      style={{
                        padding: "4px 10px",
                        borderRadius: "999px",
                        fontSize: "12px",
                        fontWeight: "600",
                        color: "#92400e",
                        background: "#fef3c7",
                      }}
                    >
                      {lr.status}
                    </span>
                  </td>
                  <td style={tdStyle}>
                    <div style={{ display: "flex", gap: "8px" }}>
                      <button onClick={() => handleDecision(lr.id, "APPROVED")} style={approveButtonStyle}>
                        Approve
                      </button>
                      <button onClick={() => handleDecision(lr.id, "REJECTED")} style={rejectButtonStyle}>
                        Reject
                      </button>
                    </div>
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
const approveButtonStyle = { padding: "6px 12px", borderRadius: "6px", border: "none", background: "#166534", color: "#fff", fontSize: "12px", fontWeight: "600", cursor: "pointer" };
const rejectButtonStyle = { padding: "6px 12px", borderRadius: "6px", border: "none", background: "#b91c1c", color: "#fff", fontSize: "12px", fontWeight: "600", cursor: "pointer" };

export default PendingLeaveRequests;