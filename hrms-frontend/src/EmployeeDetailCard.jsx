import { useState } from "react";
import api from "./api";

function EmployeeDetailCard({ employee, onClose, onUpdated, onDeleted }) {
  const [editingSalary, setEditingSalary] = useState(false);
  const [salaryInput, setSalaryInput] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");

  const [editingDetails, setEditingDetails] = useState(false);
  const [form, setForm] = useState(null);
  const [detailsSaving, setDetailsSaving] = useState(false);
  const [detailsError, setDetailsError] = useState("");

  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  if (!employee) return null;

  const initials = `${employee.firstName?.[0] || ""}${employee.lastName?.[0] || ""}`.toUpperCase();

  function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  function formatCurrency(value) {
    if (value === null || value === undefined) return "-";
    return `₹${Number(value).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  function startEditingSalary() {
    setSalaryInput(employee.salary ?? "");
    setSaveError("");
    setEditingSalary(true);
  }

  async function handleSaveSalary() {
    setSaveError("");
    if (salaryInput === "" || isNaN(Number(salaryInput)) || Number(salaryInput) < 0) {
      setSaveError("Enter a valid salary amount");
      return;
    }
    setSaving(true);
    try {
      const response = await api.patch(`/employees/${employee.id}/salary`, {
        salary: Number(salaryInput),
      });
      setEditingSalary(false);
      if (onUpdated) onUpdated(response.data);
    } catch (err) {
      setSaveError(err.response?.data?.message || "Failed to update salary");
    } finally {
      setSaving(false);
    }
  }

  function startEditingDetails() {
    setForm({
      firstName: employee.firstName || "",
      lastName: employee.lastName || "",
      email: employee.email || "",
      employeeCode: employee.employeeCode || "",
      companyId: employee.companyId || "",
      departmentId: employee.departmentId || "",
      managerId: employee.managerId || "",
    });
    setDetailsError("");
    setEditingDetails(true);
  }

  function updateForm(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSaveDetails() {
    setDetailsError("");
    setDetailsSaving(true);
    try {
      const response = await api.put(`/employees/${employee.id}`, {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        employeeCode: form.employeeCode,
        companyId: Number(form.companyId),
        departmentId: Number(form.departmentId),
        managerId: form.managerId ? Number(form.managerId) : null,
      });
      setEditingDetails(false);
      if (onUpdated) onUpdated(response.data);
    } catch (err) {
      setDetailsError(err.response?.data?.message || "Failed to update employee");
    } finally {
      setDetailsSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete ${employee.firstName} ${employee.lastName}? This cannot be undone.`)) {
      return;
    }
    setDeleteError("");
    setDeleting(true);
    try {
      await api.delete(`/employees/${employee.id}`);
      if (onDeleted) onDeleted(employee.id);
      onClose();
    } catch (err) {
      setDeleteError(
        err.response?.data?.message ||
          "Failed to delete employee. They may have related records (attendance, leave, payroll)."
      );
    } finally {
      setDeleting(false);
    }
  }

  const hasAddress = employee.street || employee.city || employee.state;

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
          borderRadius: "22px",
          width: "380px",
          maxWidth: "90vw",
          maxHeight: "85vh",
          overflowY: "auto",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
          padding: "0",
        }}
      >
        {/* Header */}
        <div
          style={{
            background: "linear-gradient(135deg, #0a1f44 0%, #14315c 100%)",
            borderRadius: "22px 22px 0 0",
            padding: "32px 28px 24px",
            textAlign: "center",
            position: "relative",
          }}
        >
          <button
            onClick={onClose}
            style={{
              position: "absolute",
              top: "16px",
              right: "16px",
              background: "rgba(255,255,255,0.15)",
              border: "none",
              borderRadius: "50%",
              width: "28px",
              height: "28px",
              color: "#fff",
              fontSize: "16px",
              cursor: "pointer",
              lineHeight: 1,
            }}
          >
            ×
          </button>
          <div
            style={{
              width: "72px",
              height: "72px",
              borderRadius: "50%",
              background: "#f5f0e6",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 14px",
            }}
          >
            <span style={{ fontSize: "24px", fontWeight: "700", color: "#0a1f44" }}>
              {initials}
            </span>
          </div>
          <h2 style={{ color: "#fff", fontSize: "20px", fontWeight: "700", margin: 0 }}>
            {employee.firstName} {employee.lastName}
          </h2>
          <p style={{ color: "rgba(245,240,230,0.75)", fontSize: "13px", marginTop: "4px" }}>
            {employee.departmentName} · {employee.employeeCode}
          </p>
        </div>

        {/* Details */}
        <div style={{ padding: "24px 28px 28px" }}>
          {/* Edit / Delete action buttons */}
          {!editingDetails && (
            <div style={{ display: "flex", gap: "8px", marginBottom: "20px" }}>
              <button onClick={startEditingDetails} style={editButtonStyle}>
                Edit Details
              </button>
              <button onClick={handleDelete} disabled={deleting} style={deleteButtonStyle}>
                {deleting ? "Deleting..." : "Delete"}
              </button>
            </div>
          )}
          {deleteError && (
            <p style={{ color: "#b91c1c", fontSize: "12px", marginBottom: "16px" }}>
              {deleteError}
            </p>
          )}

          {editingDetails ? (
            <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
              <EditField label="First Name" value={form.firstName} onChange={(v) => updateForm("firstName", v)} />
              <EditField label="Last Name" value={form.lastName} onChange={(v) => updateForm("lastName", v)} />
              <EditField label="Email" value={form.email} onChange={(v) => updateForm("email", v)} />
              <EditField label="Employee Code" value={form.employeeCode} onChange={(v) => updateForm("employeeCode", v)} />
              <EditField label="Company ID" value={form.companyId} onChange={(v) => updateForm("companyId", v)} type="number" />
              <EditField label="Department ID" value={form.departmentId} onChange={(v) => updateForm("departmentId", v)} type="number" />
              <EditField label="Manager ID (optional)" value={form.managerId} onChange={(v) => updateForm("managerId", v)} type="number" />

              {detailsError && (
                <p style={{ color: "#b91c1c", fontSize: "12px", margin: 0 }}>{detailsError}</p>
              )}

              <div style={{ display: "flex", gap: "8px", marginTop: "4px" }}>
                <button onClick={handleSaveDetails} disabled={detailsSaving} style={saveButtonStyle}>
                  {detailsSaving ? "Saving..." : "Save Changes"}
                </button>
                <button onClick={() => setEditingDetails(false)} disabled={detailsSaving} style={cancelButtonStyle}>
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <>
              <DetailRow label="Email" value={employee.email} />
              <DetailRow label="Employee Code" value={employee.employeeCode} />
              <DetailRow label="Company" value={employee.companyName} />
              <DetailRow label="Department" value={employee.departmentName} />
              <DetailRow label="Manager" value={employee.managerName || "—"} />

              {/* Salary row — editable */}
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  gap: "16px",
                  padding: "10px 0",
                  borderBottom: "1px solid #f0ede4",
                }}
              >
                <span
                  style={{
                    fontSize: "12px",
                    fontWeight: "700",
                    color: "#999",
                    textTransform: "uppercase",
                    letterSpacing: "0.4px",
                    whiteSpace: "nowrap",
                  }}
                >
                  Salary
                </span>

                {editingSalary ? (
                  <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
                    <input
                      type="number"
                      value={salaryInput}
                      onChange={(e) => setSalaryInput(e.target.value)}
                      autoFocus
                      style={{
                        width: "110px",
                        padding: "6px 8px",
                        borderRadius: "6px",
                        border: "1px solid #d6d0c4",
                        fontSize: "14px",
                        fontFamily: "inherit",
                      }}
                    />
                    <button onClick={handleSaveSalary} disabled={saving} style={smallSaveStyle}>
                      {saving ? "..." : "Save"}
                    </button>
                    <button onClick={() => setEditingSalary(false)} disabled={saving} style={smallCancelStyle}>
                      Cancel
                    </button>
                  </div>
                ) : (
                  <span
                    onClick={startEditingSalary}
                    style={{
                      fontSize: "14px",
                      color: "#0a1f44",
                      fontWeight: "600",
                      cursor: "pointer",
                      textDecoration: "underline",
                      textDecorationStyle: "dotted",
                    }}
                    title="Click to edit"
                  >
                    {formatCurrency(employee.salary)} ✎
                  </span>
                )}
              </div>
              {saveError && (
                <p style={{ color: "#b91c1c", fontSize: "12px", marginTop: "4px", marginBottom: 0 }}>
                  {saveError}
                </p>
              )}

              <DetailRow label="Joined" value={formatDate(employee.joiningDate)} />
              {hasAddress && (
                <DetailRow
                  label="Address"
                  value={[employee.street, employee.city, employee.state, employee.postalCode, employee.country]
                    .filter(Boolean)
                    .join(", ")}
                />
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function DetailRow({ label, value }) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        gap: "16px",
        padding: "10px 0",
        borderBottom: "1px solid #f0ede4",
      }}
    >
      <span style={{ fontSize: "12px", fontWeight: "700", color: "#999", textTransform: "uppercase", letterSpacing: "0.4px", whiteSpace: "nowrap" }}>
        {label}
      </span>
      <span style={{ fontSize: "14px", color: "#333", textAlign: "right" }}>
        {value || "-"}
      </span>
    </div>
  );
}

function EditField({ label, value, onChange, type = "text" }) {
  return (
    <div>
      <label style={{ display: "block", fontSize: "11px", fontWeight: "700", color: "#999", textTransform: "uppercase", marginBottom: "4px" }}>
        {label}
      </label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{
          width: "100%",
          padding: "8px 10px",
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

const editButtonStyle = {
  flex: 1,
  padding: "10px",
  borderRadius: "8px",
  border: "1px solid #0a1f44",
  background: "transparent",
  color: "#0a1f44",
  fontSize: "13px",
  fontWeight: "600",
  cursor: "pointer",
};

const deleteButtonStyle = {
  flex: 1,
  padding: "10px",
  borderRadius: "8px",
  border: "none",
  background: "#b91c1c",
  color: "#fff",
  fontSize: "13px",
  fontWeight: "600",
  cursor: "pointer",
};

const saveButtonStyle = {
  flex: 1,
  padding: "10px",
  borderRadius: "8px",
  border: "none",
  background: "#166534",
  color: "#fff",
  fontSize: "13px",
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
  fontSize: "13px",
  cursor: "pointer",
};

const smallSaveStyle = {
  padding: "6px 10px",
  borderRadius: "6px",
  border: "none",
  background: "#166534",
  color: "#fff",
  fontSize: "12px",
  fontWeight: "600",
  cursor: "pointer",
};

const smallCancelStyle = {
  padding: "6px 10px",
  borderRadius: "6px",
  border: "1px solid #ccc",
  background: "transparent",
  color: "#666",
  fontSize: "12px",
  cursor: "pointer",
};

export default EmployeeDetailCard;