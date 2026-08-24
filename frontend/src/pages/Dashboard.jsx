import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Building2, LogOut, Plus, Ticket, Users, AlertCircle } from "lucide-react";
import api from "../api/axios";
import CreateTicketModal from "../components/CreateTicketModal";

export default function Dashboard() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const name = localStorage.getItem("name");
  const role = localStorage.getItem("role");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/");
      return;
    }
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      const res = await api.get("/tickets");
      setTickets(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  const statusColor = {
    OPEN: "bg-amber-500/10 text-amber-400 border-amber-500/20",
    ASSIGNED: "bg-blue-500/10 text-blue-400 border-blue-500/20",
    IN_PROGRESS: "bg-purple-500/10 text-purple-400 border-purple-500/20",
    RESOLVED: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
  };

  const urgencyColor = {
    LOW: "text-slate-400",
    MEDIUM: "text-blue-400",
    HIGH: "text-orange-400",
    CRITICAL: "text-red-400",
  };

  const openCount = tickets.filter((t) => t.status === "OPEN").length;
  const inProgressCount = tickets.filter((t) => t.status === "IN_PROGRESS" || t.status === "ASSIGNED").length;
  const resolvedCount = tickets.filter((t) => t.status === "RESOLVED").length;

  return (
    <div className="min-h-screen bg-slate-950">
      <nav className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-500 flex items-center justify-center">
              <Building2 size={16} className="text-white" />
            </div>
            <span className="text-lg font-semibold text-white">Resolvyx</span>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/categories")}
              className="text-sm text-slate-400 hover:text-white transition-all"
            >
              Categories
            </button>
            <div className="text-right hidden sm:block">
              <p className="text-sm text-white font-medium">{name}</p>
              <p className="text-xs text-slate-400">{role}</p>
            </div>
            <button
              onClick={handleLogout}
              className="w-9 h-9 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-all"
            >
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-6 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-white">Tickets</h1>
            <p className="text-slate-400 text-sm mt-1">Track and manage all reported issues</p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="bg-indigo-500 hover:bg-indigo-400 text-white font-medium text-sm px-4 py-2.5 rounded-lg flex items-center gap-2 transition-all"
          >
            <Plus size={16} />
            New ticket
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          <div className="bg-white/5 border border-white/10 rounded-xl p-5">
            <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
              <AlertCircle size={14} />
              Open
            </div>
            <p className="text-3xl font-bold text-white">{openCount}</p>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-5">
            <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
              <Users size={14} />
              In progress
            </div>
            <p className="text-3xl font-bold text-white">{inProgressCount}</p>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-5">
            <div className="flex items-center gap-2 text-slate-400 text-sm mb-2">
              <Ticket size={14} />
              Resolved
            </div>
            <p className="text-3xl font-bold text-white">{resolvedCount}</p>
          </div>
        </div>

        {loading ? (
          <p className="text-slate-400 text-sm">Loading tickets...</p>
        ) : tickets.length === 0 ? (
          <div className="bg-white/5 border border-white/10 rounded-xl p-12 text-center">
            <Ticket size={32} className="mx-auto text-slate-600 mb-3" />
            <p className="text-white font-medium mb-1">No tickets yet</p>
            <p className="text-slate-400 text-sm">Create your first ticket to get started.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {tickets.map((ticket, i) => (
              <motion.div
                key={ticket.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className="bg-white/5 border border-white/10 rounded-xl p-5 hover:border-white/20 transition-all"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <h3 className="text-white font-medium mb-1">{ticket.title}</h3>
                    <p className="text-slate-400 text-sm line-clamp-1">{ticket.description}</p>
                    <div className="flex items-center gap-3 mt-3 text-xs">
                      <span className="text-slate-500">{ticket.categoryName}</span>
                      <span className={`font-medium ${urgencyColor[ticket.urgency]}`}>
                        {ticket.urgency}
                      </span>
                      {ticket.assignedToName && (
                        <span className="text-slate-500">→ {ticket.assignedToName}</span>
                      )}
                    </div>
                  </div>
                  <span
                    className={`text-xs font-medium px-2.5 py-1 rounded-full border whitespace-nowrap ${statusColor[ticket.status]}`}
                  >
                    {ticket.status.replace("_", " ")}
                  </span>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </div>

      {showModal && (
        <CreateTicketModal
          onClose={() => setShowModal(false)}
          onCreated={fetchTickets}
        />
      )}
    </div>
  );
}