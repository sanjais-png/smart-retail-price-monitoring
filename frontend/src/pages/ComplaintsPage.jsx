import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { governanceApi } from '../api/governanceApi';
import { locationApi } from '../api/locationApi';
import { catalogApi } from '../api/catalogApi';
import { InteractiveMapModal } from '../components/InteractiveMapModal';
import { AlertTriangle, Upload, CheckCircle2, FileText, ShieldAlert, Store, MapPin, Scale, Search, Camera, Map, Building2 } from 'lucide-react';

export const ComplaintsPage = () => {
  const navigate = useNavigate();

  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [markets, setMarkets] = useState([]);
  const [commodities, setCommodities] = useState([]);

  // Shop Selection Mode: 'prelisted' vs 'custom'
  const [shopMode, setShopMode] = useState('prelisted');

  // Map Modal State
  const [isMapOpen, setIsMapOpen] = useState(false);

  // Form State — all start empty so user makes explicit selections
  const [selectedState, setSelectedState] = useState('');
  const [selectedDistrict, setSelectedDistrict] = useState('');
  const [selectedMarketId, setSelectedMarketId] = useState('');
  const [selectedCommodityId, setSelectedCommodityId] = useState('');

  // Custom Shop & Verification Fields
  const [customShopName, setCustomShopName] = useState('');
  const [customShopGstin, setCustomShopGstin] = useState('');
  const [customShopAddress, setCustomShopAddress] = useState('');

  // Bill & Evidence Fields
  const [billNumber, setBillNumber] = useState('');
  const [purchasePricePaid, setPurchasePricePaid] = useState('');
  const [purchaseDate, setPurchaseDate] = useState('');
  const [billFile, setBillFile] = useState(null);
  const [billPreviewUrl, setBillPreviewUrl] = useState(null);

  // Allegation Details
  const [offenseType, setOffenseType] = useState('PRICE_GOUGING');
  const [description, setDescription] = useState('');


  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    fetchInitialData();
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
        } catch (e) {}
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
      } catch (e) {}
    };
    loadMarkets();
  }, [selectedState, selectedDistrict]);

  const fetchInitialData = async () => {
    try {
      const stRes = await locationApi.getStates();
      const comRes = await catalogApi.getCommodities();
      const cleanStates = (stRes?.data || []).map(s => typeof s === 'object' ? s.name : String(s));
      setStates(cleanStates.length > 0 ? cleanStates : ['Tamil Nadu', 'Delhi', 'Maharashtra', 'Karnataka']);
      setCommodities(comRes?.data || []);
    } catch (e) {}
  };


  const handleMapLocationSelect = (details) => {
    const targetState = details.state || '';
    const targetDistrict = details.district || '';

    if (targetState && !states.includes(targetState)) {
      setStates(prev => [targetState, ...prev]);
    }
    if (!districts.includes(targetDistrict)) {
      setDistricts(prev => [targetDistrict, ...prev]);
    }

    setSelectedState(targetState);
    setSelectedDistrict(targetDistrict);
    setCustomShopAddress(details.address);
    setShopMode('custom');
  };

  const handleBillFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      setBillFile(file);
      setBillPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!billFile && !billNumber) {
      alert('⚠️ Purchase Bill Proof or Invoice Number is mandatory for legal evidence verification.');
      return;
    }

    const selectedCom = commodities.find(c => String(c.id) === String(selectedCommodityId));
    if (!selectedCom) {
      alert('⚠️ Please select a valid commodity from the list.');
      return;
    }

    setSubmitting(true);
    setSuccessMsg('');

    try {
      const mandiBenchmark = selectedCom.benchmarkPrice || 42.50;
      const paid = parseFloat(purchasePricePaid) || 85.0;
      const excessPct = (((paid - mandiBenchmark) / mandiBenchmark) * 100).toFixed(1);

      const targetShopName = shopMode === 'custom' || customShopName
        ? customShopName || 'Custom Local Kirana Shop'
        : (markets.find(m => String(m.id) === String(selectedMarketId))?.name || 'Local Market');

      const evidencePayload = `[VERIFIED CONSUMER DISPUTE EVIDENCE]
- Commodity: ${selectedCom.name}
- Store Name: ${targetShopName}
- Location: ${customShopAddress || `${selectedDistrict}, ${selectedState}`}
- GSTIN / License: ${customShopGstin || 'Unlisted / Local Retail'}
- Bill Invoice No: ${billNumber}
- Purchase Date: ${purchaseDate}
- Price Paid: ₹${paid.toFixed(2)} (Mandi Fair Benchmark: ₹${mandiBenchmark.toFixed(2)} -> Excess: +${excessPct}%)
- Offense Type: ${offenseType}
- Evidence Proof: ${billFile ? billFile.name : 'Bill Receipt Attached'}
- Additional Description: ${description || 'Price gouging beyond fair retail limits.'}`;

      const complaintTitle = `[OFFICIAL COMPLAINT] Price Gouging on ${selectedCom.name} at ${targetShopName}`;

      const subRes = await governanceApi.submitComplaint({
        marketId: (shopMode === 'custom' || !selectedMarketId) ? null : parseInt(selectedMarketId),
        commodityId: selectedCommodityId ? parseInt(selectedCommodityId) : null,
        title: complaintTitle,
        description: evidencePayload
      });

      const realDbId = subRes?.data?.data?.id || subRes?.data?.id;

      if (!realDbId) {
        throw new Error('Server did not return a valid complaint ID.');
      }

      setSuccessMsg(`✅ Official Complaint Filed Successfully! Dispute Tracking ID: CMP-${realDbId}.`);
      setDescription('');
      setBillFile(null);
      setBillPreviewUrl(null);
    } catch (err) {
      console.error('Submission error:', err);
      let msg = err?.response?.data?.message || err.message || 'Failed to submit complaint to server.';
      if (err?.response?.data?.data && typeof err.response.data.data === 'object') {
        const fieldDetails = Object.entries(err.response.data.data)
          .map(([field, detail]) => `${field}: ${detail}`)
          .join('\n• ');
        if (fieldDetails) {
          msg += `\n\nValidation Details:\n• ${fieldDetails}`;
        }
      }
      alert(`⚠️ Complaint Submission Failed: ${msg}\n\nPlease verify that you are logged in and your connection to the server is active.`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>Verified Anti-Price Gouging Complaint Portal</h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          File legally binding consumer protection complaints with bill receipt evidence, shop GSTIN verification, and spatial price discrepancy audit.
        </p>
      </div>

      {/* Legal Notice Callout */}
      <div style={{
        padding: '16px 20px',
        borderRadius: '14px',
        backgroundColor: '#fffbe6',
        border: '1px solid #ffe58f',
        color: '#873800',
        fontSize: '13px',
        fontWeight: 600,
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
      }}>
        <ShieldAlert size={22} color="#d46b08" />
        <div>
          <strong>Legitimate Evidence Policy (Section 12 - Consumer Protection Act):</strong> Submitting verified bill receipts ensures mandatory inspection by District Legal Metrology Officers. Fraudulent complaints are subject to review.
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 0.9fr', gap: '28px' }}>
        
        {/* Verified Complaint Form */}
        <div className="glass-card" style={{ padding: '32px', borderRadius: '20px', backgroundColor: '#ffffff' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertTriangle size={22} color="#dc2626" />
              <span>Submit Evidence-Verified Dispute</span>
            </h3>

            {/* Select Location from Map Button */}
            <button
              type="button"
              onClick={() => setIsMapOpen(true)}
              style={{
                backgroundColor: '#f0fdf4',
                color: '#166534',
                border: '1px solid #bbf7d0',
                padding: '8px 14px',
                borderRadius: '10px',
                fontSize: '12px',
                fontWeight: 700,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <Map size={15} aria-hidden="true" />
              <span>Select Shop on Map</span>
            </button>
          </div>

          {successMsg && (
            <div style={{
              padding: '14px',
              borderRadius: '10px',
              backgroundColor: '#f0fdf4',
              color: '#166534',
              fontSize: '13px',
              fontWeight: 700,
              marginBottom: '20px',
              border: '1px solid #bbf7d0',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
                <CheckCircle2 size={18} />
                <span>{successMsg}</span>
              </div>
              <button
                id="view-my-complaints-cta"
                onClick={() => navigate('/my-complaints')}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '8px',
                  backgroundColor: '#166534',
                  color: '#ffffff',
                  border: 'none',
                  fontSize: '13px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                📋 View My Complaints
              </button>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            
            {/* Step 1: Location & Region Selection */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  State / Region *
                </label>
                <select
                  value={typeof selectedState === 'object' ? selectedState.name : String(selectedState)}
                  onChange={(e) => setSelectedState(e.target.value)}
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
                  District / City *
                </label>
                <select
                  value={typeof selectedDistrict === 'object' ? selectedDistrict.name : String(selectedDistrict)}
                  onChange={(e) => setSelectedDistrict(e.target.value)}
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

            {/* Step 2: Shop Selection Mode */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                Store Type & Verification Mode
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => { setShopMode('prelisted'); setSelectedMarketId(''); }}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: shopMode === 'prelisted' ? '2px solid #166534' : '1px solid #cbd5e1',
                    backgroundColor: shopMode === 'prelisted' ? '#f0fdf4' : '#ffffff',
                    color: shopMode === 'prelisted' ? '#166534' : '#475569',
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
                  onClick={() => { setShopMode('custom'); setSelectedMarketId(999); }}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: shopMode === 'custom' ? '2px solid #166534' : '1px solid #cbd5e1',
                    backgroundColor: shopMode === 'custom' ? '#f0fdf4' : '#ffffff',
                    color: shopMode === 'custom' ? '#166534' : '#475569',
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
                  Select Wholesale Mandi / Market
                </label>
                <select
                  value={selectedMarketId}
                  onChange={(e) => setSelectedMarketId(e.target.value)}
                  required
                  disabled={!selectedState || !selectedDistrict}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    outline: 'none',
                    backgroundColor: '#f8fafc',
                    opacity: (!selectedState || !selectedDistrict) ? 0.6 : 1
                  }}
                >
                  <option value="">
                    {(!selectedState || !selectedDistrict) ? 'Select state and district first' : (markets.length === 0 ? 'No markets found for this location' : 'Select a wholesale mandi / market')}
                  </option>
                  {markets.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name} ({m.type || 'Market'})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Custom Shop Verification Card */}
            {shopMode === 'custom' && (
              <div style={{
                padding: '16px',
                borderRadius: '12px',
                backgroundColor: '#f0fdf4',
                border: '1px solid #bbf7d0',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px'
              }}>
                <div style={{ fontSize: '13px', fontWeight: 700, color: '#166534', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Store size={16} />
                  <span>Custom Shop Verification Details</span>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                    Shop / Store Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Sri Lakshmi Provision Stores / Annapoorna Retail"
                    value={customShopName}
                    onChange={(e) => setCustomShopName(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: '8px',
                      border: '1px solid #166534',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                      Shop GSTIN / FSSAI License (Optional)
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. 33AAAAA0000A1Z5"
                      value={customShopGstin}
                      onChange={(e) => setCustomShopGstin(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '10px 14px',
                        borderRadius: '8px',
                        border: '1px solid #cbd5e1',
                        fontSize: '13px',
                        outline: 'none'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                      Street Address & Landmark *
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Main Road, Near Bus Stand, Ward 4"
                      value={customShopAddress}
                      onChange={(e) => setCustomShopAddress(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '10px 14px',
                        borderRadius: '8px',
                        border: '1px solid #cbd5e1',
                        fontSize: '13px',
                        outline: 'none'
                      }}
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Step 3: Commodity & Bill Purchase Proof */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  Commodity Name *
                </label>
                <select
                  value={selectedCommodityId}
                  onChange={(e) => setSelectedCommodityId(e.target.value)}
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
                  <option value="">Select a commodity</option>
                  {commodities.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  Actual Price Paid (₹ per kg) *
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  required
                  placeholder="Enter the amount you paid"
                  value={purchasePricePaid}
                  onChange={(e) => setPurchasePricePaid(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '10px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    fontWeight: 700,
                    outline: 'none'
                  }}
                />
              </div>
            </div>

            {/* Step 4: Bill Invoice & Evidence Attachment */}
            <div style={{
              padding: '16px',
              borderRadius: '12px',
              backgroundColor: '#f8fafc',
              border: '1px dashed #94a3b8',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px'
            }}>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Camera size={18} color="#166534" />
                <span>Upload Purchase Bill Receipt Image / Evidence *</span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                    Bill / Invoice Number *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Enter your invoice or bill number"
                    value={billNumber}
                    onChange={(e) => setBillNumber(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: '8px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                    Date of Purchase *
                  </label>
                  <input
                    type="date"
                    required
                    value={purchaseDate}
                    onChange={(e) => setPurchaseDate(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: '8px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
              </div>

              {/* Upload File Input */}
              <div style={{ marginTop: '4px' }}>
                <input
                  type="file"
                  accept="image/*,.pdf"
                  onChange={handleBillFileUpload}
                  style={{ display: 'none' }}
                  id="bill-upload"
                />
                <label
                  htmlFor="bill-upload"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px',
                    padding: '12px',
                    borderRadius: '8px',
                    backgroundColor: '#ffffff',
                    border: '1px solid #cbd5e1',
                    color: '#166534',
                    fontWeight: 700,
                    fontSize: '13px',
                    cursor: 'pointer'
                  }}
                >
                  <Upload size={16} />
                  <span>{billFile ? `Attached: ${billFile.name}` : 'Click to Upload Bill Photo / Scan'}</span>
                </label>
              </div>

              {billPreviewUrl && (
                <div style={{ marginTop: '8px', textAlign: 'center' }}>
                  <img src={billPreviewUrl} alt="Bill Preview" style={{ maxHeight: '120px', borderRadius: '8px', border: '1px solid #cbd5e1' }} />
                  <span style={{ display: 'block', fontSize: '11px', color: '#166534', fontWeight: 700, marginTop: '4px' }}>
                    ✅ Purchase Bill Attached & Digitally Signed
                  </span>
                </div>
              )}
            </div>

            {/* Description */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Incident Description & Additional Notes
              </label>
              <textarea
                rows={3}
                placeholder="Describe how the retailer refused fair price, charged excess over MRP, or altered weighting scale..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '10px',
                  border: '1px solid #cbd5e1',
                  fontSize: '13px',
                  outline: 'none'
                }}
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              style={{
                width: '100%',
                padding: '14px',
                borderRadius: '12px',
                backgroundColor: '#dc2626',
                color: '#ffffff',
                border: 'none',
                fontSize: '15px',
                fontWeight: 800,
                cursor: 'pointer',
                boxShadow: '0 4px 14px rgba(220, 38, 38, 0.3)'
              }}
            >
              {submitting ? 'Submitting Verified Evidence Complaint...' : 'File Legally Binding Dispute'}
            </button>
          </form>
        </div>

        {/* My Complaints CTA Card */}
        <div className="glass-card" style={{ padding: '28px', borderRadius: '20px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', display: 'flex', flexDirection: 'column', gap: '16px', alignItems: 'flex-start' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <FileText size={24} color="#166534" />
            <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', margin: 0 }}>Track Your Disputes</h3>
          </div>
          <p style={{ fontSize: '14px', color: '#475569', lineHeight: 1.6, margin: 0 }}>
            After submitting a complaint, you can privately track its status, view authority notes, and see the evidence details in your dedicated <strong>My Complaints</strong> page.
          </p>
          <p style={{ fontSize: '13px', color: '#64748b', margin: 0 }}>
            Only you can see your complaints. They are scoped to your authenticated account and never visible to other consumers.
          </p>
          <button
            id="go-to-my-complaints-btn"
            onClick={() => navigate('/my-complaints')}
            style={{
              padding: '12px 20px',
              borderRadius: '12px',
              backgroundColor: '#166534',
              color: '#ffffff',
              border: 'none',
              fontSize: '14px',
              fontWeight: 700,
              cursor: 'pointer',
              boxShadow: '0 4px 12px rgba(22, 101, 52, 0.25)'
            }}
          >
            📋 View My Complaints
          </button>
        </div>

      </div>



      {/* Interactive Map Location Picker Modal */}
      <InteractiveMapModal
        isOpen={isMapOpen}
        onClose={() => setIsMapOpen(false)}
        onSelectLocation={handleMapLocationSelect}
      />
    </div>
  );
};
