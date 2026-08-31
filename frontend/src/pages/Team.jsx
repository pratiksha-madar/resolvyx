import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Building2, ArrowLeft, Users, Copy, Check } from "lucide-react";
import api from "../api/axios";

export default function Team() {
  const navigate = useNavigate();
  const [team, setTeam] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const orgCode = localStorage.getItem("orgCode");

  useEffect(() => {
    api
      .get("/team")
      .then((res) => setTeam(res.data))
      .catch((err) => {
        setError(err.response?.data?.message || "Couldn't load team.");
      })
      .finally(() => setLoading(false));
  }, []);

  const handleCopyCode = () => {
    if (orgCode) {
      navigator.clipboard.writeText(orgCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const roleColor = {
    ORG_ADMIN: "bg-indigo-500/10 text-indigo-400 border-indigo-500/20",
    STAFF: "bg-blue-500/10 text-blue-400 border-blue-500/20",
    MEMBER: "bg-slate-500/10 text-slate-400 border-slate-500/20",
  };

  return (
    <div className="min-h-screen bg-slate-950">
      <nav className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center gap-4">
          <button
            onClick={() => navigate("/dashboard")}
            className="w-9 h-9 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-all"
          >
            <ArrowLeft size={16} />
          </button>
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-500 flex items-center justify-center">
              <Building2 size={16} className="text-white" />
            </div>
            <span className="text-lg font-semibold text-white">Resolvyx</span>
          </div>
        </div>
      </nav>

      <div className="max-w-3xl mx-auto px-6 py-8">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-2xl font-bold text-white">Team</h1>
        </div>
        <p className="text-slate-400 text-sm mb-6">Everyone in your organization</p>

        {orgCode && (
          <div className="bg-white/5 border border-white/10 rounded-xl p-4 mb-8 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 mb-1">Invite people with this code</p>
              <p className="text-white font-mono text-lg tracking-wider">{orgCode}</p>
            </div>
            <button
              onClick={handleCopyCode}
              className="bg-white/5 hover:bg-white/10 text-white text-sm font-medium px-4 py-2 rounded-lg flex items-center gap-2 transition-all"
            >
              {copied ? <Check size={14} className="text-emerald-400" /> : <Copy size={14} />}
              {copied ? "Copied" : "Copy"}
            </button>
          </div>
        )}

        {loading ? (
          <p className="text-slate-400 text-sm">Loading team...</p>
        ) : error ? (
          <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        ) : team.length === 0 ? (
          <div className="bg-white/5 border border-white/10 rounded-xl p-12 text-center">
            <Users size={32} className="mx-auto text-slate-600 mb-3" />
            <p className="text-white font-medium mb-1">No team members yet</p>
            <p className="text-slate-400 text-sm">Share your org code to invite people.</p>
          </div>
        ) : (
          <div className="space-y-2">
            {team.map((member, i) => (
              <motion.div
                key={member.id}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className="bg-white/5 border border-white/10 rounded-xl px-5 py-4 flex items-center justify-between"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-indigo-500/10 flex items-center justify-center text-indigo-400 text-sm font-semibold">
                    {member.name.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <p className="text-white text-sm font-medium">{member.name}</p>
                    <p className="text-slate-500 text-xs">{member.email}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  {member.role === "STAFF" && (
                    <span className="text-xs text-slate-500">
                      {member.activeTicketCount} active ticket{member.activeTicketCount !== 1 ? "s" : ""}
                    </span>
                  )}
                  <span
                    className={`text-xs font-medium px-2.5 py-1 rounded-full border whitespace-nowrap ${roleColor[member.role]}`}
                  >
                    {member.role.replace("_", " ")}
                  </span>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}