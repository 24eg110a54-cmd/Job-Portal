import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";
import { errorMessage } from "../api";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "CANDIDATE" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const user = await register(form);
      navigate(user.role === "RECRUITER" ? "/post-job" : "/resume");
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="card form narrow" onSubmit={submit}>
      <h1>Create your account</h1>
      {error && <p className="error" role="alert">{error}</p>}
      <label>Full name<input name="name" required value={form.name} onChange={change} autoComplete="name" /></label>
      <label>Email<input name="email" type="email" required value={form.email} onChange={change} autoComplete="email" /></label>
      <label>Password<input name="password" type="password" required minLength={6} value={form.password} onChange={change} autoComplete="new-password" /></label>
      <fieldset>
        <legend>I am a</legend>
        <label className="radio"><input type="radio" name="role" value="CANDIDATE" checked={form.role === "CANDIDATE"} onChange={change} /> Job seeker</label>
        <label className="radio"><input type="radio" name="role" value="RECRUITER" checked={form.role === "RECRUITER"} onChange={change} /> Recruiter</label>
      </fieldset>
      <button className="btn" disabled={busy}>{busy ? "Creating account…" : "Create account"}</button>
      <p className="muted">Already registered? <Link to="/login">Sign in</Link></p>
    </form>
  );
}
