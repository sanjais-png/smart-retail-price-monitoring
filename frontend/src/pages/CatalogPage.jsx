import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { catalogApi } from '../api/catalogApi';
import { ShoppingBag, Search, TrendingUp, TrendingDown, AlertTriangle, ShieldCheck, Activity } from 'lucide-react';

export const CatalogPage = () => {
  const [categories, setCategories] = useState([]);
  const [commodities, setCommodities] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadCategories = async () => {
      const res = await catalogApi.getCategories();
      setCategories(['All', ...(res.data || [])]);
    };
    loadCategories();
  }, []);

  useEffect(() => {
    const loadCommodities = async () => {
      setLoading(true);
      const res = await catalogApi.getCommodities(selectedCategory, searchTerm);
      setCommodities(res.data || []);
      setLoading(false);
    };
    loadCommodities();
  }, [selectedCategory, searchTerm]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>National Commodity Price & Variation Catalog</h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          Explore baseline pricing, historical 24h variations, and spatial fair margins across 284+ essential consumer goods.
        </p>
      </div>

      {/* Filter Bar */}
      <div className="glass-card" style={{ padding: '20px', borderRadius: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        {/* Category Chips */}
        <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`filter-pill ${selectedCategory === cat ? 'active' : ''}`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Search */}
        <div style={{ position: 'relative', width: '280px' }}>
          <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '12px', top: '12px' }} />
          <input
            type="text"
            placeholder="Search commodity name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '10px 14px 10px 38px',
              borderRadius: '10px',
              border: '1px solid #cbd5e1',
              fontSize: '13px',
              outline: 'none'
            }}
          />
        </div>
      </div>

      {/* Commodities Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
        {commodities.map((item) => {
          const isUp = item.priceChange?.startsWith('+');
          const isHigh = item.status === 'HIGH_VOLATILITY' || item.status === 'MODERATE_SPIKE';
          const yestPrice = item.yesterdayPrice;
          const benchPrice = item.benchmarkPrice;
          const minPrice = item.minPrice;
          const maxPrice = item.maxPrice;

          return (
            <Link
              key={item.id || item.name}
              to={`/catalog/${item.id}`}
              className="glass-card"
              style={{
                padding: '24px',
                borderRadius: '20px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                backgroundColor: '#ffffff',
                border: isHigh ? '1px solid #fecaca' : '1px solid #e2e8f0',
                textDecoration: 'none',
                color: 'inherit',
                cursor: 'pointer'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{
                    padding: '4px 10px',
                    borderRadius: '12px',
                    backgroundColor: '#f0fdf4',
                    color: '#166534',
                    fontSize: '12px',
                    fontWeight: 700,
                    border: '1px solid #bbf7d0'
                  }}>
                    {item.category}
                  </span>

                  {/* 24h Variation Badge */}
                  {item.priceChange ? (
                    <span style={{
                      padding: '4px 10px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 800,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px',
                      backgroundColor: isUp ? '#fef2f2' : '#f0fdf4',
                      color: isUp ? '#dc2626' : '#166534',
                      border: isUp ? '1px solid #fecaca' : '1px solid #bbf7d0'
                    }}>
                      {isUp ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                      <span>{item.priceChange} (24h)</span>
                    </span>
                  ) : (
                    <span style={{ fontSize: '11px', color: '#94a3b8' }}>No 24h delta</span>
                  )}
                </div>

                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#0f172a', marginTop: '14px' }}>{item.name}</h3>
                <p style={{ fontSize: '13px', color: '#64748b', marginTop: '2px' }}>Unit: per {item.unit}</p>

                {/* Historical Yesterday Comparison Pill */}
                {yestPrice != null && benchPrice != null ? (
                  <div className="baseline-pill">
                    <span>Yesterday Baseline: <strong>₹{Number(yestPrice).toFixed(2)}</strong></span>
                    <span style={{ color: isUp ? '#ef4444' : '#10b981', fontWeight: 700, marginLeft: '6px' }}>
                      {isUp ? `+₹${(benchPrice - yestPrice).toFixed(2)}` : `-₹${(yestPrice - benchPrice).toFixed(2)}`}
                    </span>
                  </div>
                ) : (
                  <div className="baseline-pill">
                    <span>Yesterday Baseline: <strong>N/A</strong></span>
                  </div>
                )}
              </div>

              <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                <div>
                  <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', fontWeight: 700 }}>Mandi Benchmark</span>
                  <div style={{ fontSize: '24px', fontWeight: 800, color: '#166534' }}>
                    {benchPrice != null ? `₹${Number(benchPrice).toFixed(2)}` : 'N/A'}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', fontWeight: 700 }}>Fair Range</span>
                  <div style={{ fontSize: '13px', fontWeight: 600, color: '#334155' }}>
                    {minPrice != null && maxPrice != null ? `₹${Number(minPrice).toFixed(2)} - ₹${Number(maxPrice).toFixed(2)}` : 'N/A'}
                  </div>
                </div>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
};
