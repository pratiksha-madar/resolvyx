import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Building2, ArrowLeft, Plus, Tag } from "lucide-react";
import api from "../api/axios";

export default function Categories() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const role = localStorage.getItem("role");

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await api.get("/categories");
      setCategories(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError("");
    if (!name.trim()) return;
    setCreating(true);
    try {
      await api.post("/categories", { name });
      setName("");
      fetchCategories();
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't create category.");
    } finally {
      setCreating(false);
    }
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

      <div className="max-w-2xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-1">Categories</h1>
        <p className="text-slate-400 text-sm mb-8">
          Categories help route tickets to the right people
        </p>

        {role === "ORG_ADMIN" && (
          <form onSubmit={handleCreate} className="flex gap-3 mb-8">
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Water supply, Wifi, Parking"
              className="flex-1 bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
            />
            <button
              type="submit"
              disabled={creating}
              className="bg-indigo-500 hover:bg-indigo-400 disabled:opacity-50 text-white font-medium text-sm px-4 py-2.5 rounded-lg flex items-center gap-2 transition-all whitespace-nowrap"
            >
              <Plus size={16} />
              Add
            </button>
          </form>
        )}

        {error && (
          <p className="text-red-400 text-xs bg-red-500/10 border border-red-500/20 rounded-lg px-3 py-2 mb-6">
            {error}
          </p>
        )}

        {loading ? (
          <p className="text-slate-400 text-sm">Loading categories...</p>
        ) : categories.length === 0 ? (
          <div className="bg-white/5 border border-white/10 rounded-xl p-12 text-center">
            <Tag size={32} className="mx-auto text-slate-600 mb-3" />
            <p className="text-white font-medium mb-1">No categories yet</p>
            <p className="text-slate-400 text-sm">
              {role === "ORG_ADMIN"
                ? "Add your first category above."
                : "Ask your admin to set up categories."}
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {categories.map((c, i) => (
              <motion.div
                key={c.id}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className="bg-white/5 border border-white/10 rounded-lg px-4 py-3 flex items-center gap-3"
              >
                <div className="w-8 h-8 rounded-lg bg-indigo-500/10 flex items-center justify-center">
                  <Tag size={14} className="text-indigo-400" />
                </div>
                <span className="text-white text-sm font-medium">{c.name}</span>
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}