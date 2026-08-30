import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import { LocationMapModal } from '../components/LocationMapModal';
import {
  User, Key, Shield, CheckCircle2, AlertCircle, Save, Phone, Mail,
  MapPin, Lock, Camera, ClipboardList, Navigation, Map, X, Check, Loader2
} from 'lucide-react';

export const ProfilePage = () => {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();

  // Profile Form State
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // Editable Profile Fields
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState(null);
  const [longitude, setLongitude] = useState(null);

  // Validation & Error States
  const [phoneError, setPhoneError] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileError, setProfileError] = useState('');

  // Geolocation & Map Modal State
  const [isLocating, setIsLocating] = useState(false);
  const [locationPreview, setLocationPreview] = useState(null);
  const [isMapOpen, setIsMapOpen] = useState(false);

  // Password Modal & Staged Workflow State
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [passwordStep, setPasswordStep] = useState(1); // 1: Verify Current, 2: New Password
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [verifyingPassword, setVerifyingPassword] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  // Fetch initial profile from backend on mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    setLoading(true);
    const res = await userApi.getMyProfile();
    if (res.success && res.data) {
      const p = res.data;
      setProfileData(p);
      setFullName(p.firstName || p.username || '');
      setEmail(p.email || '');
      setPhone(p.phone || '');
      setAddress(p.address || '');
      setLatitude(p.latitude);
      setLongitude(p.longitude);

      // Validate legacy phone if present
      if (p.phone && !/^[6-9]\d{9}$/.test(p.phone)) {
        setPhoneError('Please enter a valid 10-digit mobile number.');
      } else {
        setPhoneError('');
      }
    }
    setLoading(false);
  };

  // Indian 10-digit Mobile Regex Validation
  const validatePhone = (val) => {
    if (!val || val.trim() === '') {
      setPhoneError('');
      return true;
    }
    const indianPhoneRegex = /^[6-9]\d{9}$/;
    if (!indianPhoneRegex.test(val)) {
      setPhoneError('Please enter a valid 10-digit mobile number.');
      return false;
    }
    setPhoneError('');
    return true;
  };

  const handlePhoneChange = (e) => {
    const val = e.target.value;
    setPhone(val);
    if (phoneError) {
      setPhoneError('');
    }
  };

  // 📍 Use Current Location (Browser Geolocation API + Nominatim Reverse Geocoding)
  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }

    setIsLocating(true);
    setProfileError('');

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = Number(pos.coords.latitude.toFixed(6));
        const lon = Number(pos.coords.longitude.toFixed(6));

        try {
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
          const data = await res.json();
          const resolvedAddr = data.display_name || `Location (${lat}, ${lon})`;

          setLocationPreview({
            address: resolvedAddr,
            latitude: lat,
            longitude: lon
          });
        } catch (err) {
          setLocationPreview({
            address: `Current Location (${lat}, ${lon})`,
            latitude: lat,
            longitude: lon
          });
        } finally {
          setIsLocating(false);
        }
      },
      (error) => {
        setIsLocating(false);
        if (error.code === error.PERMISSION_DENIED) {
          setProfileError('Location access was not granted. You can choose your location manually from the map.');
        } else {
          setProfileError('Unable to determine location. Please select your location from the map.');
        }
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  // User confirms location preview
  const handleConfirmLocationPreview = () => {
    if (locationPreview) {
      setAddress(locationPreview.address);
      setLatitude(locationPreview.latitude);
      setLongitude(locationPreview.longitude);
      setLocationPreview(null);
      setProfileSuccess('Location updated! Click "Save Personal Details" to persist to database.');
      setTimeout(() => setProfileSuccess(''), 4000);
    }
  };

  // Selected from Leaflet map modal
  const handleSelectFromMap = (selected) => {
    setAddress(selected.address);
    setLatitude(selected.latitude);
    setLongitude(selected.longitude);
    setProfileSuccess('Map location selected! Click "Save Personal Details" to persist to database.');
    setTimeout(() => setProfileSuccess(''), 4000);
  };

  // Save Personal Profile Details to Backend MySQL
  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setProfileError('');
    setProfileSuccess('');

    if (phone && !validatePhone(phone)) {
      setProfileError('Please enter a valid 10-digit mobile number starting with 6, 7, 8, or 9.');
      return;
    }

    setSaving(true);
    const updatePayload = {
      firstName: fullName,
      phone: phone,
      address: address,
      latitude: latitude,
      longitude: longitude
    };

    const res = await userApi.updateProfile(updatePayload);
    setSaving(false);

    if (res.success && res.data) {
      setProfileSuccess('✅ Personal information updated successfully!');
      loadProfile();
      setTimeout(() => setProfileSuccess(''), 4000);
    } else {
      setProfileError(res.message || 'Unable to update profile. Please check requirements.');
    }
  };

  // Password Verification (Step 1)
  const handleVerifyCurrentPassword = async (e) => {
    e.preventDefault();
    setPasswordError('');
    if (!currentPassword) {
      setPasswordError('Please enter your current password.');
      return;
    }

    setVerifyingPassword(true);
    const res = await userApi.verifyPassword(currentPassword);
    setVerifyingPassword(false);

    if (res.success) {
      setPasswordStep(2);
      setPasswordError('');
    } else {
      setPasswordError(res.message || 'Current password is incorrect.');
    }
  };

  // Password Update (Step 2)
  const handleUpdatePassword = async (e) => {
    e.preventDefault();
    setPasswordError('');

    if (newPassword.length < 6) {
      setPasswordError('New password must be at least 6 characters long.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError('New password and confirmation password do not match.');
      return;
    }

    setChangingPassword(true);
    const res = await userApi.changePassword(currentPassword, newPassword);
    setChangingPassword(false);

    if (res.success) {
      setPasswordSuccess('✅ Password updated successfully! Please log in again with your new password.');
      setTimeout(() => {
        setIsPasswordModalOpen(false);
        setPasswordStep(1);
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
        logout();
        navigate('/auth');
      }, 2500);
    } else {
      setPasswordError(res.message || 'Failed to update password.');
    }
  };

  if (loading) {
    return <div style={{ padding: '60px', textAlign: 'center', color: '#64748b' }}>Loading user profile...</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      <div>
        <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>Account Profile & Security Settings</h2>
        <p style={{ fontSize: '15px', color: '#64748b', marginTop: '4px' }}>
          Manage your personal details, location preferences, and password security.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '0.8fr 1.2fr', gap: '28px' }}>
        
        {/* Left Column: User Summary Card */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          <div className="glass-card" style={{ padding: '32px', borderRadius: '20px', backgroundColor: '#ffffff', textAlign: 'center' }}>
            <div style={{ position: 'relative', width: '90px', height: '90px', margin: '0 auto 16px' }}>
              <div style={{
                width: '100%',
                height: '100%',
                borderRadius: '50%',
                backgroundColor: '#166534',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '36px',
                fontWeight: 800,
                boxShadow: '0 8px 20px rgba(22, 101, 52, 0.3)'
              }}>
                {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
              </div>
            </div>

            <h3 style={{ fontSize: '22px', fontWeight: 800, color: '#0f172a' }}>{fullName || user?.username}</h3>
            <p style={{ fontSize: '13px', color: '#64748b', marginTop: '2px' }}>@{user?.username || 'user'}</p>

            <span style={{
              display: 'inline-block',
              marginTop: '12px',
              padding: '6px 16px',
              borderRadius: '20px',
              backgroundColor: '#f0fdf4',
              color: '#166534',
              fontSize: '12px',
              fontWeight: 800,
              border: '1px solid #bbf7d0'
            }}>
              {user?.role || 'ROLE_USER'}
            </span>

            {/* Account Details Summary */}
            <div style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid #f1f5f9', textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '13px', color: '#475569' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Mail size={16} color="#166534" />
                <span>{email || 'No email specified'}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Phone size={16} color="#166534" />
                <span>{phone || 'No mobile number set'}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <MapPin size={16} color="#166534" style={{ marginTop: '2px', flexShrink: 0 }} />
                <span>{address || 'No location address set'}</span>
              </div>
              {latitude && longitude && (
                <div style={{ fontSize: '11px', color: '#166534', fontFamily: 'monospace', marginLeft: '26px' }}>
                  Coordinates: {latitude}, {longitude}
                </div>
              )}
            </div>
          </div>

          {/* Security Action Card */}
          <div className="glass-card" style={{ padding: '24px', borderRadius: '20px', backgroundColor: '#ffffff', display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Shield size={20} color="#166534" />
              <span style={{ fontSize: '16px', fontWeight: 800, color: '#0f172a' }}>Security & Password</span>
            </div>
            <p style={{ fontSize: '13px', color: '#64748b', margin: 0, lineHeight: 1.5 }}>
              Protect your account by updating your password through a secure 2-step verification workflow.
            </p>
            <button
              onClick={() => {
                setIsPasswordModalOpen(true);
                setPasswordStep(1);
                setPasswordError('');
                setPasswordSuccess('');
              }}
              style={{
                padding: '12px 20px',
                borderRadius: '12px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: '14px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px'
              }}
            >
              <Key size={16} />
              Change Password
            </button>
          </div>
        </div>

        {/* Right Column: Update Personal Details Form */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          
          <div className="glass-card" style={{ padding: '32px', borderRadius: '20px', backgroundColor: '#ffffff' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <User size={20} color="#166534" />
              <span>Personal Details & Location Settings</span>
            </h3>

            {profileSuccess && (
              <div style={{ padding: '14px', borderRadius: '12px', backgroundColor: '#f0fdf4', color: '#166534', fontSize: '13px', fontWeight: 700, marginBottom: '20px', border: '1px solid #bbf7d0' }}>
                {profileSuccess}
              </div>
            )}

            {profileError && (
              <div style={{ padding: '14px', borderRadius: '12px', backgroundColor: '#fef2f2', color: '#dc2626', fontSize: '13px', fontWeight: 700, marginBottom: '20px', border: '1px solid #fecaca' }}>
                {profileError}
              </div>
            )}

            <form onSubmit={handleSaveProfile} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                    Full Display Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Enter your display name"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
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
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                    Username (Fixed)
                  </label>
                  <input
                    type="text"
                    disabled
                    value={user?.username || 'user'}
                    style={{
                      width: '100%',
                      padding: '12px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      backgroundColor: '#f1f5f9',
                      color: '#94a3b8',
                      outline: 'none'
                    }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                    Email Address (Fixed)
                  </label>
                  <input
                    type="email"
                    disabled
                    value={email}
                    style={{
                      width: '100%',
                      padding: '12px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      backgroundColor: '#f1f5f9',
                      color: '#94a3b8',
                      outline: 'none'
                    }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                    Mobile Number (10 Digits)
                  </label>
                  <input
                    type="tel"
                    inputMode="numeric"
                    maxLength={10}
                    placeholder="Enter your 10-digit mobile number"
                    value={phone}
                    onChange={handlePhoneChange}
                    onBlur={() => validatePhone(phone)}
                    style={{
                      width: '100%',
                      padding: '12px',
                      borderRadius: '10px',
                      border: phoneError ? '1px solid #ef4444' : '1px solid #cbd5e1',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                  {phoneError && (
                    <span style={{ fontSize: '12px', color: '#dc2626', fontWeight: 600, marginTop: '4px', display: 'block' }}>
                      {phoneError}
                    </span>
                  )}
                </div>
              </div>

              {/* Location Controls Section */}
              <div style={{ paddingTop: '16px', borderTop: '1px solid #f1f5f9', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <label style={{ fontSize: '14px', fontWeight: 800, color: '#0f172a' }}>
                  Default City / Location Address
                </label>

                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    onClick={handleUseCurrentLocation}
                    disabled={isLocating}
                    style={{
                      padding: '10px 18px',
                      borderRadius: '10px',
                      backgroundColor: '#f0fdf4',
                      color: '#166534',
                      border: '1px solid #bbf7d0',
                      fontSize: '13px',
                      fontWeight: 700,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    <Navigation size={15} />
                    {isLocating ? 'Determining Location...' : 'Use Current Location'}
                  </button>

                  <button
                    type="button"
                    onClick={() => setIsMapOpen(true)}
                    style={{
                      padding: '10px 18px',
                      borderRadius: '10px',
                      backgroundColor: '#ffffff',
                      color: '#0f172a',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      fontWeight: 700,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    <Map size={15} color="#166534" />
                    Choose From Map
                  </button>
                </div>

                {/* Location Preview Box */}
                {locationPreview && (
                  <div style={{
                    padding: '16px',
                    borderRadius: '12px',
                    backgroundColor: '#eff6ff',
                    border: '1px solid #bfdbfe',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '10px'
                  }}>
                    <div style={{ fontSize: '12px', fontWeight: 800, color: '#1e40af', textTransform: 'uppercase' }}>
                      📍 Geolocation Preview (Requires Confirmation)
                    </div>
                    <div style={{ fontSize: '13px', color: '#1e3a8a', fontWeight: 600 }}>
                      {locationPreview.address}
                    </div>
                    <div style={{ fontSize: '11px', color: '#2563eb', fontFamily: 'monospace' }}>
                      Coordinates: {locationPreview.latitude}, {locationPreview.longitude}
                    </div>
                    <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
                      <button
                        type="button"
                        onClick={handleConfirmLocationPreview}
                        style={{
                          padding: '6px 14px',
                          borderRadius: '8px',
                          backgroundColor: '#1d4ed8',
                          color: '#ffffff',
                          border: 'none',
                          fontWeight: 700,
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        Confirm & Apply Location
                      </button>
                      <button
                        type="button"
                        onClick={() => setLocationPreview(null)}
                        style={{
                          padding: '6px 14px',
                          borderRadius: '8px',
                          backgroundColor: '#ffffff',
                          color: '#1e40af',
                          border: '1px solid #bfdbfe',
                          fontWeight: 700,
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}

                {/* Current Selected Location Input */}
                <div>
                  <input
                    type="text"
                    placeholder="Search for an address or choose a location"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '12px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                  {latitude && longitude && (
                    <div style={{ fontSize: '12px', color: '#166534', marginTop: '6px', fontFamily: 'monospace' }}>
                      Selected Coordinates: Lat {latitude}, Lon {longitude}
                    </div>
                  )}
                </div>
              </div>

              <button
                type="submit"
                disabled={saving || Boolean(phoneError)}
                style={{
                  alignSelf: 'flex-start',
                  padding: '12px 28px',
                  borderRadius: '10px',
                  backgroundColor: phoneError ? '#cbd5e1' : '#166534',
                  color: '#ffffff',
                  border: 'none',
                  fontSize: '14px',
                  fontWeight: 700,
                  cursor: phoneError ? 'not-allowed' : 'pointer',
                  marginTop: '8px'
                }}
              >
                {saving ? 'Saving...' : 'Save Personal Details'}
              </button>
            </form>
          </div>

        </div>

      </div>

      {/* Leaflet Map Location Picker Modal */}
      <LocationMapModal
        isOpen={isMapOpen}
        onClose={() => setIsMapOpen(false)}
        initialLat={latitude}
        initialLng={longitude}
        initialAddress={address}
        onSelectLocation={handleSelectFromMap}
      />

      {/* Staged Change Password Modal */}
      {isPasswordModalOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.65)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 2000,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: '20px',
            maxWidth: '480px',
            width: '100%',
            padding: '32px',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Key size={22} color="#166534" />
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#0f172a' }}>Change Password</h3>
              </div>
              <button
                onClick={() => setIsPasswordModalOpen(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}
              >
                <X size={20} />
              </button>
            </div>

            {passwordSuccess && (
              <div style={{ padding: '14px', borderRadius: '10px', backgroundColor: '#f0fdf4', color: '#166534', fontSize: '13px', fontWeight: 700, marginBottom: '20px', border: '1px solid #bbf7d0' }}>
                {passwordSuccess}
              </div>
            )}

            {passwordError && (
              <div style={{ padding: '14px', borderRadius: '10px', backgroundColor: '#fef2f2', color: '#dc2626', fontSize: '13px', fontWeight: 700, marginBottom: '20px', border: '1px solid #fecaca' }}>
                {passwordError}
              </div>
            )}

            {/* STEP 1: VERIFY CURRENT PASSWORD */}
            {passwordStep === 1 && (
              <form onSubmit={handleVerifyCurrentPassword} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                    Step 1: Enter Current Password *
                  </label>
                  <input
                    type="password"
                    required
                    placeholder="Enter your current password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
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
                  disabled={verifyingPassword}
                  style={{
                    padding: '12px 24px',
                    borderRadius: '10px',
                    backgroundColor: '#166534',
                    color: '#ffffff',
                    border: 'none',
                    fontSize: '14px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  {verifyingPassword ? 'Verifying...' : 'Verify Current Password'}
                </button>
              </form>
            )}

            {/* STEP 2: ENTER NEW PASSWORD & CONFIRMATION */}
            {passwordStep === 2 && (
              <form onSubmit={handleUpdatePassword} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <div style={{ padding: '10px 14px', borderRadius: '8px', backgroundColor: '#f0fdf4', color: '#166534', fontSize: '12px', fontWeight: 700, border: '1px solid #bbf7d0' }}>
                  ✓ Current password verified. Enter your new password below.
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                    New Password (Min 6 chars) *
                  </label>
                  <input
                    type="password"
                    required
                    placeholder="Create a new password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
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

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                    Confirm New Password *
                  </label>
                  <input
                    type="password"
                    required
                    placeholder="Re-enter your new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
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

                <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                  <button
                    type="button"
                    onClick={() => setPasswordStep(1)}
                    style={{
                      padding: '12px 20px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      backgroundColor: '#ffffff',
                      color: '#475569',
                      fontWeight: 700,
                      fontSize: '14px',
                      cursor: 'pointer'
                    }}
                  >
                    Back
                  </button>
                  <button
                    type="submit"
                    disabled={changingPassword}
                    style={{
                      flex: 1,
                      padding: '12px 24px',
                      borderRadius: '10px',
                      backgroundColor: '#166534',
                      color: '#ffffff',
                      border: 'none',
                      fontSize: '14px',
                      fontWeight: 700,
                      cursor: 'pointer'
                    }}
                  >
                    {changingPassword ? 'Updating Password...' : 'Update Password'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
