import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="nav">
      <NavLink to="/" className="brand">Shortlist</NavLink>
      <nav>
        <NavLink to="/" end>Jobs</NavLink>
        {user?.role === "CANDIDATE" && (
          <>
            <NavLink to="/resume">My resume</NavLink>
            <NavLink to="/matches">Matches</NavLink>
          </>
        )}
        {user?.role === "RECRUITER" && <NavLink to="/post-job">Post a job</NavLink>}
      </nav>
      <div className="nav-user">
        {user ? (
          <>
            <span>{user.name}</span>
            <button className="btn ghost" onClick={signOut}>Sign out</button>
          </>
        ) : (
          <>
            <NavLink to="/login">Sign in</NavLink>
            <NavLink to="/register" className="btn">Create account</NavLink>
          </>
        )}
      </div>
    </header>
  );
}
