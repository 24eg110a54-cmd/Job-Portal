import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api";

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("user"));
    } catch {
      return null;
    }
  });

  const save = (data) => {
    const { token, ...profile } = data;
    localStorage.setItem("token", token);
    localStorage.setItem("user", JSON.stringify(profile));
    setUser(profile);
    return profile;
  };

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login", { replace: true });
  }, [navigate]);

  const login = async (email, password) => save((await api.post("/auth/login", { email, password })).data);
  const register = async (payload) => save((await api.post("/auth/register", payload)).data);

  // Listen for the 401-interceptor event so in-memory state stays in sync (H-3)
  useEffect(() => {
    window.addEventListener("auth:logout", logout);
    return () => window.removeEventListener("auth:logout", logout);
  }, [logout]);

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>;
}
