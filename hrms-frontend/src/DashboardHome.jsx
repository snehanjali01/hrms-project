import { useState, useEffect } from "react";
import api from "./api";

function DashboardHome({ user, onNavigate }) {
  const [totalEmployees, setTotalEmployees] = useState(null);
  const [presentToday, setPresentToday] = useState(null);
  const [pendingLeave, setPendingLeave] = useState(null);
  const [payrollPending, setPayrollPending] = useState(null);
  const [loading, setLoading] = useState(true);

  // The API enforces these rules; the UI only skips calls and cards the user cannot use
  const roles = user?.roles || [];
  const isHrOrAdmin = roles.includes("ADMIN") || roles.includes("HR");
  const isApprover = isHrOrAdmin || roles.includes("MANAGER");

  useEffect(() => {
    async function fetchStats() {
      try {
        const employeesRes = await api.get("/employees", { params: { page: 0, size: 1 } });
        const total = employeesRes.data.totalElements;
        setTotalEmployees(total);

        if (isHrOrAdmin) {
          const [attendanceRes, payrollRes] = await Promise.all([
            api.get("/attendance"),
            api.get("/payroll"), // org-wide, no employeeId filter
          ]);

          const today = new Date().toISOString().split("T")[0];
          const presentCount = attendanceRes.data.filter(
            (a) => a.attendanceDate === today && a.status === "PRESENT"
          ).length;
          setPresentToday(presentCount);

          // Payroll for month M is generated on the 1st of month M+1 (per the cron job),
          // so "this period's payroll" always refers to LAST month, not the current one.
          const now = new Date();
          const prevMonthDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
          const targetMonth = prevMonthDate.getMonth() + 1;
          const targetYear = prevMonthDate.getFullYear();

          const generatedCount = payrollRes.data.filter(
            (p) => p.payPeriodMonth === targetMonth && p.payPeriodYear === targetYear
          ).length;
          setPayrollPending(Math.max(total - generatedCount, 0));
        }

        if (isApprover) {
          const pendingRes = await api.get("/leave-requests/pending");
          setPendingLeave(pendingRes.data.length);
        }
      } catch (err) {
        console.error("Failed to load dashboard stats", err);
      } finally {
        setLoading(false);
      }
    }
    fetchStats();
  }, [user.employeeId, isHrOrAdmin, isApprover]);

  const cards = [
    {
      label: "Total Employees",
      value: totalEmployees,
      accent: "#0a1f44",
      icon: "👥",
      navigateTo: "employees",
      visible: true,
    },
    {
      label: "Present Today",
      value: presentToday,
      accent: "#166534",
      icon: "✅",
      navigateTo: "presentToday",
      visible: isHrOrAdmin,
    },
    {
      label: "Pending Leave Requests",
      value: pendingLeave,
      accent: "#92400e",
      icon: "🗓️",
      navigateTo: "pendingLeave",
      visible: isApprover,
    },
    {
      label: "Payroll Status",
      value: payrollPending === 0 ? "All Generated" : `${payrollPending} Pending`,
      accent: "#6d28d9",
      icon: "💰",
      isText: true,
      navigateTo: "payrollOverview",
      visible: isHrOrAdmin,
    },
  ];

  const today = new Date();
  const dateStr = today.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });

  return (
    <div>
      <div
        style={{
          background: "linear-gradient(135deg, #0a1f44 0%, #14315c 100%)",
          borderRadius: "18px",
          padding: "36px 40px",
          marginBottom: "28px",
          color: "#ffffff",
          boxShadow: "0 6px 20px rgba(10,31,68,0.25)",
        }}
      >
        <p
          style={{
            fontSize: "13px",
            fontWeight: "600",
            letterSpacing: "1px",
            textTransform: "uppercase",
            color: "rgba(245,240,230,0.7)",
            margin: 0,
            marginBottom: "8px",
          }}
        >
          {dateStr}
        </p>
        <h2 style={{ fontSize: "28px", fontWeight: "700", margin: 0 }}>
          Welcome back, {user?.employeeName?.split(" ")[0] || "there"} 👋
        </h2>
        <p
          style={{
            fontSize: "14px",
            color: "rgba(245,240,230,0.75)",
            marginTop: "8px",
            marginBottom: 0,
          }}
        >
          Here's what's happening across the organization today.
        </p>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: "24px",
        }}
      >
        {cards.filter((card) => card.visible).map((card) => (
          <div
            key={card.label}
            onClick={() => onNavigate(card.navigateTo)}
            style={{
              background: "#ffffff",
              borderRadius: "18px",
              padding: "32px",
              boxShadow: "0 4px 16px rgba(0,0,0,0.07)",
              borderTop: `4px solid ${card.accent}`,
              position: "relative",
              overflow: "hidden",
              cursor: "pointer",
              transition: "transform 0.15s ease",
            }}
          >
            <div style={{ fontSize: "34px", marginBottom: "18px" }}>{card.icon}</div>
            <p
              style={{
                fontSize: "13px",
                fontWeight: "700",
                color: "#999",
                marginBottom: "10px",
                textTransform: "uppercase",
                letterSpacing: "0.6px",
              }}
            >
              {card.label}
            </p>
            <p
              style={{
                fontSize: card.isText ? "26px" : "40px",
                fontWeight: "800",
                color: card.accent,
                margin: 0,
                lineHeight: 1,
              }}
            >
              {loading ? "···" : card.value ?? "-"}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default DashboardHome;