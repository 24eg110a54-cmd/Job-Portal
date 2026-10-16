import { useEffect, useState } from "react";
import api, { errorMessage } from "../api";
import JobCard from "../components/JobCard.jsx";

const empty = { title: "", company: "", location: "", description: "", skills: "" };

export default function PostJob() {
  const [form, setForm] = useState(empty);
  const [mine, setMine] = useState([]);
  const [formError, setFormError] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [busy, setBusy] = useState(false);

  // Simplified from useCallback+useEffect([load]) — load never changes so useCallback adds no value (L-3)
  useEffect(() => {
    api.get("/jobs/mine").then((res) => setMine(res.data)).catch((err) => setFormError(errorMessage(err)));
  }, []);

  const load = () =>
    api.get("/jobs/mine").then((res) => setMine(res.data)).catch((err) => setFormError(errorMessage(err)));

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setFormError("");
    try {
      const { skills, ...rest } = form;
      await api.post("/jobs", { ...rest, requiredSkills: skills.split(",").map((s) => s.trim()).filter(Boolean) });
      setForm(empty);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const remove = async (id) => {
    // Confirmation guard to prevent accidental irreversible deletion (M-6)
    if (!window.confirm("Delete this job posting? This cannot be undone.")) return;
    setDeleteError("");
    try {
      await api.delete(`/jobs/${id}`);
      load();
    } catch (err) {
      setDeleteError(errorMessage(err));
    }
  };

  return (
    <>
      <h1>Post a job</h1>
      <form className="card form" onSubmit={submit}>
        {formError && <p className="error" role="alert">{formError}</p>}
        <label>Job title<input name="title" required value={form.title} onChange={change} /></label>
        <div className="row">
          <label>Company<input name="company" required value={form.company} onChange={change} /></label>
          <label>Location<input name="location" required value={form.location} onChange={change} /></label>
        </div>
        <label>Description<textarea name="description" rows={5} required value={form.description} onChange={change} /></label>
        <label>
          Required skills, separated by commas
          <input name="skills" placeholder="java, spring boot, mysql" value={form.skills} onChange={change} />
        </label>
        <button className="btn" disabled={busy}>{busy ? "Publishing…" : "Publish job"}</button>
      </form>

      <h2>Your postings</h2>
      {deleteError && <p className="error" role="alert">{deleteError}</p>}
      {mine.length === 0 && <p className="muted">You haven't posted any jobs yet.</p>}
      <div className="grid">
        {mine.map((job) => <JobCard key={job.id} job={job} onDelete={remove} />)}
      </div>
    </>
  );
}
