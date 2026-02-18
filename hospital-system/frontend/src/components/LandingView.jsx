import React from 'react'

function LandingView({ setView }) {
  return (
    <main>
      <div className="hero">
        <div className="container">
          <h1>Advanced Health Logistics</h1>
          <p>
            Welcome to <strong>EverVault</strong> by <strong>Evernorth</strong>. We
            provide secure, priority-based equipment management and triage solutions for
            modern healthcare facilities.
          </p>
          <div className="hero-btns">
            <button className="btn-primary" onClick={() => setView('USER')}>
              Go to Patient Portal
            </button>
            <button className="btn-outline" onClick={() => setView('ADMIN')}>
              Admin Access
            </button>
          </div>
        </div>
      </div>

      <div className="container">
        <div className="features-grid">
          <div className="feature-card">
            <h2 style={{ color: 'var(--secondary)' }}>
              ⚡ Priority Triage
            </h2>
            <p>
              Smart algorithms ensure emergency cases are prioritized instantly without
              disrupting critical workflows.
            </p>
          </div>
          <div className="feature-card">
            <h2 style={{ color: 'var(--secondary)' }}>
              🕙 Live Tracking
            </h2>
            <p>
              Real-time machine availability and automated slot estimations for
              predictable facility planning.
            </p>
          </div>
          <div className="feature-card">
            <h2 style={{ color: 'var(--secondary)' }}>
              🛡️ Secure Vault
            </h2>
            <p>Enterprise-grade security for patient bookings and medical equipment logistics.</p>
          </div>
        </div>
      </div>
    </main>
  )
}

export default LandingView

