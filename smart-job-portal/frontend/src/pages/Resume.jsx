import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errorMessage } from "../api";

export default function Resume() {
  const [current, setCurrent] = useState(null);
  const [file, setFile] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/resume/me").then((res) => setCurrent(res.status === 200 ? res.data : null)).catch(() => {});
  }, []);

  const upload = async (e) => {
    e.preventDefault();
    if (!file) return setError("Choose a PDF, DOCX or TXT file first.");
    const body = new FormData();
    body.append("file", file);
    setBusy(true);
    setError("");
    try {
      const res = await api.post("/resume/upload", body);
      setCurrent(res.data);
      setFile(null);
      e.target.reset();
    } catch (err) {
      setError(errorMessage(err, "Upload failed. Try again."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <h1>My resume</h1>
      <form className="card form" onSubmit={upload}>
        <label>
          Upload a PDF, DOCX or TXT file (max 5 MB)
          <input type="file" accept=".pdf,.docx,.txt" onChange={(e) => setFile(e.target.files[0])} />
        </label>
        {error && <p className="error" role="alert">{error}</p>}
        <button className="btn" disabled={busy}>{busy ? "Reading resume…" : "Upload resume"}</button>
      </form>

      {current ? (
        <section className="card">
          <h2>Skills found in {current.fileName}</h2>
          {current.skills.length === 0
            ? <p className="muted">No known skills detected. Check that the file contains selectable text.</p>
            : <div className="chips">{current.skills.map((s) => <span key={s} className="chip match">{s}</span>)}</div>}
          <p><Link className="btn" to="/matches">See matching jobs</Link></p>
        </section>
      ) : (
        <p className="muted">No resume yet. Upload one to see jobs ranked for you.</p>
      )}
    </>
  );
}
