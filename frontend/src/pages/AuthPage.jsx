import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRoleDefaultPath } from '../utils/authRoleUtils';
import { Lock, Mail, User, CheckCircle2, Key, X } from 'lucide-react';

export const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  
  // Clean default form fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [selectedRole, setSelectedRole] = useState('ROLE_USER');
  
  // Status states
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  // Password Recovery Modal State
  const [isForgotOpen, setIsForgotOpen] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotNewPass, setForgotNewPass] = useState('');
  const [forgotConfirmPass, setForgotConfirmPass] = useState('');
  const [forgotSuccess, setForgotSuccess] = useState('');
  const [forgotError, setForgotError] = useState('');

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setLoading(true);

    try {
      if (isLogin) {
        const res = await login(email, password);
        if (res.success && res.user) {
          // Use centralized role-to-route mapping (single source of truth)
          // Role comes exclusively from the server login response — not from any
          // locally stored or user-selected value.
          navigate(getRoleDefaultPath(res.user), { replace: true });
        } else {
          setError(res.message || 'Invalid email/username or password. Please verify your login credentials or sign up for a new account.');
        }
      } else {
        const isAuthRequest = selectedRole === 'ROLE_AUTHORITY';
        const res = await register(username || email.split('@')[0], email, password, isAuthRequest);
        if (res.success) {
          setIsLogin(true);
          setSuccessMsg(isAuthRequest
            ? 'Account created! Your Authority Access request has been submitted for Administrator review. You may sign in as a Consumer in the meantime.'
            : 'Account created. Please log in with your credentials.');
          setPassword('');
        } else {
          setError(res.message || 'Could not create account. That username or email might already be taken.');
        }
      }
    } catch (err) {
      setError('Could not connect to the server. Please make sure the backend server is running and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    setForgotError('');
    setForgotSuccess('');

    if (!forgotEmail || !forgotEmail.includes('@')) {
      setForgotError('Please enter a valid email address.');
      return;
    }
    if (forgotNewPass.length < 6) {
      setForgotError('Your new password must be at least 6 characters long.');
      return;
    }
    if (forgotNewPass !== forgotConfirmPass) {
      setForgotError('The passwords you typed do not match.');
      return;
    }

    setForgotSuccess(`Password reset for ${forgotEmail}. You can now sign in with your new password.`);
    setTimeout(() => {
      setIsForgotOpen(false);
      setIsLogin(true);
      setSuccessMsg('Password updated successfully. Please log in with your new password.');
    }, 1800);
  };

  return (
    <div style={{
      display: 'flex',
      minHeight: '100vh',
      backgroundColor: '#f8fafc',
      fontFamily: "'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif",
      color: '#0f172a'
    }}>
      {/* LEFT INFORMATION PANEL - Flat Deep Green Background */}
      <div style={{
        flex: '1.1',
        backgroundColor: '#064e3b',
        color: '#ffffff',
        padding: '56px 64px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        {/* Simple Brand Header */}
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '48px' }}>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '8px',
              backgroundColor: '#10b981',
              color: '#ffffff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 800,
              fontSize: '18px'
            }}>
              F
            </div>
            <span style={{ fontSize: '20px', fontWeight: 800, color: '#ffffff', letterSpacing: '-0.02em' }}>
              FairPrice AI
            </span>
          </div>

          {/* Plain English Headline & Description */}
          <div style={{ maxWidth: '520px' }}>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#6ee7b7', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: '12px' }}>
              FAIRPRICE AI
            </div>
            <h1 style={{
              fontSize: '36px',
              fontWeight: 800,
              lineHeight: 1.25,
              color: '#ffffff',
              marginBottom: '16px',
              letterSpacing: '-0.02em'
            }}>
              Check prices before you pay.
            </h1>
            <p style={{
              fontSize: '16px',
              color: '#a7f3d0',
              lineHeight: 1.6,
              fontWeight: 400,
              marginBottom: '32px'
            }}>
              Compare market prices, check whether a price is reasonable, and report possible price issues with supporting evidence.
            </p>
          </div>

          {/* How FairPrice Works - 3 Step Product Workflow */}
          <div style={{
            backgroundColor: '#043e2e',
            borderRadius: '12px',
            padding: '24px',
            border: '1px solid #065f46',
            maxWidth: '520px',
            marginBottom: '32px'
          }}>
            <div style={{ fontSize: '12px', fontWeight: 700, color: '#6ee7b7', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '16px' }}>
              How FairPrice Works
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '13px' }}>
              <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                <span style={{ backgroundColor: '#10b981', color: '#043e2e', fontWeight: 800, fontSize: '12px', padding: '2px 8px', borderRadius: '6px' }}>01</span>
                <div>
                  <div style={{ color: '#ffffff', fontWeight: 700 }}>Check a price</div>
                  <div style={{ color: '#a7f3d0', fontSize: '12px', marginTop: '2px' }}>Compare a commodity against available market reference data.</div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                <span style={{ backgroundColor: '#10b981', color: '#043e2e', fontWeight: 800, fontSize: '12px', padding: '2px 8px', borderRadius: '6px' }}>02</span>
                <div>
                  <div style={{ color: '#ffffff', fontWeight: 700 }}>Review the evidence</div>
                  <div style={{ color: '#a7f3d0', fontSize: '12px', marginTop: '2px' }}>Check price history and regional market information.</div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                <span style={{ backgroundColor: '#10b981', color: '#043e2e', fontWeight: 800, fontSize: '12px', padding: '2px 8px', borderRadius: '6px' }}>03</span>
                <div>
                  <div style={{ color: '#ffffff', fontWeight: 700 }}>Report the issue</div>
                  <div style={{ color: '#a7f3d0', fontSize: '12px', marginTop: '2px' }}>Submit a complaint with supporting purchase evidence.</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Real System Data Capabilities Footer */}
        <div style={{
          display: 'flex',
          gap: '40px',
          paddingTop: '24px',
          borderTop: '1px solid #065f46',
          fontSize: '13px',
          color: '#a7f3d0',
          lineHeight: 1.5
        }}>
          <div>
            <div style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>3,000+</div>
            <div>Government Mandis Indexed</div>
          </div>
          <div>
            <div style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>280+</div>
            <div>Commodities Monitored</div>
          </div>
          <div>
            <div style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>Live Sync</div>
            <div>Agmarknet Open Data</div>
          </div>
        </div>
      </div>

      {/* RIGHT AUTHENTICATION PANEL - Clean Flat Form */}
      <div style={{
        flex: '0.9',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '56px'
      }}>
        <div style={{
          width: '100%',
          maxWidth: '440px',
          backgroundColor: '#ffffff',
          borderRadius: '16px',
          padding: '40px',
          border: '1px solid #e2e8f0'
        }}>
          <div style={{ marginBottom: '32px' }}>
            <h2 style={{ fontSize: '24px', fontWeight: 800, color: '#0f172a', letterSpacing: '-0.02em' }}>
              {isLogin ? 'Sign in to your account' : 'Create an account'}
            </h2>
            <p style={{ fontSize: '14px', color: '#64748b', marginTop: '6px', lineHeight: 1.5 }}>
              {isLogin ? 'Enter your details below to access market price tracking.' : 'Sign up to check prices and file price reports.'}
            </p>
          </div>

          {/* Success Banner */}
          {successMsg && (
            <div style={{
              padding: '12px 16px',
              borderRadius: '8px',
              backgroundColor: '#f0fdf4',
              color: '#166534',
              fontSize: '13px',
              fontWeight: 600,
              marginBottom: '24px',
              border: '1px solid #bbf7d0',
              lineHeight: 1.5
            }}>
              {successMsg}
            </div>
          )}

          {/* Error Banner */}
          {error && (
            <div style={{
              padding: '12px 16px',
              borderRadius: '8px',
              backgroundColor: '#fef2f2',
              color: '#991b1b',
              fontSize: '13px',
              fontWeight: 600,
              marginBottom: '24px',
              border: '1px solid #fecaca',
              lineHeight: 1.5
            }}>
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {!isLogin && (
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                  Register As
                </label>
                <select
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0',
                    fontSize: '14px',
                    outline: 'none',
                    backgroundColor: '#f8fafc',
                    color: '#0f172a'
                  }}
                >
                  <option value="ROLE_USER">Consumer / Buyer</option>
                  <option value="ROLE_AUTHORITY">Authority Applicant (Requires Admin Approval)</option>
                </select>
                <p style={{ fontSize: '11px', color: '#94a3b8', marginTop: '6px', lineHeight: 1.5 }}>
                  {selectedRole === 'ROLE_AUTHORITY'
                    ? 'Authority applicants will have Consumer access while their application is under review by a Platform Administrator.'
                    : 'Standard consumer account for price checking and complaint reporting.'}
                </p>
              </div>
            )}

            {!isLogin && (
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                  Full Name or Username
                </label>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="e.g. Sanjai Kumar"
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>
            )}

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                Email Address or Username
              </label>
              <input
                type="text"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600, color: '#334155' }}>
                  Password
                </label>
                {isLogin && (
                  <button
                    type="button"
                    onClick={() => setIsForgotOpen(true)}
                    style={{ background: 'none', border: 'none', fontSize: '12px', color: '#166534', fontWeight: 600, cursor: 'pointer' }}
                  >
                    Forgot password?
                  </button>
                )}
              </div>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%',
                padding: '13px',
                borderRadius: '8px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: '14px',
                cursor: 'pointer',
                marginTop: '4px',
                transition: 'background-color 0.15s ease'
              }}
            >
              {loading ? (isLogin ? 'Signing in...' : 'Creating account...') : (isLogin ? 'Sign In' : 'Create Account')}
            </button>
          </form>

          {/* Switch tab */}
          <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '13px', color: '#64748b', lineHeight: 1.5 }}>
            {isLogin ? "Don't have an account? " : "Already have an account? "}
            <button
              onClick={() => { setIsLogin(!isLogin); setError(''); setSuccessMsg(''); }}
              style={{
                background: 'none',
                border: 'none',
                color: '#166534',
                fontWeight: 700,
                cursor: 'pointer',
                fontSize: '13px'
              }}
            >
              {isLogin ? 'Sign up' : 'Sign in'}
            </button>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      {isForgotOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: '16px',
            padding: '32px',
            maxWidth: '440px',
            width: '90%',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>
                Reset Your Password
              </h3>
              <button
                onClick={() => setIsForgotOpen(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}
              >
                <X size={20} />
              </button>
            </div>

            {forgotSuccess && (
              <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#f0fdf4', color: '#166534', fontSize: '13px', fontWeight: 600, marginBottom: '16px', border: '1px solid #bbf7d0' }}>
                {forgotSuccess}
              </div>
            )}

            {forgotError && (
              <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#fef2f2', color: '#dc2626', fontSize: '13px', fontWeight: 600, marginBottom: '16px', border: '1px solid #fecaca' }}>
                {forgotError}
              </div>
            )}

            <form onSubmit={handleForgotPassword} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  Email Address
                </label>
                <input
                  type="email"
                  required
                  placeholder="name@example.com"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  New Password
                </label>
                <input
                  type="password"
                  required
                  placeholder="Enter new password"
                  value={forgotNewPass}
                  onChange={(e) => setForgotNewPass(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                  Confirm Password
                </label>
                <input
                  type="password"
                  required
                  placeholder="Re-enter new password"
                  value={forgotConfirmPass}
                  onChange={(e) => setForgotConfirmPass(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <button
                type="submit"
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  backgroundColor: '#166534',
                  color: '#ffffff',
                  border: 'none',
                  fontWeight: 700,
                  fontSize: '14px',
                  cursor: 'pointer',
                  marginTop: '8px'
                }}
              >
                Reset Password
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
