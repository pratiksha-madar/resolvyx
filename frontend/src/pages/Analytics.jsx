import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Building2, ArrowLeft, TrendingUp, Clock, Star, AlertTriangle } from "lucide-react";
import api from "../api/axios";

export default function Analytics() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get("/tickets/analytics")
      .then((res) => setData(res.data))
      .catch((err) => {
        setError(err.response?.data?.message || "Couldn't load analytics.");
      })
      .finally(() => setLoading(false));
  }, []);

  const maxCategoryCount = data ? Math.max(1, ...Object.values(data.ticketsByCategory)) : 1;
  const maxStaffCount = data ? Math.max(1, ...Object.values(data.ticketsByStaff)) : 1;

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

      <div className="max-w-6xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-1">Analytics</h1>
        <p className="text-slate-400 text-sm mb-8">Performance overview for your organization</p>

        {loading ? (
          <p className="text-slate-400 text-sm">Loading analytics...</p>
        ) : error ? (
          <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white/5 border border-white/10 rounded-xl p-5"
              >
                <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
                  <TrendingUp size={14} />
                  Total tickets
                </div>
                <p className="text-3xl font-bold text-white">{data.totalTickets}</p>
                <p className="text-xs text-slate-500 mt-1">
                  {data.resolvedTickets} resolved · {data.openTickets} open · {data.inProgressTickets} active
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.05 }}
                className="bg-white/5 border border-white/10 rounded-xl p-5"
              >
                <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
                  <Clock size={14} />
                  Avg. resolution time
                </div>
                <p className="text-3xl font-bold text-white">
                  {data.averageResolutionHours != null ? `${data.averageResolutionHours.toFixed(1)}h` : "—"}
                </p>
                <p className="text-xs text-slate-500 mt-1">Across all resolved tickets</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 }}
                className="bg-white/5 border border-white/10 rounded-xl p-5"
              >
                <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
                  <Star size={14} />
                  Avg. rating
                </div>
                <p className="text-3xl font-bold text-white">
                  {data.averageRating != null ? data.averageRating.toFixed(1) : "—"}
                </p>
                <p className="text-xs text-slate-500 mt-1">From resolved-ticket feedback</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.15 }}
                className="bg-white/5 border border-white/10 rounded-xl p-5"
              >
                <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
                  <AlertTriangle size={14} />
                  Escalated
                </div>
                <p className="text-3xl font-bold text-white">{data.escalatedTickets}</p>
                <p className="text-xs text-slate-500 mt-1">SLA deadline missed</p>
              </motion.div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="bg-white/5 border border-white/10 rounded-xl p-6">
                <h2 className="text-white font-semibold mb-4">Tickets by category</h2>
                {Object.keys(data.ticketsByCategory).length === 0 ? (
                  <p className="text-slate-500 text-sm">No data yet.</p>
                ) : (
                  <div className="space-y-3">
                    {Object.entries(data.ticketsByCategory).map(([name, count]) => (
                      <div key={name}>
                        <div className="flex justify-between text-sm mb-1">
                          <span className="text-slate-300">{name}</span>
                          <span className="text-slate-500">{count}</span>
                        </div>
                        <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                          <motion.div
                            initial={{ width: 0 }}
                            animate={{ width: `${(count / maxCategoryCount) * 100}%` }}
                            className="h-full bg-indigo-500 rounded-full"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="bg-white/5 border border-white/10 rounded-xl p-6">
                <h2 className="text-white font-semibold mb-4">Tickets by staff</h2>
                {Object.keys(data.ticketsByStaff).length === 0 ? (
                  <p className="text-slate-500 text-sm">No assignments yet.</p>
                ) : (
                  <div className="space-y-3">
                    {Object.entries(data.ticketsByStaff).map(([name, count]) => (
                      <div key={name}>
                        <div className="flex justify-between text-sm mb-1">
                          <span className="text-slate-300">{name}</span>
                          <span className="text-slate-500">{count}</span>
                        </div>
                        <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                          <motion.div
                            initial={{ width: 0 }}
                            animate={{ width: `${(count / maxStaffCount) * 100}%` }}
                            className="h-full bg-purple-500 rounded-full"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}