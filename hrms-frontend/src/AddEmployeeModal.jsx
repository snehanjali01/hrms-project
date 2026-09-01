import { useState } from "react";
import api from "./api";

function AddEmployeeModal({ onClose, onCreated }) {
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    employeeCode: "",
    companyId: "",
    departmentId: "",
    managerId: "",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      const response = await api.post("/employees", {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        employeeCode: form.employeeCode,
        companyId: Number(form.companyId),
        departmentId: Number(form.departmentId),
        managerId: form.managerId ? Number(form.managerId) : null,
        createUserAccount: false,
      });
      if (onCreated) onCreated(response.data);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create employee");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div
      onClick={onClose}
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: "rgba(10, 31, 68, 0.55)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: "#ffffff",
          borderRadius: "18px",
          width: "400px",
          maxWidth: "90vw",
          maxHeight: "85vh",
          overflowY: "auto",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
          padding: "28px",
        }}
      >
        <h2 style={{ fontSize: "20px", fontWeight: "700", color: "#0a1f44", marginTop: 0, marginBottom: "20px" }}>
          Add Employee
        </h2>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
          <Field label="First Name" value={form.firstName} onChange={(v) => update("firstName", v)} required />
          <Field label="Last Name" value={form.lastName} onChange={(v) => update("lastName", v)} required />
          <Field label="Email" value={form.email} onChange={(v) => update("email", v)} type="email" required />
          <Field label="Employee Code" value={form.employeeCode} onChange={(v) => update("employeeCode", v)} required />
          <Field label="Company ID" value={form.companyId} onChange={(v) => update("companyId", v)} type="number" required />
          <Field label="Department ID" value={form.departmentId} onChange={(v) => update("departmentId", v)} type="number" required />
          <Field label="Manager ID (optional)" value={form.managerId} onChange={(v) => update("managerId", v)} type="number" />

          {error && <p style={{ color: "#b91c1c", fontSize: "13px", margin: 0 }}>{error}</p>}

          <div style={{ display: "flex", gap: "8px", marginTop: "8px" }}>
            <button type="submit" disabled={saving} style={submitButtonStyle}>
              {saving ? "Creating..." : "Create Employee"}
            </button>
            <button type="button" onClick={onClose} disabled={saving} style={cancelButtonStyle}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({ label, value, onChange, type = "text", required }) {
  return (
    <div>
      <label style={{ display: "block", fontSize: "12px", fontWeight: "600", color: "#0a1f44", marginBottom: "4px" }}>
        {label}
      </label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        style={{
          width: "100%",
          padding: "9px 12px",
          borderRadius: "8px",
          border: "1px solid #d6d0c4",
          fontSize: "14px",
          fontFamily: "inherit",
          boxSizing: "border-box",
        }}
      />
    </div>
  );
}

const submitButtonStyle = {
  flex: 1,
  padding: "10px",
  borderRadius: "8px",
  border: "none",
  background: "#0a1f44",
  color: "#f5f0e6",
  fontSize: "14px",
  fontWeight: "600",
  cursor: "pointer",
};

const cancelButtonStyle = {
  flex: 1,
  padding: "10px",
  borderRadius: "8px",
  border: "1px solid #ccc",
  background: "transparent",
  color: "#666",
  fontSize: "14px",
  cursor: "pointer",
};

export default AddEmployeeModal;