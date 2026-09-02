import { useState, useEffect } from "react";
import leaveApi from "./leaveApi";

function LeaveRequests({ user }) {
  const [myRequests, setMyRequests] = useState([]);
  const [pendingApprovals, setPendingApprovals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [reason, setReason] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function fetchAll() {
    setLoading(true);
    setError("");
    try {
      const myReqRes = await leaveApi.get("/leave-requests", {
        params: { employeeId: user.employeeId },
      });
      setMyRequests(myReqRes.data);

      // Requests waiting on this user, if they manage anyone
      try {
        const pendingRes = await leaveApi.get("/leave-requests/pending", {
          params: { managerId: user.employeeId },
        });
        setPendingApprovals(pendingRes.data);
      } catch {
        setPendingApprovals([]);
      }
    } catch (err) {
      setError("Failed to load leave requests");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError("");
    setSubmitting(true);
    try {
      await leaveApi.post("/leave-requests", {
        employeeId: user.employeeId,
        startDate,
        endDate,
        reason,
      });
      setStartDate("");
      setEndDate("");
      setReason("");
      fetchAll();
    } catch (err) {
      const msg =
        err.response?.data?.message || "Failed to submit leave request";
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDecision(id, decision) {
    try {
      await leaveApi.put(`/leave-requests/${id}/decision`, {
        approverId: user.employeeId,
        decision,
      });
      fetchAll();
    } catch (err) {
      alert(err.response?.data?.message || "Failed to update decision");
    }
  }

  function statusColor(status) {
    if (status === "APPROVED") return "#166534";
    if (status === "REJECTED") return "#b91c1c";
    return "#92400e";
  }

  function statusBg(status) {
    if (status === "APPROVED") return "#dcfce7";
    if (status === "REJECTED") return "#fee2e2";
    return "#fef3c7";
  }

  function renderTable(rows, showActions) {
    return (
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
              <th style={thStyle}>Approved By</th>
              {showActions && <th style={thStyle}>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={showActions ? 7 : 6} style={emptyCellStyle}>
                  Loading...
                </td>
              </tr>
            ) : rows.length === 0 ? (
              <tr>
                <td colSpan={showActions ? 7 : 6} style={emptyCellStyle}>
                  No requests found
                </td>
              </tr>
            ) : (
              rows.map((lr) => (
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
                        color: statusColor(lr.status),
                        background: statusBg(lr.status),
                      }}
                    >
                      {lr.status}
                    </span>
                  </td>
                  <td style={tdStyle}>{lr.approvedByName || "-"}</td>
                  {showActions && (
                    <td style={tdStyle}>
                      {lr.status === "PENDING" && (
                        <div style={{ display: "flex", gap: "8px" }}>
                          <button
                            onClick={() => handleDecision(lr.id, "APPROVED")}
                            style={approveButtonStyle}
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleDecision(lr.id, "REJECTED")}
                            style={rejectButtonStyle}
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div>
      {/* Submit new leave request */}
      <form
        onSubmit={handleSubmit}
        style={{
          background: "#ffffff",
          borderRadius: "12px",
          padding: "24px",
          marginBottom: "24px",
          boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
          display: "flex",
          gap: "12px",
          flexWrap: "wrap",
          alignItems: "flex-end",
        }}
      >
        <div>
          <label style={labelStyle}>Start Date</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={inputStyle}
            required
          />
        </div>
        <div>
          <label style={labelStyle}>End Date</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={inputStyle}
            required
          />
        </div>
        <div style={{ flex: 1, minWidth: "200px" }}>
          <label style={labelStyle}>Reason</label>
          <input
            type="text"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            style={{ ...inputStyle, width: "100%" }}
            placeholder="e.g. Family function"
          />
        </div>
        <button type="submit" disabled={submitting} style={submitButtonStyle}>
          {submitting ? "Submitting..." : "Submit Request"}
        </button>
      </form>

      {submitError && (
        <p style={{ color: "#b91c1c", marginBottom: "16px" }}>{submitError}</p>
      )}

      {error && <p style={{ color: "#b91c1c" }}>{error}</p>}

      {/* Requests awaiting approval — only shown if there are any */}
      {pendingApprovals.length > 0 && (
        <>
          <h3 style={sectionHeaderStyle}>Requests Awaiting My Approval</h3>
          {renderTable(pendingApprovals, true)}
          <div style={{ marginBottom: "32px" }} />
        </>
      )}

      {/* My own requests */}
      <h3 style={sectionHeaderStyle}>My Leave Requests</h3>
      {renderTable(myRequests, true)}
    </div>
  );
}

const sectionHeaderStyle = {
  fontSize: "16px",
  fontWeight: "700",
  color: "#0a1f44",
  marginBottom: "12px",
};

const labelStyle = {
  display: "block",
  fontSize: "12px",
  fontWeight: "600",
  color: "#0a1f44",
  marginBottom: "4px",
};

const inputStyle = {
  padding: "8px 12px",
  borderRadius: "8px",
  border: "1px solid #d6d0c4",
  fontSize: "14px",
  fontFamily: "inherit",
};

const submitButtonStyle = {
  padding: "10px 20px",
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

const approveButtonStyle = {
  padding: "6px 12px",
  borderRadius: "6px",
  border: "none",
  background: "#166534",
  color: "#fff",
  fontSize: "12px",
  fontWeight: "600",
  cursor: "pointer",
};

const rejectButtonStyle = {
  padding: "6px 12px",
  borderRadius: "6px",
  border: "none",
  background: "#b91c1c",
  color: "#fff",
  fontSize: "12px",
  fontWeight: "600",
  cursor: "pointer",
};

export default LeaveRequests;