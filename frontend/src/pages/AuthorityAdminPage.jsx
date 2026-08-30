import React, { useState, useEffect } from 'react';
import { governanceApi } from '../api/governanceApi';
import { Building2, CheckCircle2, Clock, XCircle, RefreshCw, Scale, FileText, X, ChevronDown } from 'lucide-react';

// Status mapping: UI label -> DB persisted value
const STATUS_MAP = {
  ACCEPT: 'RESOLVED',
  HOLD:   'INVESTIGATING',
  REJECT: 'REJECTED',
};

// Customer-facing display labels for each DB status
const STATUS_DISPLAY = {
  PENDING:       { label: 'Pending Review',      bg: '#fffbe6', color: '#d48806', icon: '⏳' },
  INVESTIGATING: { label: 'Under Investigation', bg: '#eff6ff', color: '#1d4ed8', icon: '🔍' },
  RESOLVED:      { label: 'Accepted for Follow-up', bg: '#f0fdf4', color: '#166534', icon: '✅' },
  REJECTED:      { label: 'Rejected',            bg: '#fef2f2', color: '#dc2626', icon: '🚫' },
  DISMISSED:     { label: 'Dismissed',           bg: '#f8fafc', color: '#94a3b8', icon: '❌' },
};

export const AuthorityAdminPage = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedDispute, setSelectedDispute] = useState(null);
  const [reviewNotes, setReviewNotes] = useState('');
  const [updatingId, setUpdatingId] = useState(null);
  const [actionSuccessMsg, setActionSuccessMsg] = useState('');

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    setLoading(true);
    const res = await governanceApi.getComplaints();
    setComplaints(res.data || []);
    setLoading(false);
  };

  const handleDecision = async (decision) => {
    if (!selectedDispute) return;
    const persistedStatus = STATUS_MAP[decision];
    setUpdatingId(selectedDispute.id);
    await governanceApi.updateComplaintStatus(selectedDispute.id, persistedStatus, reviewNotes);
    const decisionLabels = {
      ACCEPT: 'Accepted for Follow-up',
      HOLD:   'Held for Further Investigation',
      REJECT: 'Rejected',
    };
    setActionSuccessMsg(`✅ Complaint #${selectedDispute.id} — ${decisionLabels[decision]}. Decision persisted to database.`);
    await fetchComplaints();
    setUpdatingId(null);
    setSelectedDispute(null);
    setReviewNotes('');
    setTimeout(() => setActionSuccessMsg(''), 5000);
  };

  const filterTabs = [
    { key: 'ALL',          label: 'All Complaints' },
    { key: 'PENDING',      label: 'Pending Review' },
    { key: 'INVESTIGATING',label: 'Under Investigation' },
    { key: 'RESOLVED',     label: 'Accepted' },
    { key: 'REJECTED',     label: 'Rejected' },
  ];

  const displayed = statusFilter === 'ALL'
    ? complaints
    : complaints.filter(c => c.status === statusFilter);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Building2 size={28} color="#166534" />
          Authority Review Hub
        </h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          Review consumer price complaints and issue ACCEPT, HOLD, or REJECT decisions. All decisions are persisted to the database and immediately visible to the complainant.
        </p>
      </div>

      {/* Success banner */}
      {actionSuccessMsg && (
        <div style={{ padding: '14px 20px', borderRadius: '12px', backgroundColor: '#f0fdf4', color: '#166534', fontWeight: 700, border: '1px solid #bbf7d0', fontSize: '14px' }}>
          {actionSuccessMsg}
        </div>
      )}

      {/* Status filter tabs */}
      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        {filterTabs.map(tab => (
          <button
            key={tab.key}
            onClick={() => setStatusFilter(tab.key)}
            className={`filter-pill ${statusFilter === tab.key ? 'active' : ''}`}
            style={{ padding: '10px 20px', fontSize: '13px' }}
          >
            {tab.label}
            <span style={{
              marginLeft: '6px',
              padding: '2px 7px',
              borderRadius: '20px',
              backgroundColor: statusFilter === tab.key ? 'rgba(255,255,255,0.3)' : '#f1f5f9',
              fontSize: '11px',
              fontWeight: 800,
            }}>
              {tab.key === 'ALL' ? complaints.length : complaints.filter(c => c.status === tab.key).length}
            </span>
          </button>
        ))}
        <button
          onClick={fetchComplaints}
          style={{ marginLeft: 'auto', padding: '10px 16px', borderRadius: '10px', border: '1px solid #e2e8f0', backgroundColor: '#ffffff', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: '#64748b', fontWeight: 600 }}
        >
          <RefreshCw size={14} />
          Refresh
        </button>
      </div>

      {/* Complaints Table */}
      <div className="glass-card" style={{ padding: '0', borderRadius: '20px', backgroundColor: '#ffffff', overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '60px', textAlign: 'center', color: '#64748b' }}>Loading complaints...</div>
        ) : displayed.length === 0 ? (
          <div style={{ padding: '60px', textAlign: 'center', color: '#64748b' }}>
            <FileText size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
            <p>No complaints in this category.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #f1f5f9', backgroundColor: '#f8fafc' }}>
                  {['ID', 'Complainant', 'Retailer / Market', 'Commodity', 'Price Paid vs Fair', 'Overcharge', 'Status', 'Action'].map(h => (
                    <th key={h} style={{ padding: '14px 16px', fontSize: '12px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {displayed.map((c) => {
                  const st = STATUS_DISPLAY[c.status] || STATUS_DISPLAY.PENDING;
                  return (
                    <tr key={c.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background 0.1s' }}
                        onMouseEnter={e => e.currentTarget.style.backgroundColor = '#f8fafc'}
                        onMouseLeave={e => e.currentTarget.style.backgroundColor = 'transparent'}>
                      <td style={{ padding: '14px 16px', fontWeight: 800, color: '#166534', fontSize: '14px' }}>
                        #{c.id}
                      </td>
                      <td style={{ padding: '14px 16px', fontSize: '13px', color: '#0f172a', fontWeight: 600 }}>
                        {c.complainantUsername || c.user || 'N/A'}
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <div style={{ fontWeight: 700, color: '#0f172a', fontSize: '13px' }}>{c.retailerName || c.market || '—'}</div>
                        <div style={{ fontSize: '11px', color: '#64748b' }}>{c.location || ''}</div>
                      </td>
                      <td style={{ padding: '14px 16px', fontSize: '13px', color: '#166534', fontWeight: 600 }}>
                        {c.commodityName || c.commodity || '—'}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: 700, fontSize: '14px' }}>
                        ₹{c.reportedPrice || c.pricePaid || '—'}
                        <span style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 400 }}> (Fair ₹{c.fairPrice || c.benchmarkPrice || '—'})</span>
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        {c.inflationPercent || c.overchargePercent ? (
                          <span style={{ padding: '4px 8px', borderRadius: '6px', backgroundColor: '#fef2f2', color: '#dc2626', fontWeight: 800, fontSize: '12px' }}>
                            {c.inflationPercent || c.overchargePercent}
                          </span>
                        ) : '—'}
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 800, backgroundColor: st.bg, color: st.color }}>
                          {st.icon} {st.label}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <button
                          onClick={() => { setSelectedDispute(c); setReviewNotes(''); }}
                          style={{ padding: '8px 14px', borderRadius: '8px', border: 'none', backgroundColor: '#166534', color: '#ffffff', fontSize: '12px', fontWeight: 700, cursor: 'pointer' }}
                        >
                          Review
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Review Decision Modal */}
      {selectedDispute && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(15, 23, 42, 0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '20px', padding: '32px', maxWidth: '680px', width: '92%', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)' }}>
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #166534', paddingBottom: '16px', marginBottom: '20px' }}>
              <div>
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#166534', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Scale size={24} />
                  <span>Complaint Review — #{selectedDispute.id}</span>
                </h3>
                <span style={{ fontSize: '12px', color: '#64748b' }}>
                  Complainant: <strong>{selectedDispute.complainantUsername || selectedDispute.user || 'N/A'}</strong>
                </span>
              </div>
              <button onClick={() => setSelectedDispute(null)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: '#94a3b8' }}>
                <X size={22} />
              </button>
            </div>

            {/* Price Evidence Card */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', padding: '16px', borderRadius: '14px', backgroundColor: '#f8fafc', border: '1px solid #cbd5e1', marginBottom: '20px' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Price Paid</span>
                <div style={{ fontSize: '22px', fontWeight: 800, color: '#dc2626' }}>₹{selectedDispute.reportedPrice || selectedDispute.pricePaid || '—'}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Mandi Benchmark</span>
                <div style={{ fontSize: '22px', fontWeight: 800, color: '#166534' }}>₹{selectedDispute.fairPrice || selectedDispute.benchmarkPrice || '—'}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Overcharge %</span>
                <div style={{ fontSize: '22px', fontWeight: 800, color: '#d97706' }}>{selectedDispute.inflationPercent || selectedDispute.overchargePercent || '—'}</div>
              </div>
            </div>

            {/* Complaint Description */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>📄 Consumer's Evidence & Description</label>
              <div style={{ padding: '14px', borderRadius: '10px', backgroundColor: '#f1f5f9', border: '1px solid #cbd5e1', fontSize: '13px', maxHeight: '140px', overflowY: 'auto', color: '#0f172a', whiteSpace: 'pre-wrap' }}>
                {selectedDispute.description || '(No description provided)'}
              </div>
            </div>

            {/* Authority Review Notes */}
            <div style={{ marginBottom: '24px' }}>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                📝 Review Notes <span style={{ fontWeight: 400, color: '#94a3b8' }}>(optional — visible in audit log)</span>
              </label>
              <textarea
                rows={3}
                placeholder="Record your review findings, evidence evaluation, or reasoning for the decision..."
                value={reviewNotes}
                onChange={(e) => setReviewNotes(e.target.value)}
                style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #cbd5e1', fontSize: '13px', outline: 'none', fontFamily: 'inherit', resize: 'vertical', boxSizing: 'border-box' }}
              />
            </div>

            {/* Decision Buttons */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '16px' }}>
              <button
                onClick={() => handleDecision('ACCEPT')}
                disabled={!!updatingId}
                style={{ padding: '14px', borderRadius: '12px', border: 'none', backgroundColor: '#166534', color: '#ffffff', fontWeight: 800, fontSize: '13px', cursor: 'pointer', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px', boxShadow: '0 4px 14px rgba(22, 101, 52, 0.3)', opacity: updatingId ? 0.6 : 1 }}
              >
                <CheckCircle2 size={22} />
                <span>ACCEPT FOR FOLLOW-UP</span>
                <span style={{ fontSize: '10px', opacity: 0.8, fontWeight: 400 }}>Saved as: RESOLVED</span>
              </button>
              <button
                onClick={() => handleDecision('HOLD')}
                disabled={!!updatingId}
                style={{ padding: '14px', borderRadius: '12px', border: 'none', backgroundColor: '#1d4ed8', color: '#ffffff', fontWeight: 800, fontSize: '13px', cursor: 'pointer', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px', boxShadow: '0 4px 14px rgba(29, 78, 216, 0.3)', opacity: updatingId ? 0.6 : 1 }}
              >
                <Clock size={22} />
                <span>HOLD FOR FURTHER INVESTIGATION</span>
                <span style={{ fontSize: '10px', opacity: 0.8, fontWeight: 400 }}>Saved as: INVESTIGATING</span>
              </button>
              <button
                onClick={() => handleDecision('REJECT')}
                disabled={!!updatingId}
                style={{ padding: '14px', borderRadius: '12px', border: 'none', backgroundColor: '#dc2626', color: '#ffffff', fontWeight: 800, fontSize: '13px', cursor: 'pointer', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px', boxShadow: '0 4px 14px rgba(220, 38, 38, 0.3)', opacity: updatingId ? 0.6 : 1 }}
              >
                <XCircle size={22} />
                <span>REJECT COMPLAINT</span>
                <span style={{ fontSize: '10px', opacity: 0.8, fontWeight: 400 }}>Saved as: REJECTED</span>
              </button>
            </div>

            {updatingId && (
              <p style={{ textAlign: 'center', color: '#64748b', fontSize: '13px' }}>⏳ Persisting decision to database...</p>
            )}

            <div style={{ textAlign: 'right' }}>
              <button onClick={() => setSelectedDispute(null)} style={{ padding: '10px 24px', borderRadius: '10px', backgroundColor: '#f1f5f9', color: '#334155', border: 'none', fontWeight: 700, cursor: 'pointer', fontSize: '13px' }}>
                Close without Decision
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
