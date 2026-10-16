import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errorMessage } from "../api";
import JobCard from "../components/JobCard.jsx";

export default function Recommendations() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/resume/recommendations")
      .then((res) => setItems(res.data))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <h1>Jobs matched to your resume</h1>
      {loading && <p className="muted">Scoring jobs…</p>}
      {error && (
        <p className="error" role="alert">
          {error}. <Link to="/resume">Go to My resume</Link>
        </p>
      )}
      <div className="grid">
        {items.map((r) => (
          <JobCard key={r.job.id} job={r.job} score={r.score} matched={r.matchedSkills} missing={r.missingSkills} />
        ))}
      </div>
    </>
  );
}
