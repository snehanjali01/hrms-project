import { useState, useEffect } from "react";
import api from "./api";

const MONTH_NAMES = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

function Payroll() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(new Date().getFullYear());
  const [genError, setGenError] = useState("");
  const [genResult, setGenResult] = useState(null);
  const [generating, setGenerating] = useState(false);

  async function fetchPayroll() {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/payroll"); // no employeeId = all employees
      const sorted = [...response.data].sort((a, b) => {
        if (b.payPeriodYear !== a.payPeriodYear) return b.payPeriodYear - a.payPeriodYear;
        if (b.payPeriodMonth !== a.payPeriodMonth) return b.payPeriodMonth - a.payPeriodMonth;
        return a.employeeName.localeCompare(b.employeeName);
      });
      setRecords(sorted);
    } catch (err) {
      setError("Failed to load payroll records");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchPayroll();
  }, []);

  async function handleGenerateAll(e) {
    e.preventDefault();
    setGenError("");
    setGenResult(null);
    setGenerating(true);
    try {
      const response = await api.post("/payroll/generate-all", {
        month: Number(month),
        year: Number(year),
      });
      setGenResult(response.data);
      fetchPayroll();
    } catch (err) {
      setGenError(err.response?.data?.message || "Failed to generate payroll");
    } finally {
      setGenerating(false);
    }
  }

  function formatCurrency(value) {
    return `₹${Number(value).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  return (
    <div>
      {/* Bulk generate form */}
      <form
        onSubmit={handleGenerateAll}
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
          <label style={labelStyle}>Month</label>
          <select
            value={month}
            onChange={(e) => setMonth(e.target.value)}
            style={inputStyle}
          >
            {MONTH_NAMES.map((name, i) => (
              <option key={i + 1} value={i + 1}>
                {name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Year</label>
          <input
            type="number"
            value={year}
            onChange={(e) => setYear(e.target.value)}
            style={{ ...inputStyle, width: "100px" }}
          />
        </div>
        <button type="submit" disabled={generating} style={generateButtonStyle}>
          {generating ? "Generating..." : "Generate Payroll for All Employees"}
        </button>
      </form>

      {genError && (
        <p style={{ color: "#b91c1c", marginBottom: "16px" }}>{genError}</p>
      )}

      {genResult && (
        <div
          style={{
            background: "#eff6ff",
            border: "1px solid #bfdbfe",
            borderRadius: "10px",
            padding: "16px 20px",
            marginBottom: "20px",
            fontSize: "14px",
            color: "#1e3a5f",
          }}
        >
          <strong>{genResult.generated}</strong> payroll record(s) generated,{" "}
          <strong>{genResult.skipped}</strong> skipped.
          {genResult.skippedReasons?.length > 0 && (
            <ul style={{ margin: "8px 0 0", paddingLeft: "20px" }}>
              {genResult.skippedReasons.map((reason, i) => (
                <li key={i}>{reason}</li>
              ))}
            </ul>
          )}
        </div>
      )}

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
              <th style={thStyle}>Period</th>
              <th style={thStyle}>Basic Salary</th>
              <th style={thStyle}>Deductions</th>
              <th style={thStyle}>Net Salary</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} style={emptyCellStyle}>Loading...</td>
              </tr>
            ) : records.length === 0 ? (
              <tr>
                <td colSpan={5} style={emptyCellStyle}>No payroll records found</td>
              </tr>
            ) : (
              records.map((r) => (
                <tr key={r.id} style={{ borderBottom: "1px solid #eee" }}>
                  <td style={tdStyle}>{r.employeeName}</td>
                  <td style={tdStyle}>
                    {MONTH_NAMES[r.payPeriodMonth - 1]} {r.payPeriodYear}
                  </td>
                  <td style={tdStyle}>{formatCurrency(r.basicSalary)}</td>
                  <td style={{ ...tdStyle, color: "#b91c1c" }}>
                    -{formatCurrency(r.deductions)}
                  </td>
                  <td style={{ ...tdStyle, fontWeight: "700", color: "#166534" }}>
                    {formatCurrency(r.netSalary)}
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

const generateButtonStyle = {
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

export default Payroll;