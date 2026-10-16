import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";
import { errorMessage } from "../api";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const user = await login(form.email, form.password);
      navigate(user.role === "RECRUITER" ? "/post-job" : "/matches");
    } catch (err) {
      setError(errorMessage(err, "Could not sign in. Check your connection and try again."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="card form narrow" onSubmit={submit}>
      <h1>Sign in</h1>
      {error && <p className="error" role="alert">{error}</p>}
      <label>Email<input name="email" type="email" required value={form.email} onChange={change} autoComplete="email" /></label>
      <label>Password<input name="password" type="password" required value={form.password} onChange={change} autoComplete="current-password" /></label>
      <button className="btn" disabled={busy}>{busy ? "Signing in…" : "Sign in"}</button>
      <p className="muted">New here? <Link to="/register">Create an account</Link></p>
    </form>
  );
}
