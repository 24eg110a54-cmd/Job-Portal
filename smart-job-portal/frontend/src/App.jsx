import { Navigate, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Jobs from "./pages/Jobs.jsx";
import Resume from "./pages/Resume.jsx";
import Recommendations from "./pages/Recommendations.jsx";
import PostJob from "./pages/PostJob.jsx";

export default function App() {
  return (
    <>
      <Navbar />
      <main className="page">
        <Routes>
          <Route path="/" element={<Jobs />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/resume" element={<ProtectedRoute role="CANDIDATE"><Resume /></ProtectedRoute>} />
          <Route path="/matches" element={<ProtectedRoute role="CANDIDATE"><Recommendations /></ProtectedRoute>} />
          <Route path="/post-job" element={<ProtectedRoute role="RECRUITER"><PostJob /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
