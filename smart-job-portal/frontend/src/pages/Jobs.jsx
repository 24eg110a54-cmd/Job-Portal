import { useEffect, useState } from "react";
import api, { errorMessage } from "../api";
import JobCard from "../components/JobCard.jsx";

export default function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const timer = setTimeout(async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("/jobs", { params: q ? { q } : {} });
        setJobs(res.data);
      } catch (err) {
        setError(errorMessage(err, "Could not load jobs. The server may be waking up; try again in a moment."));
      } finally {
        setLoading(false);
      }
    }, 250);
    return () => clearTimeout(timer);
  }, [q]);

  return (
    <>
      <h1>Open jobs</h1>
      <input
        className="search"
        type="search"
        placeholder="Search by title, company, location or skill"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        aria-label="Search jobs"
      />
      {error && <p className="error" role="alert">{error}</p>}
      {loading && <p className="muted">Loading jobs…</p>}
      {!loading && !error && jobs.length === 0 && <p className="muted">No jobs match “{q}”. Try a broader search.</p>}
      <div className="grid">
        {jobs.map((job) => <JobCard key={job.id} job={job} />)}
      </div>
    </>
  );
}
