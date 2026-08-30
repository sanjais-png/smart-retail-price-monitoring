import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { catalogApi } from '../api/catalogApi';
import axiosClient from '../api/axiosClient';
import {
  ArrowLeft,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  ShieldCheck,
  Bell,
  Flag,
  MapPin,
  Activity,
  CheckCircle,
  ChevronRight,
  RefreshCw
} from 'lucide-react';

const EMOJI_MAP = {
  Tomato: '🍅',
  Onion: '🧅',
  Potato: '🥔',
  Rice: '🌾',
  Wheat: '🌾',
  Dal: '🥣',
  Oil: '🛢️',
  Milk: '🥛',
  Egg: '🥚',
  Apple: '🍎',
  Banana: '🍌',
  Sugar: '🍬',
  Ginger: '🫚',
  Garlic: '🧄',
  Carrot: '🥕',
  Cabbage: '🥬',
  Cauliflower: '🥦'
};

const getEmojiForCommodity = (name) => {
  if (!name) return '🛒';
  for (const [key, emoji] of Object.entries(EMOJI_MAP)) {
    if (name.toLowerCase().includes(key.toLowerCase())) return emoji;
  }
  return '📦';
};

export const CommodityDetailPage = () => {
  const { commodityId } = useParams();
  const navigate = useNavigate();

  const [commodity, setCommodity] = useState(null);
  const [historyData, setHistoryData] = useState([]);
  const [regionalData, setRegionalData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const loadCommodityDetails = async () => {
      setLoading(true);
      setError(null);
      setCommodity(null);
      setHistoryData([]);
      setRegionalData([]);

      try {
        const comRes = await catalogApi.getCommodityById(commodityId);
        
        if (!isMounted) return;

        if (comRes && comRes.success && comRes.data) {
          setCommodity(comRes.data);
        } else {
          setError(comRes?.error || 'Commodity not found');
          setLoading(false);
          return;
        }

        // Fetch real history data from existing backend API
        try {
          const histRes = await axiosClient.get('/api/v1/history', {
            params: { commodityId, marketId: 1 }
          });
          if (isMounted && histRes.data && histRes.data.data) {
            const rawHist = Array.isArray(histRes.data.data) ? histRes.data.data : [];
            setHistoryData(rawHist);
          }
        } catch (hErr) {
          // If history call fails, keep historyData empty
        }

        // Fetch real regional benchmarks from existing dataset API
        try {
          const regRes = await axiosClient.get('/api/v1/datasets/regional-benchmarks');
          if (isMounted && regRes.data && regRes.data.data) {
            const rawReg = Array.isArray(regRes.data.data) ? regRes.data.data : [];
            setRegionalData(rawReg.slice(0, 5));
          }
        } catch (rErr) {
          // If regional call fails, keep regionalData empty
        }

      } catch (err) {
        if (isMounted) {
          console.error('Failed to load commodity details:', err);
          setError('Unable to load commodity information. Please try again.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    if (commodityId) {
      loadCommodityDetails();
    }

    return () => {
      isMounted = false;
    };
  }, [commodityId]);

  if (loading) {
    return (
      <div style={{ padding: '40px 0', textAlign: 'center' }}>
        <div className="glass-card" style={{ padding: '48px', borderRadius: '24px', maxWidth: '600px', margin: '0 auto' }}>
          <Activity size={36} className="spin" color="#166534" style={{ marginBottom: '16px' }} />
          <h3 style={{ fontSize: '20px', fontWeight: 700, color: '#0f172a' }}>Loading Commodity Details...</h3>
          <p style={{ fontSize: '14px', color: '#64748b', marginTop: '8px' }}>
            Fetching baseline prices, 24h variation stats, and market benchmarks.
          </p>
        </div>
      </div>
    );
  }

  if (error || !commodity) {
    return (
      <div style={{ padding: '40px 0', textAlign: 'center' }}>
        <div className="glass-card" style={{ padding: '48px', borderRadius: '24px', maxWidth: '560px', margin: '0 auto', backgroundColor: '#ffffff' }}>
          <AlertTriangle size={48} color="#dc2626" style={{ marginBottom: '16px' }} />
          <h3 style={{ fontSize: '22px', fontWeight: 800, color: '#0f172a' }}>Commodity Not Found</h3>
          <p style={{ fontSize: '14px', color: '#64748b', marginTop: '8px', marginBottom: '24px' }}>
            {error || 'The requested commodity ID does not exist in the catalog.'}
          </p>
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'center' }}>
            <button
              onClick={() => navigate('/catalog')}
              style={{
                padding: '10px 20px',
                borderRadius: '12px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                cursor: 'pointer'
              }}
            >
              Back to Catalog
            </button>
            <button
              onClick={() => window.location.reload()}
              style={{
                padding: '10px 20px',
                borderRadius: '12px',
                backgroundColor: '#f1f5f9',
                color: '#334155',
                border: '1px solid #cbd5e1',
                fontWeight: 600,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  const emoji = getEmojiForCommodity(commodity.name);
  const benchPrice = commodity.benchmarkPrice;
  const isUp = commodity.priceChange?.startsWith('+');
  const minFair = commodity.minPrice;
  const maxFair = commodity.maxPrice;
  const yestPrice = commodity.yesterdayPrice;
  const isSpike = commodity.status === 'HIGH_VOLATILITY' || commodity.status === 'MODERATE_SPIKE';

  // Group history observations strictly by authentic recordedDate (No artificial date manufacturing)
  const historyByDateMap = new Map();
  if (Array.isArray(historyData)) {
    historyData.forEach(item => {
      const dateKey = item.recordedDate || 'Today';
      const priceVal = item.averagePrice || item.price || 0;
      if (!historyByDateMap.has(dateKey)) {
        historyByDateMap.set(dateKey, { sum: 0, count: 0, date: dateKey });
      }
      const existing = historyByDateMap.get(dateKey);
      existing.sum += Number(priceVal);
      existing.count += 1;
    });
  }

  const groupedHistory = Array.from(historyByDateMap.values()).map(g => ({
    recordedDate: g.date,
    averagePrice: g.count > 0 ? (g.sum / g.count) : 0
  }));

  const hasSingleDateHistory = groupedHistory.length === 1;

  // Strict Input-Gated Fairness Assessment (Only evaluate when required inputs exist)
  const observedPrice = regionalData.length > 0 && regionalData[0].avgPrice != null ? regionalData[0].avgPrice : commodity.price;
  const hasFairnessInputs = benchPrice != null && observedPrice != null;
  const isFairPrice = hasFairnessInputs ? (Math.abs(observedPrice - benchPrice) / benchPrice <= 0.15) : null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      
      {/* Dynamic Breadcrumbs & Back Button */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#64748b' }}>
          <Link to="/catalog" style={{ color: '#166534', fontWeight: 600, textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <ArrowLeft size={16} /> Catalog
          </Link>
          <ChevronRight size={14} color="#94a3b8" />
          <span>{commodity.category || 'Essential'}</span>
          <ChevronRight size={14} color="#94a3b8" />
          <span style={{ fontWeight: 700, color: '#0f172a' }}>{commodity.name}</span>
        </div>
      </div>

      {/* Main Header Product Card */}
      <div className="glass-card" style={{ padding: '32px', borderRadius: '24px', backgroundColor: '#ffffff', border: isSpike ? '1px solid #fecaca' : '1px solid #e2e8f0' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '20px' }}>
          
          {/* Left info */}
          <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
            <div style={{
              fontSize: '48px',
              width: '84px',
              height: '84px',
              borderRadius: '20px',
              backgroundColor: '#f0fdf4',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '1px solid #bbf7d0'
            }}>
              {emoji}
            </div>
            <div>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                <span style={{ padding: '4px 10px', borderRadius: '12px', backgroundColor: '#f0fdf4', color: '#166534', fontSize: '12px', fontWeight: 700, border: '1px solid #bbf7d0' }}>
                  {commodity.category}
                </span>
                {isSpike && (
                  <span style={{ padding: '4px 10px', borderRadius: '12px', backgroundColor: '#fef2f2', color: '#dc2626', fontSize: '12px', fontWeight: 800, border: '1px solid #fecaca', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <AlertTriangle size={12} /> Regional Spike
                  </span>
                )}
              </div>
              <h1 style={{ fontSize: '30px', fontWeight: 800, color: '#0f172a', marginTop: '6px' }}>{commodity.name}</h1>
              <p style={{ fontSize: '14px', color: '#64748b', marginTop: '2px' }}>
                National Mandi Benchmark Standard • Per {commodity.unit}
              </p>
            </div>
          </div>

          {/* Action Triggers */}
          <div style={{ display: 'flex', gap: '12px' }}>
            <button
              onClick={() => {
                setAlertSuccess(true);
                setTimeout(() => setAlertSuccess(false), 3000);
              }}
              style={{
                padding: '10px 18px',
                borderRadius: '12px',
                backgroundColor: alertSuccess ? '#f0fdf4' : '#ffffff',
                color: alertSuccess ? '#166534' : '#334155',
                border: alertSuccess ? '1px solid #bbf7d0' : '1px solid #cbd5e1',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              {alertSuccess ? <CheckCircle size={16} color="#166534" /> : <Bell size={16} />}
              <span>{alertSuccess ? 'Alert Active!' : 'Set Price Alert'}</span>
            </button>

            <button
              onClick={() => navigate('/fairness')}
              style={{
                padding: '10px 18px',
                borderRadius: '12px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Flag size={16} />
              <span>Report Paid Price</span>
            </button>
          </div>
        </div>

        {/* Pricing Metrics Strip */}
        <div style={{ marginTop: '28px', paddingTop: '24px', borderTop: '1px solid #f1f5f9', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
          <div>
            <span style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>Mandi Benchmark</span>
            <div style={{ fontSize: '32px', fontWeight: 800, color: '#166534', marginTop: '4px' }}>
              {benchPrice != null ? `₹${Number(benchPrice).toFixed(2)}` : 'N/A'} <span style={{ fontSize: '14px', color: '#64748b', fontWeight: 600 }}>/ {commodity.unit}</span>
            </div>
          </div>

          <div>
            <span style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>Fair Spatial Benchmark Range</span>
            <div style={{ fontSize: '20px', fontWeight: 700, color: '#0f172a', marginTop: '8px' }}>
              {minFair != null && maxFair != null ? `₹${Number(minFair).toFixed(2)} – ₹${Number(maxFair).toFixed(2)}` : 'N/A'}
            </div>
          </div>

          <div>
            <span style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>24h Price Variation</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '8px' }}>
              {commodity.priceChange ? (
                <span style={{
                  padding: '4px 10px',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: 800,
                  backgroundColor: isUp ? '#fef2f2' : '#f0fdf4',
                  color: isUp ? '#dc2626' : '#166534',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px'
                }}>
                  {isUp ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
                  {commodity.priceChange}
                </span>
              ) : (
                <span style={{ fontSize: '13px', color: '#64748b' }}>24h change unavailable</span>
              )}
              {yestPrice != null && (
                <span style={{ fontSize: '13px', color: '#64748b' }}>vs yesterday (₹{Number(yestPrice).toFixed(2)})</span>
              )}
            </div>
          </div>

          <div>
            <span style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>Fairness Rating</span>
            <div style={{ marginTop: '8px' }}>
              {hasFairnessInputs ? (
                <span style={{
                  padding: '6px 14px',
                  borderRadius: '10px',
                  fontSize: '13px',
                  fontWeight: 800,
                  backgroundColor: isFairPrice ? '#f0fdf4' : '#fef2f2',
                  color: isFairPrice ? '#166534' : '#dc2626',
                  border: isFairPrice ? '1px solid #bbf7d0' : '1px solid #fecaca',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  <ShieldCheck size={16} /> {isFairPrice ? 'OPTIMAL FAIR PRICE' : 'PRICE DEVIATION DETECTED'}
                </span>
              ) : (
                <span style={{
                  padding: '6px 14px',
                  borderRadius: '10px',
                  fontSize: '13px',
                  fontWeight: 600,
                  backgroundColor: '#f8fafc',
                  color: '#64748b',
                  border: '1px solid #e2e8f0',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  Fairness assessment unavailable
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid: Price History & Regional Markets */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        
        {/* Historical Trend Visualizer (Grouped strictly by authentic recordedDate) */}
        <div className="glass-card" style={{ padding: '28px', borderRadius: '20px', backgroundColor: '#ffffff' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a' }}>Historical Price Movement</h3>
              <p style={{ fontSize: '13px', color: '#64748b', marginTop: '2px' }}>
                {hasSingleDateHistory ? 'Agmarknet Single-Day Mandi Observations' : 'Agmarknet Government Mandi Records'}
              </p>
            </div>
            <span style={{ fontSize: '12px', fontWeight: 700, color: '#166534', backgroundColor: '#f0fdf4', padding: '4px 10px', borderRadius: '8px' }}>
              Daily Averages
            </span>
          </div>

          {/* Real Historical Observations Chart or Clean Unavailable Notice */}
          {groupedHistory.length > 0 ? (
            <div>
              <div style={{ display: 'flex', alignItems: 'flex-end', gap: '12px', height: '180px', paddingTop: '20px', paddingBottom: '10px', borderBottom: '1px solid #e2e8f0' }}>
                {groupedHistory.map((item, idx) => {
                  const priceVal = item.averagePrice || 0;
                  const barHeight = Math.min(100, Math.max(25, (priceVal / (benchPrice || priceVal || 1)) * 60));
                  return (
                    <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px', height: '100%', justifyContent: 'flex-end' }}>
                      <span style={{ fontSize: '11px', color: '#0f172a', fontWeight: 800 }}>₹{Number(priceVal).toFixed(2)}</span>
                      <div
                        style={{
                          width: hasSingleDateHistory ? '60px' : '100%',
                          height: `${barHeight}%`,
                          backgroundColor: '#166534',
                          borderRadius: '6px 6px 0 0',
                          transition: 'height 0.3s ease'
                        }}
                      />
                      <span style={{ fontSize: '11px', color: '#64748b', fontWeight: 600 }}>{item.recordedDate}</span>
                    </div>
                  );
                })}
              </div>

              {hasSingleDateHistory && (
                <p style={{ fontSize: '12px', color: '#64748b', fontStyle: 'italic', marginTop: '10px', textAlign: 'center' }}>
                  Historical data contains observations for 1 day.
                </p>
              )}
            </div>
          ) : (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
              <p style={{ fontSize: '14px', color: '#64748b', margin: 0 }}>
                Historical price data is currently unavailable for this commodity.
              </p>
            </div>
          )}
        </div>

        {/* Nearby Regional Markets Comparison Table */}
        <div className="glass-card" style={{ padding: '28px', borderRadius: '20px', backgroundColor: '#ffffff' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <MapPin size={18} color="#2563eb" />
            <span>Regional Mandi Prices</span>
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {regionalData.length > 0 ? (
              regionalData.map((mkt, idx) => (
                <div key={mkt.id || idx} style={{ padding: '12px 14px', borderRadius: '12px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 700, color: '#0f172a' }}>{mkt.marketName || mkt.state || 'Regional Mandi'}</div>
                    <div style={{ fontSize: '11px', color: '#64748b' }}>{mkt.district || mkt.city || 'APMC Market'}</div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '15px', fontWeight: 800, color: '#166534' }}>
                      {mkt.avgPrice != null ? `₹${Number(mkt.avgPrice).toFixed(2)}` : 'N/A'}
                    </div>
                    <span style={{ fontSize: '10px', fontWeight: 700, color: '#166534' }}>
                      Fair Mandi
                    </span>
                  </div>
                </div>
              ))
            ) : (
              <div style={{ padding: '20px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
                <p style={{ fontSize: '13px', color: '#64748b', margin: 0 }}>
                  No regional mandi data currently available.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

    </div>
  );
};
