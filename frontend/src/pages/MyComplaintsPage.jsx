import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { governanceApi } from "../api/governanceApi";
import {
  FileText, AlertTriangle, CheckCircle2, Clock, XCircle,
  Search, ShieldAlert, Plus, ChevronDown, ChevronUp,
  Tag, Store, Calendar, Receipt, MessageSquare
} from "lucide-react";

const STATUS_CONFIG = {
  PENDING: { label: "Pending Review", color: "#d97706", bg: "#fef3c7", icon: Clock },
  INVESTIGATING: { label: "Under Investigation", color: "#2563eb", bg: "#dbeafe", icon: Search },
  RESOLVED: { label: "Resolved", color: "#16a34a", bg: "#dcfce7", icon: CheckCircle2 },
  REJECTED: { label: "Rejected", color: "#dc2626", bg: "#fee2e2", icon: XCircle },
};

const StatusBadge = ({ status }) => {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;
  const Icon = cfg.icon;
  return (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: "5px",
      padding: "4px 12px", borderRadius: "20px", fontSize: "12px",
      fontWeight: 700, backgroundColor: cfg.bg, color: cfg.color,
    }}>
      <Icon size={12} />
      {cfg.label}
    </span>
  );
};

const ComplaintCard = ({ complaint }) => {
  const [expanded, setExpanded] = useState(false);
  const priceDiff = complaint.reportedPrice && complaint.fairPrice
    ? (complaint.reportedPrice - complaint.fairPrice).toFixed(2) : null;

  return (
    <div style={{
      borderRadius: "16px", border: "1px solid #e2e8f0",
      backgroundColor: "#ffffff", overflow: "hidden",
      boxShadow: "0 1px 4px rgba(15, 23, 42, 0.06)"
    }}>
      <div style={{
        padding: "20px 24px", display: "flex", justifyContent: "space-between",
        alignItems: "flex-start", gap: "16px",
        borderBottom: expanded ? "1px solid #f1f5f9" : "none"
      }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px", flexWrap: "wrap" }}>
            <span style={{ fontSize: "12px", fontWeight: 800, color: "#166534", letterSpacing: "0.04em" }}>
              {complaint.displayId || ("CMP-" + complaint.id)}
            </span>
            <StatusBadge status={complaint.status} />
          </div>
          <h4 style={{ fontSize: "15px", fontWeight: 700, color: "#0f172a", margin: 0, lineHeight: 1.4 }}>
            {complaint.title}
          </h4>
          <div style={{ display: "flex", gap: "16px", marginTop: "10px", flexWrap: "wrap" }}>
            {complaint.commodityName && complaint.commodityName !== "Not Available" && (
              <span style={{ display: "flex", alignItems: "center", gap: "4px", fontSize: "12px", color: "#64748b" }}>
                <Tag size={12} /> {complaint.commodityName}
              </span>
            )}
            {complaint.marketName && complaint.marketName !== "Not Available" && (
              <span style={{ display: "flex", alignItems: "center", gap: "4px", fontSize: "12px", color: "#64748b" }}>
                <Store size={12} /> {complaint.marketName}
              </span>
            )}
            {complaint.createdAt && (
              <span style={{ display: "flex", alignItems: "center", gap: "4px", fontSize: "12px", color: "#64748b" }}>
                <Calendar size={12} />
                Filed: {new Date(complaint.createdAt).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
              </span>
            )}
          </div>
        </div>

        <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "8px", flexShrink: 0 }}>
          {complaint.reportedPrice && (
            <div style={{ textAlign: "right" }}>
              <div style={{ fontSize: "11px", color: "#94a3b8", fontWeight: 600, marginBottom: "2px" }}>PRICE PAID</div>
              <div style={{ fontSize: "18px", fontWeight: 800, color: "#dc2626" }}>
                {"₹" + complaint.reportedPrice + "/kg"}
              </div>
              {complaint.fairPrice && (
                <div style={{ fontSize: "11px", color: "#64748b" }}>
                  {"Fair: ₹" + complaint.fairPrice + "/kg"}
                  {priceDiff && <span style={{ color: "#dc2626", fontWeight: 700 }}>{" (+₹" + priceDiff + ")"}</span>}
                </div>
              )}
            </div>
          )}
          <button onClick={() => setExpanded(x => !x)} style={{
            display: "flex", alignItems: "center", gap: "4px",
            padding: "6px 12px", borderRadius: "8px",
            border: "1px solid #e2e8f0", backgroundColor: "#f8fafc",
            color: "#475569", fontSize: "12px", fontWeight: 600, cursor: "pointer"
          }}>
            {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
            {expanded ? "Hide" : "Details"}
          </button>
        </div>
      </div>

      {expanded && (
        <div style={{ padding: "20px 24px", display: "flex", flexDirection: "column", gap: "16px" }}>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "14px" }}>
            {[
              { icon: Receipt, label: "Invoice Number", value: complaint.billNumber },
              { icon: Calendar, label: "Purchase Date", value: complaint.purchaseDate },
              { icon: AlertTriangle, label: "Overcharge %", value: complaint.inflationPercent },
              { icon: Store, label: "Shop / Market", value: complaint.marketName !== "Not Available" ? complaint.marketName : null },
            ].filter(f => f.value).map((field, idx) => {
              const Icon = field.icon;
              return (
                <div key={idx} style={{
                  padding: "12px 14px", borderRadius: "10px",
                  backgroundColor: "#f8fafc", border: "1px solid #f1f5f9"
                }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "4px" }}>
                    <Icon size={13} color="#64748b" />
                    <span style={{ fontSize: "11px", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.04em" }}>
                      {field.label}
                    </span>
                  </div>
                  <span style={{ fontSize: "14px", fontWeight: 700, color: "#0f172a" }}>{field.value}</span>
                </div>
              );
            })}
          </div>
          {complaint.resolutionNotes && (
            <div style={{
              padding: "14px 16px", borderRadius: "10px",
              backgroundColor: "#f0fdf4", border: "1px solid #bbf7d0"
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "6px" }}>
                <MessageSquare size={14} color="#16a34a" />
                <span style={{ fontSize: "12px", fontWeight: 700, color: "#16a34a", textTransform: "uppercase" }}>
                  Official Authority Notes
                </span>
              </div>
              <p style={{ fontSize: "13px", color: "#0f172a", margin: 0, lineHeight: 1.5 }}>
                {complaint.resolutionNotes}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export const MyComplaintsPage = () => {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await governanceApi.getMyComplaints();
        const apiList = Array.isArray(res?.data)
          ? res.data
          : (res?.data?.data?.content || res?.data?.data || []);
        setComplaints(apiList);
      } catch {
        setComplaints([]);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const filtered = filter === "ALL" ? complaints : complaints.filter(c => c.status === filter);
  const countByStatus = s => complaints.filter(c => c.status === s).length;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "28px" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", flexWrap: "wrap", gap: "16px" }}>
        <div>
          <h2 style={{ fontSize: "28px", fontWeight: 800, color: "#0f172a", marginBottom: "6px" }}>
            My Legal Disputes
          </h2>
          <p style={{ fontSize: "15px", color: "#64748b" }}>
            Track the status of your submitted anti-price-gouging complaints.
          </p>
        </div>
        <button
          id="file-new-complaint-btn"
          onClick={() => navigate("/complaints")}
          style={{
            display: "flex", alignItems: "center", gap: "8px",
            padding: "12px 20px", borderRadius: "12px",
            backgroundColor: "#dc2626", color: "#ffffff", border: "none",
            fontSize: "14px", fontWeight: 700, cursor: "pointer",
            boxShadow: "0 4px 14px rgba(220, 38, 38, 0.25)", whiteSpace: "nowrap"
          }}
        >
          <Plus size={16} />
          File New Complaint
        </button>
      </div>

      {/* Stats */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))", gap: "14px" }}>
        {[
          { label: "Total Filed", value: complaints.length, color: "#0f172a", bg: "#f8fafc" },
          { label: "Pending", value: countByStatus("PENDING"), color: "#d97706", bg: "#fef3c7" },
          { label: "Investigating", value: countByStatus("INVESTIGATING"), color: "#2563eb", bg: "#dbeafe" },
          { label: "Resolved", value: countByStatus("RESOLVED"), color: "#16a34a", bg: "#dcfce7" },
        ].map((stat, i) => (
          <div key={i} style={{
            padding: "16px 20px", borderRadius: "14px",
            backgroundColor: stat.bg, border: "1px solid rgba(0,0,0,0.05)"
          }}>
            <div style={{ fontSize: "26px", fontWeight: 900, color: stat.color }}>{stat.value}</div>
            <div style={{ fontSize: "12px", fontWeight: 600, color: "#64748b", marginTop: "2px" }}>{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Filter Tabs */}
      <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
        {["ALL", "PENDING", "INVESTIGATING", "RESOLVED", "REJECTED"].map(s => (
          <button
            key={s}
            id={"filter-" + s.toLowerCase()}
            onClick={() => setFilter(s)}
            style={{
              padding: "8px 18px", borderRadius: "20px",
              border: filter === s ? "2px solid #166534" : "1px solid #e2e8f0",
              backgroundColor: filter === s ? "#166534" : "#ffffff",
              color: filter === s ? "#ffffff" : "#475569",
              fontSize: "13px", fontWeight: filter === s ? 700 : 500,
              cursor: "pointer", transition: "all 0.15s ease"
            }}
          >
            {s === "ALL" ? ("All (" + complaints.length + ")") : ((STATUS_CONFIG[s]?.label || s) + " (" + countByStatus(s) + ")")}
          </button>
        ))}
      </div>

      {/* Content */}
      {loading ? (
        <div style={{ textAlign: "center", padding: "60px 20px", color: "#94a3b8" }}>
          <div style={{
            width: "40px", height: "40px", border: "3px solid #e2e8f0",
            borderTopColor: "#166534", borderRadius: "50%",
            animation: "spin 0.8s linear infinite", margin: "0 auto 16px"
          }} />
          <p style={{ fontSize: "15px", fontWeight: 600 }}>Loading your disputes...</p>
        </div>
      ) : filtered.length === 0 ? (
        <div style={{
          textAlign: "center", padding: "60px 40px", borderRadius: "20px",
          border: "2px dashed #e2e8f0", backgroundColor: "#f8fafc"
        }}>
          <FileText size={48} color="#cbd5e1" style={{ margin: "0 auto 16px" }} />
          <h3 style={{ fontSize: "18px", fontWeight: 700, color: "#475569", marginBottom: "8px" }}>
            {filter === "ALL" ? "No Disputes Filed Yet" : ("No " + (STATUS_CONFIG[filter]?.label || filter) + " Disputes")}
          </h3>
          <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
            {filter === "ALL"
              ? "File a complaint if you have been overcharged at any market or retail store."
              : "No complaints match this filter."}
          </p>
          {filter === "ALL" && (
            <button onClick={() => navigate("/complaints")} style={{
              padding: "12px 24px", borderRadius: "10px",
              backgroundColor: "#166534", color: "#ffffff",
              border: "none", fontSize: "14px", fontWeight: 700, cursor: "pointer"
            }}>
              + File Your First Complaint
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
          {filtered.map(c => <ComplaintCard key={c.id || c.displayId} complaint={c} />)}
        </div>
      )}

      {/* Privacy Footer */}
      <div style={{
        padding: "16px 20px", borderRadius: "12px",
        backgroundColor: "#f8fafc", border: "1px solid #e2e8f0",
        display: "flex", alignItems: "flex-start", gap: "12px"
      }}>
        <ShieldAlert size={18} color="#64748b" style={{ flexShrink: 0, marginTop: "1px" }} />
        <p style={{ fontSize: "12px", color: "#64748b", margin: 0, lineHeight: 1.5 }}>
          <strong>Privacy Notice:</strong> Only you can view this page. Your complaint history is scoped exclusively to your authenticated account.
          Authority officers use the Authority Audit Hub and cannot access other consumers&apos; data.
          All data is stored securely per Section 12 — Consumer Protection Act, 2019.
        </p>
      </div>

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
};
