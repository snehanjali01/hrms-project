import { useState, useEffect } from "react";
import api from "./api";
import EmployeeDetailCard from "./EmployeeDetailCard";
import AddEmployeeModal from "./AddEmployeeModal.jsx";

function EmployeeList({ canManage }) {
  const [employees, setEmployees] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortField, setSortField] = useState("firstName");
  const [sortDir, setSortDir] = useState("asc");

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [employeeCode, setEmployeeCode] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);

  async function fetchEmployees() {
    setLoading(true);
    setError("");
    try {
      const params = {
        page,
        size,
        sort: `${sortField},${sortDir}`,
      };
      if (firstName) params.firstName = firstName;
      if (lastName) params.lastName = lastName;
      if (email) params.email = email;
      if (employeeCode) params.employeeCode = employeeCode;

      const response = await api.get("/employees", { params });
      setEmployees(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError("Failed to load employees");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchEmployees();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, sortField, sortDir]);

  function handleSearch(e) {
    e.preventDefault();
    setPage(0);
    fetchEmployees();
  }

  function toggleSort(field) {
    if (sortField === field) {
      setSortDir(sortDir === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDir("asc");
    }
  }

  function sortArrow(field) {
    if (sortField !== field) return "";
    return sortDir === "asc" ? " ▲" : " ▼";
  }

  function handleUpdated(updatedEmployee) {
    setSelectedEmployee(updatedEmployee);
    fetchEmployees();
  }

  function handleDeleted() {
    fetchEmployees();
  }

  function handleCreated() {
    fetchEmployees();
  }

  return (
    <div>
      {/* Filters + Add button */}
      <form
        onSubmit={handleSearch}
        style={{
          display: "flex",
          gap: "12px",
          marginBottom: "24px",
          flexWrap: "wrap",
          alignItems: "center",
        }}
      >
        <input
          placeholder="First name"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          style={filterInputStyle}
        />
        <input
          placeholder="Last name"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          style={filterInputStyle}
        />
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={filterInputStyle}
        />
        <input
          placeholder="Employee code"
          value={employeeCode}
          onChange={(e) => setEmployeeCode(e.target.value)}
          style={filterInputStyle}
        />
        <button type="submit" style={searchButtonStyle}>
          Search
        </button>
        {canManage && (
          <button
            type="button"
            onClick={() => setShowAddModal(true)}
            style={{ ...searchButtonStyle, marginLeft: "auto", background: "#166534" }}
          >
            + Add Employee
          </button>
        )}
      </form>

      {error && <p style={{ color: "#b91c1c" }}>{error}</p>}

      {/* Table */}
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
              <th style={thStyle} onClick={() => toggleSort("firstName")}>
                First Name{sortArrow("firstName")}
              </th>
              <th style={thStyle} onClick={() => toggleSort("lastName")}>
                Last Name{sortArrow("lastName")}
              </th>
              <th style={thStyle} onClick={() => toggleSort("email")}>
                Email{sortArrow("email")}
              </th>
              <th style={thStyle}>Code</th>
              <th style={thStyle}>Department</th>
              <th style={thStyle}>Company</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} style={emptyCellStyle}>
                  Loading...
                </td>
              </tr>
            ) : employees.length === 0 ? (
              <tr>
                <td colSpan={6} style={emptyCellStyle}>
                  No employees found
                </td>
              </tr>
            ) : (
              employees.map((emp) => (
                <tr
                  key={emp.id}
                  onClick={() => setSelectedEmployee(emp)}
                  style={{ borderBottom: "1px solid #eee", cursor: "pointer" }}
                >
                  <td style={tdStyle}>{emp.firstName}</td>
                  <td style={tdStyle}>{emp.lastName}</td>
                  <td style={tdStyle}>{emp.email}</td>
                  <td style={tdStyle}>{emp.employeeCode}</td>
                  <td style={tdStyle}>{emp.departmentName}</td>
                  <td style={tdStyle}>{emp.companyName}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "16px",
          marginTop: "20px",
        }}
      >
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          style={pageButtonStyle}
        >
          Previous
        </button>
        <span style={{ fontSize: "14px", color: "#0a1f44" }}>
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
        </span>
        <button
          onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
          disabled={page >= totalPages - 1}
          style={pageButtonStyle}
        >
          Next
        </button>
      </div>

      {/* Detail card popup */}
      <EmployeeDetailCard
        employee={selectedEmployee}
        onClose={() => setSelectedEmployee(null)}
        onUpdated={handleUpdated}
        onDeleted={handleDeleted}
        canManage={canManage}
      />

      {/* Add employee modal */}
      {showAddModal && (
        <AddEmployeeModal
          onClose={() => setShowAddModal(false)}
          onCreated={handleCreated}
        />
      )}
    </div>
  );
}

const filterInputStyle = {
  padding: "8px 12px",
  borderRadius: "8px",
  border: "1px solid #d6d0c4",
  fontSize: "14px",
  fontFamily: "inherit",
  minWidth: "150px",
};

const searchButtonStyle = {
  padding: "8px 20px",
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
  cursor: "pointer",
  userSelect: "none",
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

const pageButtonStyle = {
  padding: "8px 16px",
  borderRadius: "8px",
  border: "1px solid #0a1f44",
  background: "transparent",
  color: "#0a1f44",
  fontSize: "14px",
  cursor: "pointer",
};

export default EmployeeList;