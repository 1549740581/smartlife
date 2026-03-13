import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/auth'
import MainLayout from './layouts/MainLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import RentalList from './pages/rentals/RentalList'
import RentalDetail from './pages/rentals/RentalDetail'
import ComplaintList from './pages/complaints/ComplaintList'
import ComplaintDetail from './pages/complaints/ComplaintDetail'
import UserList from './pages/users/UserList'
import UserDetail from './pages/users/UserDetail'
import AddressList from './pages/addresses/AddressList'
import ScanConfirm from './pages/ScanConfirm'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

const basename = import.meta.env.PROD ? '/admin' : '/'

function App() {
  return (
    <BrowserRouter basename={basename}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/scan-confirm" element={<ScanConfirm />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="rentals" element={<RentalList />} />
          <Route path="rentals/:id" element={<RentalDetail />} />
          <Route path="complaints" element={<ComplaintList />} />
          <Route path="complaints/:id" element={<ComplaintDetail />} />
          <Route path="users" element={<UserList />} />
          <Route path="users/:id" element={<UserDetail />} />
          <Route path="addresses" element={<AddressList />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
