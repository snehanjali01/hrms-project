import { useState, useEffect } from "react";
import api from "./api";

const MONTH_NAMES = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

function PayrollOverview() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchAll() {
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
    fetchAll();
  }, []);

  function formatCurrency(value) {
    return `₹${Number(value).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
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

const thStyle = { padding: "14px 16px", textAlign: "left", color: "#ffffff", fontSize: "13px", fontWeight: "600" };
const tdStyle = { padding: "12px 16px", fontSize: "14px", color: "#333" };
const emptyCellStyle = { padding: "24px", textAlign: "center", color: "#888" };

export default PayrollOverview;