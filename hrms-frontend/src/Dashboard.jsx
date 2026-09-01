import { useState } from "react";
import EmployeeList from "./EmployeeList";
import LeaveRequests from "./LeaveRequests";
import Profile from "./Profile";
import Attendance from "./Attendance";
import Payroll from "./Payroll";
import DashboardHome from "./DashboardHome";
import PresentToday from "./PresentToday";
import PendingLeaveRequests from "./PendingLeaveRequests";
import PayrollOverview from "./PayrollOverview";

function Dashboard({ user }) {
  const [activePage, setActivePage] = useState("home");

  const menuItems = [
    { key: "home", label: "Dashboard" },
    { key: "employees", label: "Employees" },
    { key: "attendance", label: "Attendance" },
    { key: "leave", label: "Leave Requests" },
    { key: "payroll", label: "Payroll" },
  ];

  const initials = user?.employeeName
    ? user.employeeName
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
    : "?";

  function pageTitle() {
    if (activePage === "presentToday") return "Present Today";
    if (activePage === "pendingLeave") return "Pending Leave Requests";
    if (activePage === "payrollOverview") return "Payroll Overview";
    return activePage;
  }

  return (
    <div
      style={{
        display: "flex",
        height: "100vh",
        fontFamily: "'Poppins', 'Segoe UI', sans-serif",
        background: "#f5f0e6",
      }}
    >
      <div
        style={{
          width: "240px",
          background: "linear-gradient(180deg, #0a1f44 0%, #14315c 100%)",
          color: "#ffffff",
          padding: "24px 16px",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
        }}
      >
        <div>
          <h2
            style={{
              marginBottom: "32px",
              fontSize: "26px",
              fontWeight: "700",
              letterSpacing: "2px",
              color: "#ffffff",
            }}
          >
            HRMS
          </h2>

          {menuItems.map((item) => (
            <div
              key={item.key}
              onClick={() => setActivePage(item.key)}
              style={{
                padding: "12px 16px",
                marginBottom: "6px",
                borderRadius: "8px",
                cursor: "pointer",
                fontSize: "15px",
                fontWeight: activePage === item.key ? "600" : "400",
                color: "#ffffff",
                background:
                  activePage === item.key
                    ? "rgba(245, 240, 230, 0.15)"
                    : "transparent",
                borderLeft:
                  activePage === item.key
                    ? "3px solid #f5f0e6"
                    : "3px solid transparent",
                transition: "all 0.2s ease",
              }}
            >
              {item.label}
            </div>
          ))}
        </div>

        <div
          onClick={() => setActivePage("profile")}
          style={{
            display: "flex",
            alignItems: "center",
            gap: "12px",
            padding: "12px",
            borderRadius: "10px",
            cursor: "pointer",
            background:
              activePage === "profile" ? "rgba(245, 240, 230, 0.15)" : "transparent",
            borderTop: "1px solid rgba(255,255,255,0.1)",
            paddingTop: "16px",
          }}
        >
          <div
            style={{
              width: "40px",
              height: "40px",
              borderRadius: "50%",
              background: "#f5f0e6",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
            }}
          >
            <span style={{ fontSize: "14px", fontWeight: "700", color: "#0a1f44" }}>
              {initials}
            </span>
          </div>
          <div style={{ overflow: "hidden" }}>
            <div
              style={{
                fontSize: "14px",
                fontWeight: "600",
                color: "#ffffff",
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
              }}
            >
              {user?.employeeName || "Profile"}
            </div>
            <div style={{ fontSize: "12px", color: "rgba(255,255,255,0.6)" }}>
              View profile
            </div>
          </div>
        </div>
      </div>

      <div style={{ flex: 1, padding: "40px 48px", overflowY: "auto" }}>
        <h1
          style={{
            textTransform: "capitalize",
            fontSize: "32px",
            fontWeight: "700",
            color: "#0a1f44",
            marginBottom: "24px",
          }}
        >
          {pageTitle()}
        </h1>

        {activePage === "employees" ? (
          <EmployeeList />
        ) : activePage === "presentToday" ? (
          <PresentToday />
        ) : activePage === "pendingLeave" ? (
          <PendingLeaveRequests user={user} />
        ) : activePage === "payrollOverview" ? (
          <PayrollOverview />
        ) : activePage === "leave" ? (
          <LeaveRequests user={user} />
        ) : activePage === "profile" ? (
          <Profile user={user} />
        ) : activePage === "attendance" ? (
          <Attendance user={user} />
        ) : activePage === "payroll" ? (
          <Payroll user={user} />
        ) : activePage === "home" ? (
          <DashboardHome user={user} onNavigate={setActivePage} />
        ) : (
          <p style={{ color: "#4a4a4a", fontSize: "15px" }}>
            This is the {activePage} page — we'll build the real content next.
          </p>
        )}
      </div>
    </div>
  );
}

export default Dashboard;