import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { catalogApi } from '../api/catalogApi';
import axiosClient from '../api/axiosClient';
import { TrendingUp, Search, ArrowRight, Activity, ShieldCheck, Database } from 'lucide-react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export const PredictionsPage = () => {
  const [commodities, setCommodities] = useState([]);
  const [selectedCommodityId, setSelectedCommodityId] = useState(1);
  const [selectedTimeframe, setSelectedTimeframe] = useState('TOMORROW');
  const [forecastData, setForecastData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState(null);

  useEffect(() => {
    const loadCommodities = async () => {
      try {
        const res = await catalogApi.getCommodities();
        if (res.data && res.data.length > 0) {
          setCommodities(res.data);
          setSelectedCommodityId(res.data[0].id);
        }
      } catch (e) {
        console.error('Failed to load commodities catalog', e);
      }
    };
    loadCommodities();
  }, []);

  useEffect(() => {
    const fetchLiveForecast = async () => {
      setLoading(true);
      setErrorMsg(null);
      try {
        const response = await axiosClient.get(`/api/v1/predictions/forecast/${selectedCommodityId}`);
        if (response.data && response.data.data) {
          setForecastData(response.data.data);
        }
      } catch (e) {
        console.error('Failed to fetch live prediction forecast:', e);
        setErrorMsg('Live time-series forecast data is currently unavailable.');
      } finally {
        setLoading(false);
      }
    };

    if (selectedCommodityId) {
      fetchLiveForecast();
    }
  }, [selectedCommodityId]);

  const selectedCommodity = commodities.find(c => String(c.id) === String(selectedCommodityId));
  const basePrice = selectedCommodity?.benchmarkPrice || 40.0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>Data-Driven ML Price Forecasts</h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          Real-time time-series predictions evaluated using Walk-Forward Cross Validation & 95% Prediction Intervals.
        </p>
      </div>

      {/* Control Bar */}
      <div className="glass-card" style={{ padding: '24px', borderRadius: '16px', backgroundColor: '#ffffff', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1, minWidth: '300px' }}>
          <Search size={18} color="#166534" />
          <label style={{ fontSize: '14px', fontWeight: 700, color: '#0f172a', whiteSpace: 'nowrap' }}>
            Select Commodity:
          </label>
          <select
            value={selectedCommodityId}
            onChange={(e) => setSelectedCommodityId(e.target.value)}
            style={{
              width: '100%',
              padding: '12px 16px',
              borderRadius: '12px',
              border: '1px solid #cbd5e1',
              fontSize: '14px',
              fontWeight: 700,
              color: '#166534',
              backgroundColor: '#f0fdf4',
              outline: 'none'
            }}
          >
            {commodities.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.category}) — Benchmark: ₹{c.benchmarkPrice}
              </option>
            ))}
          </select>

          <Link
            to={`/catalog/${selectedCommodityId}`}
            style={{
              padding: '10px 14px',
              borderRadius: '10px',
              backgroundColor: '#f0fdf4',
              color: '#166534',
              border: '1px solid #bbf7d0',
              fontWeight: 700,
              fontSize: '13px',
              textDecoration: 'none',
              whiteSpace: 'nowrap',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <span>Details</span> <ArrowRight size={14} />
          </Link>
        </div>

        {/* Timeline selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '13px', fontWeight: 700, color: '#64748b', marginRight: '4px' }}>Time Horizon:</span>
          {[
            { id: 'TOMORROW', label: '📅 Tomorrow' },
            { id: 'NEXT_WEEK', label: '📅 Next Week (7d)' },
            { id: 'NEXT_MONTH', label: '📅 Next Month (30d)' }
          ].map((tf) => (
            <button
              key={tf.id}
              onClick={() => setSelectedTimeframe(tf.id)}
              style={{
                padding: '10px 16px',
                borderRadius: '10px',
                border: selectedTimeframe === tf.id ? 'none' : '1px solid #cbd5e1',
                backgroundColor: selectedTimeframe === tf.id ? '#166534' : '#ffffff',
                color: selectedTimeframe === tf.id ? '#ffffff' : '#334155',
                fontWeight: selectedTimeframe === tf.id ? 700 : 500,
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {tf.label}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
          Loading live ML time-series predictions...
        </div>
      ) : forecastData ? (
        <>
          {/* Multi-Horizon Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
            {[
              { label: 'Tomorrow Forecast', data: forecastData.tomorrow, periodKey: 'TOMORROW' },
              { label: '7-Day Forecast', data: forecastData.sevenDay, periodKey: 'NEXT_WEEK' },
              { label: '30-Day Forecast', data: forecastData.thirtyDay, periodKey: 'NEXT_MONTH' }
            ].map((card, idx) => {
              const predVal = card.data?.predictedPrice || basePrice;
              const isSelected = selectedTimeframe === card.periodKey;
              return (
                <div key={idx} className="glass-card" style={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: '#ffffff',
                  border: isSelected ? '2px solid #166534' : '1px solid #e2e8f0',
                  boxShadow: isSelected ? '0 10px 25px rgba(22, 101, 52, 0.12)' : 'none'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '12px', fontWeight: 800, color: '#64748b', textTransform: 'uppercase' }}>
                      {card.label}
                    </span>
                    <span style={{
                      fontSize: '11px',
                      fontWeight: 800,
                      padding: '2px 8px',
                      borderRadius: '6px',
                      backgroundColor: predVal >= basePrice ? '#f0fdf4' : '#fef2f2',
                      color: predVal >= basePrice ? '#166534' : '#dc2626'
                    }}>
                      {predVal >= basePrice ? '📈 +TREND' : '📉 -TREND'}
                    </span>
                  </div>

                  <div style={{ fontSize: '30px', fontWeight: 800, color: '#166534', marginTop: '12px' }}>
                    ₹{Number(predVal).toFixed(2)} / kg
                  </div>

                  <div style={{ marginTop: '10px', fontSize: '12px', color: '#64748b' }}>
                    Target Date: <strong style={{ color: '#0f172a' }}>{card.data?.targetDate || 'Tomorrow'}</strong>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #f1f5f9' }}>
                    <span style={{ fontSize: '11px', color: '#64748b' }}>Model Version</span>
                    <span style={{ fontSize: '11px', color: '#0284c7', fontWeight: 700 }}>{card.data?.modelVersion || 'v3.6-WalkForwardCV'}</span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Model Metrics & Governance Banner */}
          <div className="glass-card" style={{ padding: '24px', borderRadius: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', display: 'flex', gap: '24px', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Activity size={24} color="#166534" />
              <div>
                <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#0f172a' }}>Walk-Forward Model Selection</h4>
                <p style={{ fontSize: '12px', color: '#64748b' }}>Evaluated against Naive Baseline via rolling-origin out-of-sample folds</p>
              </div>
            </div>

            <div style={{ height: '30px', width: '1px', backgroundColor: '#cbd5e1' }} />

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <ShieldCheck size={24} color="#0284c7" />
              <div>
                <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#0f172a' }}>95% Prediction Interval</h4>
                <p style={{ fontSize: '12px', color: '#64748b' }}>Validated empirical coverage bounds on holdout observations</p>
              </div>
            </div>

            <div style={{ height: '30px', width: '1px', backgroundColor: '#cbd5e1' }} />

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Database size={24} color="#166534" />
              <div>
                <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#0f172a' }}>MongoDB + MySQL Preprocessed</h4>
                <p style={{ fontSize: '12px', color: '#64748b' }}>Deduplicated, outlier-cleaned time-series pipeline</p>
              </div>
            </div>
          </div>
        </>
      ) : (
        <div style={{ padding: '40px', textAlign: 'center', color: '#dc2626', backgroundColor: '#fef2f2', borderRadius: '12px' }}>
          {errorMsg || 'Could not load time-series price forecast.'}
        </div>
      )}
    </div>
  );
};
