import { Navigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";

/** Returns true if the stored JWT token is present and not yet expired. */
function isTokenValid() {
  const token = localStorage.getItem("token");
  if (!token) return false;
  try {
    // JWT payload is the second base64url segment
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    // exp is seconds since epoch
    return typeof payload.exp === "number" && payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export default function ProtectedRoute({ role, children }) {
  const { user } = useAuth();
  if (!user || !isTokenValid()) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return children;
}
