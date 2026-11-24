import { useState, useEffect } from "react";
import api from "./api";

function Profile({ user }) {
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchEmployee() {
      try {
        const response = await api.get(`/employees/${user.employeeId}`);
        setEmployee(response.data);
      } catch (err) {
        setError("Failed to load profile details");
      } finally {
        setLoading(false);
      }
    }
    fetchEmployee();
  }, [user.employeeId]);

  const initials = user?.employeeName
    ? user.employeeName
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
    : "?";

  return (
    <div
      style={{
        background: "#ffffff",
        borderRadius: "16px",
        padding: "40px",
        maxWidth: "500px",
        boxShadow: "0 2px 12px rgba(0,0,0,0.08)",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "20px",
          marginBottom: "32px",
        }}
      >
        <div
          style={{
            width: "72px",
            height: "72px",
            borderRadius: "50%",
            background: "#0a1f44",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flexShrink: 0,
          }}
        >
          <span style={{ fontSize: "24px", fontWeight: "700", color: "#f5f0e6" }}>
            {initials}
          </span>
        </div>
        <div>
          <h2 style={{ fontSize: "22px", fontWeight: "700", color: "#0a1f44", margin: 0 }}>
            {user.employeeName}
          </h2>
          <p style={{ fontSize: "14px", color: "#666", margin: "4px 0 0" }}>
            @{user.username}
          </p>
        </div>
      </div>

      {user.roles && user.roles.length > 0 && (
        <div style={{ marginBottom: "24px", display: "flex", gap: "8px" }}>
          {user.roles.map((role) => (
            <span
              key={role}
              style={{
                padding: "4px 12px",
                borderRadius: "999px",
                fontSize: "12px",
                fontWeight: "600",
                color: "#0a1f44",
                background: "#e8e2d4",
              }}
            >
              {role}
            </span>
          ))}
        </div>
      )}

      {loading ? (
        <p style={{ color: "#888" }}>Loading details...</p>
      ) : error ? (
        <p style={{ color: "#b91c1c" }}>{error}</p>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
          <ProfileRow label="Employee Code" value={employee.employeeCode} />
          <ProfileRow label="Email" value={employee.email} />
          <ProfileRow label="Department" value={employee.departmentName} />
          <ProfileRow label="Company" value={employee.companyName} />
        </div>
      )}
    </div>
  );
}

function ProfileRow({ label, value }) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        paddingBottom: "12px",
        borderBottom: "1px solid #eee",
      }}
    >
      <span style={{ fontSize: "13px", color: "#888", fontWeight: "600" }}>
        {label}
      </span>
      <span style={{ fontSize: "14px", color: "#333" }}>{value || "-"}</span>
    </div>
  );
}

export default Profile;