export default function JobCard({ job, matched = [], missing = [], score, onDelete }) {
  const hasScore = typeof score === "number";
  return (
    <article className="card job">
      <div className="job-head">
        <div>
          <h3>{job.title}</h3>
          <p className="muted">{job.company} · {job.location}</p>
        </div>
        {hasScore && (
          <div className="score" aria-label={`Match score ${score} percent`}>
            <strong>{Math.round(score)}%</strong>
            <span className="meter"><i style={{ width: `${Math.min(100, score)}%` }} /></span>
          </div>
        )}
      </div>
      <p className="desc">{job.description}</p>
      <div className="chips">
        {hasScore
          ? <>
              {matched.map((s) => <span key={s} className="chip match" aria-label={`Matched skill: ${s}`}>{s}</span>)}
              {missing.map((s) => <span key={s} className="chip miss" aria-label={`Missing skill: ${s}`}>{s}</span>)}
            </>
          : job.requiredSkills?.map((s) => <span key={s} className="chip">{s}</span>)}
      </div>
      {hasScore && missing.length > 0 && <p className="hint">Skills to add to your resume or learn are shown in orange.</p>}
      {onDelete && <button className="btn ghost danger" onClick={() => onDelete(job.id)}>Delete job</button>}
    </article>
  );
}
