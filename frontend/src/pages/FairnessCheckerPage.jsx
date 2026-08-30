import React, { useState, useEffect } from 'react';
import { catalogApi, COMPREHENSIVE_COMMODITIES } from '../api/catalogApi';
import { locationApi } from '../api/locationApi';
import { fairnessApi } from '../api/fairnessApi';
import { InteractiveMapModal } from '../components/InteractiveMapModal';
import { useNavigate } from 'react-router-dom';
import { Scale, MapPin, Store, Navigation, AlertTriangle, ArrowRight, Building2, Map } from 'lucide-react';

const DEFAULT_APMC_MARKETS = [
  { id: 101, name: 'Koyambedu Wholesale APMC Mandi', district: 'Chennai', state: 'Tamil Nadu' },
  { id: 102, name: 'MGR Central APMC Vegetable Market', district: 'Chennai', state: 'Tamil Nadu' },
  { id: 103, name: 'MGR Wholesale APMC Market', district: 'Coimbatore', state: 'Tamil Nadu' },
  { id: 104, name: 'Singanallur Farmers APMC Market', district: 'Coimbatore', state: 'Tamil Nadu' },
  { id: 105, name: 'Azadpur APMC Wholesale Mandi', district: 'Delhi', state: 'Delhi NCR' },
  { id: 106, name: 'Vashi APMC Agricultural Market', district: 'Mumbai', state: 'Maharashtra' },
  { id: 107, name: 'Yeshwantpur APMC Yard', district: 'Bangalore', state: 'Karnataka' },
  { id: 108, name: 'Gudimalkapur APMC Wholesale Market', district: 'Hyderabad', state: 'Telangana' }
];

export const FairnessCheckerPage = () => {
  const [commodities, setCommodities] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [markets, setMarkets] = useState(DEFAULT_APMC_MARKETS);

  // Mode: 'prelisted' vs 'custom'
  const [shopMode, setShopMode] = useState('prelisted');

  // Map Modal state
  const [isMapOpen, setIsMapOpen] = useState(false);

  // Selection states — start empty so user makes explicit choices
  const [selectedCommodity, setSelectedCommodity] = useState('');
  const [selectedState, setSelectedState] = useState('');
  const [selectedDistrict, setSelectedDistrict] = useState('');
  const [selectedMarketId, setSelectedMarketId] = useState('');

  // Custom shop location details
  const [customShopName, setCustomShopName] = useState('');
  const [customAddress, setCustomAddress] = useState('');

  const [userPrice, setUserPrice] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [locMessage, setLocMessage] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    const initData = async () => {
      try {
        const comRes = await catalogApi.getCommodities();
        const stRes = await locationApi.getStates();
        
        const rawCommodities = comRes?.data || [];
        const rawStates = stRes?.data || [];

        const comList = Array.isArray(rawCommodities) && rawCommodities.length > 0 ? rawCommodities : COMPREHENSIVE_COMMODITIES;
        setCommodities(comList);

        const cleanStates = (Array.isArray(rawStates) ? rawStates : []).map(s => typeof s === 'object' ? s.name : String(s));
        setStates(cleanStates.length > 0 ? cleanStates : ['Tamil Nadu', 'Delhi', 'Maharashtra', 'Karnataka', 'Telangana', 'Kerala']);
      } catch (e) {
        setCommodities(COMPREHENSIVE_COMMODITIES);
      }
    };
    initData();
  }, []);

  useEffect(() => {
    const loadDistricts = async () => {
      if (selectedState) {
        try {
          const stateStr = typeof selectedState === 'object' ? selectedState.name : String(selectedState);
          const distRes = await locationApi.getDistrictsByState(stateStr);
          let distList = (distRes?.data || []).map(d => typeof d === 'object' ? d.name : String(d));

          if (selectedDistrict && !distList.includes(selectedDistrict)) {
            distList = [selectedDistrict, ...distList];
          }

          setDistricts(distList);
        } catch (e) {
          console.error(e);
        }
      }
    };
    loadDistricts();
  }, [selectedState, selectedDistrict]);

  useEffect(() => {
    const loadMarkets = async () => {
      // Only load markets once both state and district are selected
      if (!selectedState || !selectedDistrict) {
        setMarkets([]);
        return;
      }
      try {
        const stateStr = typeof selectedState === 'object' ? selectedState.name : String(selectedState);
        const distStr = typeof selectedDistrict === 'object' ? selectedDistrict.name : String(selectedDistrict);
        const mktRes = await locationApi.getMarkets(stateStr, distStr);
        const mktList = Array.isArray(mktRes?.data) ? mktRes.data : [];
        setMarkets(mktList);
        // Reset market selection when location changes — user must re-select
        setSelectedMarketId('');
      } catch (e) {
        setMarkets([]);
      }
    };
    loadMarkets();
  }, [selectedState, selectedDistrict]);

  // Handlers
  const handleCommodityChange = (val) => {
    setSelectedCommodity(Number(val));
    setResult(null);
  };

  const handleStateChange = (val) => {
    setSelectedState(val);
    setResult(null);
  };

  const handleDistrictChange = (val) => {
    setSelectedDistrict(val);
    setResult(null);
  };

  const handleSelectShopMode = (mode) => {
    setShopMode(mode);
    setResult(null);
    // Reset market selection when switching modes — user must re-select
    setSelectedMarketId('');
  };

  const handleMapLocationSelect = (details) => {
    const targetState = details.state || '';
    const targetDistrict = details.district || '';

    if (targetState && !states.includes(targetState)) {
      setStates(prev => [targetState, ...prev]);
    }
    if (targetDistrict && !districts.includes(targetDistrict)) {
      setDistricts(prev => [targetDistrict, ...prev]);
    }

    setSelectedState(targetState);
    setSelectedDistrict(targetDistrict);
    setCustomAddress(details.address);
    setLocMessage(`📍 Selected on Map: ${targetDistrict}, ${targetState}`);
    setResult(null);
  };

  const handleAutoDetectLocation = async () => {
    setLocating(true);
    setLocMessage('');

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=10`)
              .then(r => r.json()).catch(() => null);

            let stateName = 'Tamil Nadu';
            let cityName = 'Coimbatore';

            if (res && res.address) {
              const a = res.address;
              stateName = a.state || stateName;
              cityName = a.city || a.town || a.county || a.state_district || a.subdistrict || cityName;
            }

            if (!states.includes(stateName)) setStates(prev => [stateName, ...prev]);
            if (!districts.includes(cityName)) setDistricts(prev => [cityName, ...prev]);

            setSelectedState(stateName);
            setSelectedDistrict(cityName);
            setCustomAddress(`📍 GPS (${lat.toFixed(4)}, ${lon.toFixed(4)}) - ${cityName}, ${stateName}`);
            setLocMessage(`📍 GPS Location set to ${cityName}, ${stateName}`);
            setResult(null);
          } catch (err) {
            setSelectedState('Tamil Nadu');
            setSelectedDistrict('');
            setLocMessage('⚠️ Could not detect precise location. Please select your state and district manually.');
          } finally {
            setLocating(false);
          }
        },
        () => {
          setSelectedState('Tamil Nadu');
          setSelectedDistrict('Coimbatore');
          setLocMessage('📍 Location set to Coimbatore, Tamil Nadu.');
          setLocating(false);
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    } else {
      setSelectedState('Tamil Nadu');
      setSelectedDistrict('Coimbatore');
      setLocMessage('📍 Location set to Coimbatore, Tamil Nadu.');
      setLocating(false);
    }
  };

  const handleEvaluate = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    try {
      const stateStr = typeof selectedState === 'object' ? selectedState.name : String(selectedState);
      const distStr = typeof selectedDistrict === 'object' ? selectedDistrict.name : String(selectedDistrict);
      
      const selectedComObj = commodities.find(c => String(c.id) === String(selectedCommodity));

      const res = await fairnessApi.evaluatePrice(
        selectedCommodity,
        shopMode === 'custom' ? null : selectedMarketId,
        parseFloat(userPrice) || 40.0,
        shopMode === 'custom' ? customShopName : '',
        distStr,
        stateStr,
        selectedComObj
      );
      
      const selectedMkt = markets.find(m => String(m.id) === String(selectedMarketId));
      const evalData = { ...(res?.data || {}) };

      if (shopMode === 'custom' && customShopName) {
        evalData.marketName = customShopName;
        evalData.locationDetails = `${customAddress || 'Neighborhood Area'}, ${distStr}, ${stateStr}`;
      } else if (selectedMkt) {
        evalData.marketName = selectedMkt.name;
        evalData.locationDetails = `${selectedMkt.district || distStr}, ${selectedMkt.state || stateStr}`;
      } else {
        evalData.marketName = `${distStr} Wholesale APMC Mandi`;
        evalData.locationDetails = `${distStr}, ${stateStr}`;
      }

      setResult(evalData);
    } catch (err) {
      console.error('Evaluate error', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>Spatial Price Fairness Calculator</h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          Evaluate any retail price paid at wholesale APMC mandis, local markets, neighborhood kirana stores, or unlisted shops.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 0.9fr', gap: '28px' }}>
        
        {/* Form Card */}
        <div className="glass-card" style={{ padding: '32px', borderRadius: '20px', backgroundColor: '#ffffff' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '10px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Scale size={22} color="#166534" />
              <span>Purchase & Location Details</span>
            </h3>

            {/* Map & Auto-Detect Location Buttons */}
            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                type="button"
                onClick={() => setIsMapOpen(true)}
                style={{
                  backgroundColor: '#f0fdf4',
                  color: '#166534',
                  border: '1px solid #bbf7d0',
                  padding: '8px 12px',
                  borderRadius: '10px',
                  fontSize: '12px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Map size={14} aria-hidden="true" />
                <span>Pick on Map</span>
              </button>

              <button
                type="button"
                onClick={handleAutoDetectLocation}
                disabled={locating}
                style={{
                  backgroundColor: '#f0fdf4',
                  color: '#166534',
                  border: '1px solid #bbf7d0',
                  padding: '8px 12px',
                  borderRadius: '10px',
                  fontSize: '12px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Navigation size={14} />
                <span>{locating ? 'Detecting...' : 'Auto GPS'}</span>
              </button>
            </div>
          </div>

          {locMessage && (
            <div style={{
              padding: '10px 14px',
              borderRadius: '8px',
              backgroundColor: '#f0fdf4',
              color: '#166534',
              fontSize: '12px',
              fontWeight: 600,
              marginBottom: '16px',
              border: '1px solid #bbf7d0'
            }}>
              {locMessage}
            </div>
          )}

          <form onSubmit={handleEvaluate} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            
            {/* Commodity Selector */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Commodity Name
              </label>
              <select
                value={selectedCommodity}
                onChange={(e) => handleCommodityChange(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: '12px',
                  border: '1px solid #cbd5e1',
                  fontSize: '14px',
                  outline: 'none',
                  backgroundColor: '#f8fafc'
                }}
              >
                <option value="">Select a commodity</option>
                {commodities.map((c) => (
                  <option key={c.id} value={c.id}>
                    {typeof c.name === 'string' ? c.name : 'Commodity'} ({typeof c.category === 'string' ? c.category : 'General'})
                  </option>
                ))}
              </select>
            </div>

            {/* Region / State & District Selectors */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  State / Region
                </label>
                <select
                  value={typeof selectedState === 'object' ? selectedState.name : String(selectedState)}
                  onChange={(e) => handleStateChange(e.target.value)}
                  required
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '10px',
                    border: '1px solid #cbd5e1',
                    fontSize: '13px',
                    outline: 'none'
                  }}
                >
                  <option value="">Select state or region</option>
                  {states.map((s, idx) => {
                    const val = typeof s === 'object' ? s.name : String(s);
                    return <option key={idx} value={val}>{val}</option>;
                  })}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  District / City
                </label>
                <select
                  value={typeof selectedDistrict === 'object' ? selectedDistrict.name : String(selectedDistrict)}
                  onChange={(e) => handleDistrictChange(e.target.value)}
                  required
                  disabled={!selectedState}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '10px',
                    border: '1px solid #cbd5e1',
                    fontSize: '13px',
                    outline: 'none',
                    opacity: !selectedState ? 0.6 : 1
                  }}
                >
                  <option value="">{selectedState ? 'Select district or city' : 'Select state first'}</option>
                  {districts.map((d, idx) => {
                    const val = typeof d === 'object' ? d.name : String(d);
                    return <option key={idx} value={val}>{val}</option>;
                  })}
                </select>
              </div>
            </div>

            {/* 2-Tab Store Mode Switcher */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Store Selection Mode
              </label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  type="button"
                  onClick={() => handleSelectShopMode('prelisted')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: shopMode === 'prelisted' ? 'none' : '1px solid #cbd5e1',
                    backgroundColor: shopMode === 'prelisted' ? '#166534' : '#f8fafc',
                    color: shopMode === 'prelisted' ? '#ffffff' : '#334155',
                    fontWeight: 700,
                    fontSize: '13px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '6px'
                  }}
                >
                  <Building2 size={16} aria-hidden="true" />
                  <span>Pre-listed APMC Mandi</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleSelectShopMode('custom')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: shopMode === 'custom' ? 'none' : '1px solid #cbd5e1',
                    backgroundColor: shopMode === 'custom' ? '#166534' : '#f8fafc',
                    color: shopMode === 'custom' ? '#ffffff' : '#334155',
                    fontWeight: 700,
                    fontSize: '13px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '6px'
                  }}
                >
                  <Store size={16} aria-hidden="true" />
                  <span>Custom Retail Shop / Kirana</span>
                </button>
              </div>
            </div>

            {/* Pre-listed Market Dropdown */}
            {shopMode === 'prelisted' && (
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  Select Wholesale APMC Mandi
                </label>
                <select
                  value={selectedMarketId}
                  onChange={(e) => { setSelectedMarketId(e.target.value); setResult(null); }}
                  required
                  disabled={!selectedState || !selectedDistrict}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '10px',
                    border: '1px solid #cbd5e1',
                    fontSize: '13px',
                    outline: 'none',
                    opacity: (!selectedState || !selectedDistrict) ? 0.6 : 1
                  }}
                >
                  <option value="">
                    {(!selectedState || !selectedDistrict) ? 'Select state and district first' : (markets.length === 0 ? 'No markets found for this location' : 'Select a wholesale mandi')}
                  </option>
                  {markets.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name} ({m.district || selectedDistrict})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Custom Shop Entry Fields */}
            {shopMode === 'custom' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', padding: '14px', backgroundColor: '#f0fdf4', borderRadius: '12px', border: '1px solid #bbf7d0' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#166534', marginBottom: '4px' }}>
                    Custom Retail Shop / Supermarket Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Sri Lakshmi Supermarket / Local Corner Store"
                    value={customShopName}
                    onChange={(e) => { setCustomShopName(e.target.value); setResult(null); }}
                    style={{
                      width: '100%',
                      padding: '10px',
                      borderRadius: '8px',
                      border: '1px solid #bbf7d0',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#166534', marginBottom: '4px' }}>
                    Landmark / Street Address (Optional)
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Near Bus Stand, Main Road"
                    value={customAddress}
                    onChange={(e) => setCustomAddress(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px',
                      borderRadius: '8px',
                      border: '1px solid #bbf7d0',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
              </div>
            )}

            {/* Purchase Price Input */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Retail Purchase Price Paid (₹ / unit)
              </label>
              <input
                type="number"
                step="0.5"
                min="0"
                required
                placeholder="Enter the price you paid"
                value={userPrice}
                onChange={(e) => { setUserPrice(e.target.value); setResult(null); }}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '12px',
                  border: '1px solid #cbd5e1',
                  fontSize: '16px',
                  fontWeight: 700,
                  outline: 'none'
                }}
              />
            </div>

            <button
              type="submit"
              className="btn-emerald"
              disabled={loading}
              style={{
                width: '100%',
                padding: '14px',
                borderRadius: '12px',
                fontSize: '15px',
                marginTop: '6px',
                cursor: 'pointer'
              }}
            >
              {loading ? 'Evaluating Spatial Benchmark...' : 'Evaluate Retail Price Fairness'}
            </button>
          </form>
        </div>

        {/* Results Card */}
        <div>
          {result ? (
            <div className="glass-card" style={{ padding: '32px', borderRadius: '20px', backgroundColor: '#ffffff', display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Evaluation Results</h3>
                <span style={{
                  padding: '6px 14px',
                  borderRadius: '20px',
                  backgroundColor: `${result.statusColor}15`,
                  color: result.statusColor,
                  fontWeight: 800,
                  fontSize: '12px',
                  border: `1px solid ${result.statusColor}40`
                }}>
                  {result.status}
                </span>
              </div>

              {/* Score Meter */}
              <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8fafc', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '12px', color: '#64748b', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Spatial Fairness Index Score
                </div>
                <div style={{ fontSize: '48px', fontWeight: 800, color: result.statusColor, margin: '8px 0' }}>
                  {result.fairnessScore} <span style={{ fontSize: '18px', color: '#94a3b8' }}>/ 100</span>
                </div>
                <div style={{ fontSize: '13px', color: '#475569', lineHeight: 1.5 }}>
                  {result.message}
                </div>
              </div>

              {/* Detail Metrics Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '13px' }}>
                <div style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ color: '#64748b', fontSize: '11px', display: 'block' }}>Commodity</span>
                  <strong style={{ color: '#166534', fontSize: '14px' }}>{result.commodityName || 'Selected Item'}</strong>
                </div>
                <div style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ color: '#64748b', fontSize: '11px', display: 'block' }}>Store & Location</span>
                  <strong style={{ color: '#0f172a', fontSize: '13px' }}>{result.marketName}</strong>
                </div>
                <div style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ color: '#64748b', fontSize: '11px', display: 'block' }}>Reported Purchase Price</span>
                  <strong style={{ color: '#0f172a', fontSize: '15px' }}>₹{result.userPrice?.toFixed(2)}</strong>
                </div>
                <div style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ color: '#64748b', fontSize: '11px', display: 'block' }}>Regional APMC Benchmark</span>
                  <strong style={{ color: '#166534', fontSize: '15px' }}>₹{result.benchmarkPrice?.toFixed(2)}</strong>
                </div>
              </div>

              {/* Fair Range Bar */}
              <div style={{ padding: '14px', borderRadius: '12px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', fontWeight: 700, color: '#166534', marginBottom: '6px' }}>
                  <span>Fair Spatial Retail Ceiling (+15%)</span>
                  <span>₹{result.fairPriceRange?.min} – ₹{result.fairPriceRange?.max}</span>
                </div>
              </div>

              {/* Action Button */}
              {result.status === 'VERY_HIGH' || result.status === 'HIGH' ? (
                <button
                  onClick={() => navigate('/complaints')}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '10px',
                    backgroundColor: '#dc2626',
                    color: '#ffffff',
                    border: 'none',
                    fontWeight: 700,
                    fontSize: '13px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px'
                  }}
                >
                  <AlertTriangle size={16} />
                  <span>Report Overcharging Store with Bill Evidence</span>
                </button>
              ) : null}
            </div>
          ) : (
            <div className="glass-card" style={{ padding: '48px 32px', borderRadius: '20px', backgroundColor: '#ffffff', textAlign: 'center' }}>
              <div style={{
                width: '64px',
                height: '64px',
                borderRadius: '50%',
                backgroundColor: '#f0fdf4',
                color: '#166534',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 16px'
              }}>
                <Scale size={32} />
              </div>
              <h4 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a' }}>Fairness Evaluation Dashboard</h4>
              <p style={{ fontSize: '13px', color: '#64748b', marginTop: '6px', maxWidth: '300px', margin: '6px auto 0' }}>
                Select your commodity, city, and purchase price on the left, then click <strong>Evaluate Retail Price Fairness</strong>.
              </p>
            </div>
          )}
        </div>

      </div>

      {/* Interactive Map Selector Modal */}
      <InteractiveMapModal
        isOpen={isMapOpen}
        onClose={() => setIsMapOpen(false)}
        onSelectLocation={handleMapLocationSelect}
      />
    </div>
  );
};
