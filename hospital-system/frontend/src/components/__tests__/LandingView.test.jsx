import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import LandingView from '../LandingView'

describe('LandingView Component', () => {
  const mockSetView = jest.fn()

  beforeEach(() => {
    mockSetView.mockClear()
  })

  test('renders main heading', () => {
    render(<LandingView setView={mockSetView} />)
    expect(screen.getByText('Advanced Health Logistics')).toBeInTheDocument()
  })

  test('renders welcome message with EverVault and Evernorth', () => {
    render(<LandingView setView={mockSetView} />)
    expect(screen.getByText(/EverVault/)).toBeInTheDocument()
    expect(screen.getByText(/Evernorth/)).toBeInTheDocument()
  })

  test('renders feature sections', () => {
    render(<LandingView setView={mockSetView} />)
    expect(screen.getByText(/Priority Triage/)).toBeInTheDocument()
    expect(screen.getByText(/Live Tracking/)).toBeInTheDocument()
    expect(screen.getByText(/Secure Vault/)).toBeInTheDocument()
  })

  test('renders navigation buttons', () => {
    render(<LandingView setView={mockSetView} />)
    expect(screen.getByText('Go to Patient Portal')).toBeInTheDocument()
    expect(screen.getByText('Admin Access')).toBeInTheDocument()
  })

  test('clicking Patient Portal button navigates to USER view', () => {
    render(<LandingView setView={mockSetView} />)
    const patientButton = screen.getByText('Go to Patient Portal')
    fireEvent.click(patientButton)
    expect(mockSetView).toHaveBeenCalledWith('USER')
  })

  test('clicking Admin Access button navigates to ADMIN view', () => {
    render(<LandingView setView={mockSetView} />)
    const adminButton = screen.getByText('Admin Access')
    fireEvent.click(adminButton)
    expect(mockSetView).toHaveBeenCalledWith('ADMIN')
  })

  test('renders feature descriptions', () => {
    render(<LandingView setView={mockSetView} />)
    expect(screen.getByText(/Smart algorithms ensure emergency cases/)).toBeInTheDocument()
    expect(screen.getByText(/Real-time machine availability/)).toBeInTheDocument()
    expect(screen.getByText(/Enterprise-grade security/)).toBeInTheDocument()
  })
})
