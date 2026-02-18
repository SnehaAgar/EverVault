import React from 'react'

function NavBar({ view, setView }) {
  return (
    <nav>
      <div className="container">
        <div
          className="brand"
          onClick={() => setView('LANDING')}
        >
          🏥 Ever<span>Vault</span>
        </div>
        <div className="nav-links">
          <button
            className={view === 'USER' ? 'active' : ''}
            onClick={() => setView('USER')}
          >
            Patient Portal
          </button>
          <button
            className={view === 'ADMIN' ? 'active' : ''}
            onClick={() => setView('ADMIN')}
          >
            Admin Control
          </button>
          <button
            className="btn-help"
            onClick={() => alert('Help: Contact support@evervault.com')}
          >
            Help
          </button>
        </div>
      </div>
    </nav>
  )
}

export default NavBar

