import { useState, useEffect } from 'react'
import './App.css'
import NavBar from './components/NavBar'
import LandingView from './components/LandingView'
import PatientView from './components/PatientView'
import AdminView from './components/AdminView'

function App() {
  const [view, setView] = useState('LANDING'); // 'LANDING', 'USER', or 'ADMIN'
  const [equipment, setEquipment] = useState([]);
  const [selectedEquipment, setSelectedEquipment] = useState(null);

  // Data
  const [queue, setQueue] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);

  // Form State
  const [patientName, setPatientName] = useState('');
  const [requestedPriority, setRequestedPriority] = useState('NORMAL');
  const [slotTime, setSlotTime] = useState('');

  // Auto-detect environment: If running on a dev port (like 5173 or 5174), point to backend 8080.
  // If running on 8080 (production build), use relative paths.
  const API_BASE = (window.location.port && window.location.port !== '8080') ? 'http://localhost:8080' : '';

  // Navigation helper: keep the `view` state and browser path in sync.
  const viewToPath = (v) => {
    if (v === 'USER') return '/patient';
    if (v === 'ADMIN') return '/admin';
    return '/';
  };

  const pathToView = (p) => {
    if (p.startsWith('/patient')) return 'USER';
    if (p.startsWith('/admin')) return 'ADMIN';
    return 'LANDING';
  };

  const navigateTo = (v) => {
    setView(v);
    const path = viewToPath(v);
    try {
      window.history.pushState({}, '', path);
    } catch (e) {
      // ignore history errors in some environments
    }
  };

  // Force layout fixes to prevent vertical centering
  useEffect(() => {
    const fixLayout = () => {
      document.body.style.display = 'block';
      document.body.style.alignItems = 'flex-start';
      document.body.style.justifyContent = 'flex-start';
      document.body.style.minHeight = 'auto';
      document.body.style.height = 'auto';
      document.documentElement.style.height = 'auto';
    };
    fixLayout();
    // Also run on a short delay to catch any late overrides
    const timer = setTimeout(fixLayout, 100);
    return () => clearTimeout(timer);
  }, [view]);

  // Load Data
  useEffect(() => {
    fetchEquipment();
    const interval = setInterval(() => {
      fetchEquipment(); // Periodically sync equipment status (IN_USE vs AVAILABLE)
      if (view === 'ADMIN') fetchPendingRequests();
      if (selectedEquipment) fetchQueue(selectedEquipment.id);
    }, 2000);
    return () => clearInterval(interval);
  }, [view, selectedEquipment]);

  // Initialize view from URL and handle back/forward navigation
  useEffect(() => {
    const initial = pathToView(window.location.pathname || '/');
    if (initial !== view) setView(initial);

    const onPop = () => {
      const v = pathToView(window.location.pathname || '/');
      setView(v);
    };
    window.addEventListener('popstate', onPop);
    return () => window.removeEventListener('popstate', onPop);
  }, []); // run once on mount

  const fetchEquipment = () => {
    fetch(`${API_BASE}/api/equipment`)
      .then(res => res.json())
      .then(data => setEquipment(data));
  };

  const fetchQueue = (id) => {
    fetch(`${API_BASE}/api/queue/${id}`)
      .then(res => res.json())
      .then(data => setQueue(data));
  };

  const fetchPendingRequests = () => {
    fetch(`${API_BASE}/api/bookings/pending`)
      .then(res => res.json())
      .then(data => setPendingRequests(data));
  };

  const handleRequestBooking = (e) => {
    e.preventDefault();
    if (!selectedEquipment) return alert('Please select a piece of equipment first.');

    // Simple Date Validation
    const selectedDate = new Date(slotTime);
    const now = new Date();
    if (selectedDate < now) {
      return alert('Error: You cannot book a slot in the past. Please select a future time.');
    }

    const booking = {
      patientName,
      requestedPriority,
      equipmentId: selectedEquipment.id,
      slotTime: slotTime
    };

    fetch(`${API_BASE}/api/bookings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(booking)
    })
      .then(res => {
        if (!res.ok) return res.json().then(err => { throw new Error(err.message || 'Error') });
        return res.json();
      })
      .then(() => {
        alert('Success! Your request has been sent to the Triage team.');
        setPatientName('');
        setSlotTime('');
      })
      .catch(err => alert(err.message));
  };

  const handleApprove = (bookingId, assignedPriority) => {
    fetch(`${API_BASE}/api/bookings/${bookingId}/confirm`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ assignedPriority })
    })
      .then(res => res.json())
      .then(() => {
        fetchPendingRequests();
        if (selectedEquipment) fetchQueue(selectedEquipment.id);
      });
  };

  const handleCallNext = () => {
    if (!selectedEquipment) return;
    fetch(`${API_BASE}/api/queue/${selectedEquipment.id}/next`, { method: 'POST' })
      .then(res => {
        if (res.ok) {
          fetchQueue(selectedEquipment.id);
          fetchEquipment();
        }
      });
  };

  const getFilteredPending = () => {
    if (!selectedEquipment) return []
    return pendingRequests.filter((req) => req.equipmentId === selectedEquipment.id)
  };

  return (
    <div className="app-wrapper">

      <NavBar view={view} setView={navigateTo} />

      {view === 'LANDING' && <LandingView setView={navigateTo} />}

      {view !== 'LANDING' && (
        <div className={`container view-content ${view === 'ADMIN' ? 'admin-view-container' : ''}`}>
          {view === 'USER' && (
            <PatientView
              equipment={equipment}
              selectedEquipment={selectedEquipment}
              setSelectedEquipment={setSelectedEquipment}
              patientName={patientName}
              setPatientName={setPatientName}
              slotTime={slotTime}
              setSlotTime={setSlotTime}
              requestedPriority={requestedPriority}
              setRequestedPriority={setRequestedPriority}
              handleRequestBooking={handleRequestBooking}
            />
          )}

          {view === 'ADMIN' && (
            <AdminView
              equipment={equipment}
              selectedEquipment={selectedEquipment}
              setSelectedEquipment={setSelectedEquipment}
              pendingRequests={pendingRequests}
              queue={queue}
              handleApprove={handleApprove}
              handleCallNext={handleCallNext}
            />
          )}
        </div>
      )}

      <footer>
        <p>© 2026 Evernorth Health Services | EverVault Logistics Demo</p>
      </footer>
    </div>
  )
}

export default App
