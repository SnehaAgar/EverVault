import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import NavBar from '../NavBar'

describe('NavBar Component', () => {
  const mockSetView = jest.fn()

  beforeEach(() => {
    mockSetView.mockClear()
  })

  test('renders brand name correctly', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    expect(screen.getByText(/Ever/)).toBeInTheDocument()
    expect(screen.getByText(/Vault/)).toBeInTheDocument()
  })

  test('renders navigation buttons', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    expect(screen.getByText('Patient Portal')).toBeInTheDocument()
    expect(screen.getByText('Admin Control')).toBeInTheDocument()
  })

  test('clicking brand logo navigates to landing page', () => {
    render(<NavBar view="USER" setView={mockSetView} />)
    const brand = screen.getByText(/Ever/).closest('.brand')
    fireEvent.click(brand)
    expect(mockSetView).toHaveBeenCalledWith('LANDING')
  })

  test('clicking Patient Portal button calls setView with USER', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    const patientButton = screen.getByText('Patient Portal')
    fireEvent.click(patientButton)
    expect(mockSetView).toHaveBeenCalledWith('USER')
  })

  test('clicking Admin Control button calls setView with ADMIN', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    const adminButton = screen.getByText('Admin Control')
    fireEvent.click(adminButton)
    expect(mockSetView).toHaveBeenCalledWith('ADMIN')
  })

  test('Patient Portal button has active class when view is USER', () => {
    render(<NavBar view="USER" setView={mockSetView} />)
    const patientButton = screen.getByText('Patient Portal')
    expect(patientButton).toHaveClass('active')
  })

  test('Admin Control button has active class when view is ADMIN', () => {
    render(<NavBar view="ADMIN" setView={mockSetView} />)
    const adminButton = screen.getByText('Admin Control')
    expect(adminButton).toHaveClass('active')
  })

  test('buttons do not have active class when not selected', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    const patientButton = screen.getByText('Patient Portal')
    const adminButton = screen.getByText('Admin Control')
    expect(patientButton).not.toHaveClass('active')
    expect(adminButton).not.toHaveClass('active')
  })

  // DEMO: This test will now PASS - we fixed the icon!
  test('DEMO: brand logo should show hospital icon (NOW PASSES)', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    // Check if hospital icon is in the brand - using flexible matcher
    const brand = screen.getByText(/Ever/)
    expect(brand).toHaveTextContent('🏥')
  })

  // DEMO: This test will FAIL - shows validation testing
  test('DEMO: should have Help button (WILL FAIL)', () => {
    render(<NavBar view="LANDING" setView={mockSetView} />)
    // This test expects a Help button that doesn't exist
    expect(screen.getByText('Help')).toBeInTheDocument()
  })
})
